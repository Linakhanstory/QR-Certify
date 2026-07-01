package com.example.qr_certify

import android.app.Application
import com.cloudinary.android.MediaManager

class CertifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "dt4unxor3",
            "api_key" to "742915995147664"
        )
        MediaManager.init(this, config)
    }
}
