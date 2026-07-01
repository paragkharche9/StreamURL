package com.pdklabs.streamurlcatcher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.color.DynamicColors
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var cardContainer: View
    private lateinit var headerOverlay: View
    private var player: ExoPlayer? = null
    
    private lateinit var tvUrl: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnShare: Button
    private lateinit var btnSettings: ImageButton
    private var streamUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        cardContainer = findViewById(R.id.cardContainer)
        headerOverlay = findViewById(R.id.headerOverlay)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerOverlay.setPadding(headerOverlay.paddingLeft, systemBars.top, headerOverlay.paddingRight, headerOverlay.paddingBottom)
            v.setPadding(v.paddingLeft, 0, v.paddingRight, systemBars.bottom)
            insets
        }

        playerView = findViewById(R.id.playerView)
        tvUrl = findViewById(R.id.tvUrl)
        btnCopy = findViewById(R.id.btnCopy)
        btnShare = findViewById(R.id.btnShare)
        btnSettings = findViewById(R.id.btnSettings)

        btnSettings.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        tvUrl.setOnClickListener {
            pasteFromClipboard()
        }

        handleIntent(intent)

        btnCopy.setOnClickListener {
            streamUrl?.let { url ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Stream URL", url))
                Toast.makeText(this, "URL Copied", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            streamUrl?.let { url -> shortenAndShare(url) }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        if (player == null) {
            initializePlayer()
            streamUrl?.let { url ->
                player?.setMediaItem(MediaItem.fromUri(url.toUri()))
                player?.prepare()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val item = clipboard.primaryClip?.getItemAt(0)
        val pasteData = item?.text?.toString()
        if (!pasteData.isNullOrBlank() && pasteData.startsWith("http")) {
            processUrl(pasteData)
        } else {
            Toast.makeText(this, "No valid URL in clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shortenAndShare(originalUrl: String) {
        lifecycleScope.launch {
            val finalUrl = withContext(Dispatchers.IO) { tryShorten(originalUrl) } ?: originalUrl
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, finalUrl)
            }
            startActivity(Intent.createChooser(shareIntent, "Share URL via"))
        }
    }

    private fun tryShorten(originalUrl: String): String? {
        return try {
            val apiUrl = "https://tinyurl.com/api-create.php?url=${URLEncoder.encode(originalUrl, "UTF-8")}"
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (_: Exception) { null }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val type = intent?.type

        if (Intent.ACTION_VIEW == action && (type?.startsWith("video/") == true || intent.data?.scheme?.startsWith("http") == true)) {
            intent.data?.let { uri -> processUrl(uri.toString()) }
        } else if (Intent.ACTION_SEND == action && type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText -> processUrl(sharedText) }
        }
    }

    private fun processUrl(url: String) {
        lifecycleScope.launch {
            tvUrl.text = "Processing..."
            
            val isMediaLink = url.contains(".mp4") || url.contains(".m3u8") || 
                             url.contains(".mkv") || url.contains(".mov") || 
                             url.contains(".mpd") || url.contains(".ism")

            val finalUrl = if (url.startsWith("http") && !isMediaLink) {
                withContext(Dispatchers.IO) {
                    try {
                        val request = YoutubeDLRequest(url)
                        // Reverting to 'best' to ensure a single playable link is returned
                        // The previous complex string required FFmpeg to merge streams.
                        request.addOption("-f", "best")
                        val videoInfo = YoutubeDL.getInstance().getInfo(request)
                        videoInfo.url ?: url
                    } catch (_: Exception) { url }
                }
            } else url

            streamUrl = finalUrl
            tvUrl.text = finalUrl
            
            try {
                player?.setMediaItem(MediaItem.fromUri(finalUrl.toUri()))
                player?.prepare()
                player?.playWhenReady = true
                player?.play()
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
