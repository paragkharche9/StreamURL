package com.pdklabs.streamurlcatcher

import android.app.Application
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StreamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
            
            // Proactive approach: Update engine in the background on startup
            // The library automatically checks if the local version is outdated before downloading
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    YoutubeDL.getInstance().updateYoutubeDL(this@StreamApplication, YoutubeDL.UpdateChannel.STABLE)
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
