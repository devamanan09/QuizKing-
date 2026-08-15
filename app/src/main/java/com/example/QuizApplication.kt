package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class QuizApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.w("QuizApplication", "FirebaseApp initialization handled: ${e.message}")
        }
    }
}
