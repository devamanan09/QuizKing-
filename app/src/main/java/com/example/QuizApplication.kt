package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class QuizApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebase()
    }

    private fun initFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:199916526127:android:quizking")
                    .setApiKey("AIzaSyFakeKeyForLocalInitialization00")
                    .setProjectId("quizking-app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("QuizApplication", "FirebaseApp initialized with runtime options.")
            }
        } catch (e: Exception) {
            Log.w("QuizApplication", "FirebaseApp initialization handled: ${e.message}")
        }
    }
}
