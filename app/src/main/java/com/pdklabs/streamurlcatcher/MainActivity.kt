package com.pdklabs.streamurlcatcher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var playerView: PlayerView
    private lateinit var cardContainer: View
    private var player: ExoPlayer? = null
    
    private lateinit var tvUrl: TextView
    private lateinit var btnCopy: Button
    private lateinit var btnShare: Button
    private var streamUrl: String? = null
    
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        appBarLayout = findViewById(R.id.appBarLayout)
        cardContainer = findViewById(R.id.cardContainer)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            if (!isFullscreen) {
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            } else {
                v.setPadding(0, 0, 0, 0)
            }
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                else -> false
            }
        }

        playerView = findViewById(R.id.playerView)
        tvUrl = findViewById(R.id.tvUrl)
        btnCopy = findViewById(R.id.btnCopy)
        btnShare = findViewById(R.id.btnShare)

        initializePlayer()

        playerView.setFullscreenButtonClickListener { isFullscreenClick ->
            toggleFullscreen(isFullscreenClick)
        }

        handleIntent(intent)

        btnCopy.setOnClickListener {
            streamUrl?.let { url ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Stream URL", url)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "URL Copied", Toast.LENGTH_SHORT).show()
            }
        }

        btnShare.setOnClickListener {
            streamUrl?.let { url ->
                shortenAndShare(url)
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullscreen) {
                    toggleFullscreen(false)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun toggleFullscreen(fullScreen: Boolean) {
        isFullscreen = fullScreen
        
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        if (fullScreen) {
            appBarLayout.visibility = View.GONE
            cardContainer.visibility = View.GONE
            
            // Intelligent orientation based on video dimensions
            val videoSize = player?.videoSize
            if (videoSize != null && videoSize.height > videoSize.width) {
                // For vertical videos, allow portrait rotation
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
            } else {
                // For horizontal videos, force landscape rotation
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            }

            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            // Adjust playerView to match parent
            val params = playerView.layoutParams as ViewGroup.MarginLayoutParams
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.setMargins(0, 0, 0, 0)
            playerView.layoutParams = params
        } else {
            appBarLayout.visibility = View.VISIBLE
            cardContainer.visibility = View.VISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            
            // Reset playerView layout params - ConstraintLayout will handle it
            val params = playerView.layoutParams as ViewGroup.MarginLayoutParams
            params.width = 0
            params.height = 0
            playerView.layoutParams = params
        }
    }

    private fun shortenAndShare(originalUrl: String) {
        lifecycleScope.launch {
            val shortenedUrl = withContext(Dispatchers.IO) {
                tryShorten(originalUrl, "https://is.gd")
            }

            val finalUrl = shortenedUrl ?: originalUrl
            if (shortenedUrl == null) {
                Toast.makeText(this@MainActivity, "Shortener failed, sharing original", Toast.LENGTH_SHORT).show()
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, finalUrl)
            }
            startActivity(Intent.createChooser(shareIntent, "Share URL via"))
        }
    }

    private fun tryShorten(originalUrl: String, baseUrl: String): String? {
        return try {
            val apiUrl = "$baseUrl/create.php?format=simple&url=${URLEncoder.encode(originalUrl, "UTF-8")}"
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
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

        if (Intent.ACTION_VIEW == action && type?.startsWith("video/") == true) {
            intent.data?.let { uri ->
                processUrl(uri.toString())
            }
        } else if (Intent.ACTION_SEND == action && type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                processUrl(sharedText)
            }
        }
    }

    private fun processUrl(url: String) {
        streamUrl = url
        tvUrl.text = url
        
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}