package com.pdklabs.streamurlcatcher

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.DynamicColors

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivitiesIfAvailable(application)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)
        
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val tvLicenses: TextView = findViewById(R.id.tvLicenses)
        tvLicenses.text = getLicensesText()
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.about_content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
    }

    private fun getLicensesText(): String {
        return """
            Jetpack Media3 (ExoPlayer)
            Copyright 2026 Google LLC
            Apache License 2.0
            
            Material Components for Android
            Copyright 2026 Google LLC
            Apache License 2.0
            
            AndroidX Libraries
            Copyright 2026 The Android Open Source Project
            Apache License 2.0
            
            Kotlin Standard Library
            Copyright 2010-2024 JetBrains s.r.o.
            Apache License 2.0
        """.trimIndent()
    }
}