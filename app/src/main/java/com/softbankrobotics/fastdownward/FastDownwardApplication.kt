package com.softbankrobotics.fastdownward

import android.app.Application
import com.google.firebase.FirebaseApp

class FastDownwardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.WITH_FIREBASE) {
            if (FirebaseApp.initializeApp(this) == null) {
                val missingKeys = mapOf(
                    "project_id" to R.string.project_id,
                    "google_app_id" to R.string.google_app_id,
                    "default_web_client_id" to R.string.default_web_client_id,
                    "gcm_defaultSenderId" to R.string.gcm_defaultSenderId,
                    "google_api_key" to R.string.google_api_key,
                    "google_crash_reporting_api_key" to R.string.google_crash_reporting_api_key
                ).mapNotNull { if (getString(it.value).isEmpty()) it.key else null }
                throw IllegalStateException(
                    "Missing Firebase configuration strings: ${missingKeys.joinToString(", ") { "\"$it\"" }}"
                )
            }
        }
    }
}