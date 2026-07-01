package com.pdklabs.streamurlcatcher

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamApplication : Application() {
    override fun onCreate() {
        val tag = "StreamApplication"

        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
            
            // Proactive approach: Update engine in the background on startup
            // The library automatically checks if the local version is outdated before downloading
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(tag, "Updating youtube-dl...")
                    YoutubeDL.getInstance().updateYoutubeDL(this@StreamApplication, YoutubeDL.UpdateChannel.STABLE)
                    Log.d(tag, "youtube-dl update check complete.")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to update youtube-dl", e)
                }
            }
        } catch (e: Exception) { Log.e(tag, "Failed to initialize YoutubeDL", e) }
    }
}
