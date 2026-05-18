package com.example.notesapp.presentation.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.notesapp.databinding.ActivitySplashBinding
import com.example.notesapp.presentation.ui.auth.LoginActivity
import com.example.notesapp.presentation.ui.main.MainActivity
import com.example.notesapp.presentation.ui.onboarding.OnboardingActivity
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth: FirebaseAuth by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("NoteNestPrefs", MODE_PRIVATE)
            val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)

            val destination = when {
                auth.currentUser != null -> MainActivity::class.java
                !onboardingCompleted -> OnboardingActivity::class.java
                else -> LoginActivity::class.java
            }

            startActivity(Intent(this, destination))
            finish()
        }, 2000L)
    }
}