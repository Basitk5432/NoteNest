package com.example.notesapp.presentation.ui.onboarding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.notesapp.R
import com.example.notesapp.data.model.OnboardingItem
import com.example.notesapp.databinding.ActivityOnboardingBinding
import com.example.notesapp.presentation.adapter.OnboardingAdapter
import com.example.notesapp.presentation.ui.auth.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefs: SharedPreferences

    private val onboardingItems = listOf(
        OnboardingItem(
            title = "Organize Your Thoughts",
            description = "Create categories for everything in your life — work, home, personal and more.",
            imageRes = R.drawable.ic_onboarding1
        ),
        OnboardingItem(
            title = "Never Forget Anything",
            description = "Write notes inside each category and keep all your important information in one place.",
            imageRes = R.drawable.ic_onboarding2
        ),
        OnboardingItem(
            title = "Safe & Secure",
            description = "Your notes are private and protected. Only you can see your data with secure login.",
            imageRes = R.drawable.ic_onboarding3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("NoteNestPrefs", MODE_PRIVATE)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        // Listen to page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateDots(position)
                updateButton(position)
            }
        })
    }

    private fun setupButtons() {
        // Next button
        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < onboardingItems.size - 1) {
                // Go to next page
                binding.viewPager.currentItem = current + 1
            } else {
                // Last page — go to Login
                goToLogin()
            }
        }

        // Skip button
        binding.tvSkip.setOnClickListener {
            goToLogin()
        }
    }

    private fun updateDots(position: Int) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index == position) R.drawable.bg_dot_active
                else R.drawable.bg_dot_inactive
            )
        }
    }

    private fun updateButton(position: Int) {
        if (position == onboardingItems.size - 1) {
            // Last page — change button text to Get Started
            binding.btnNext.text = "Get Started"
            // Hide skip on last page
            binding.tvSkip.visibility = View.INVISIBLE
        } else {
            binding.btnNext.text = "Next"
            binding.tvSkip.visibility = View.VISIBLE
        }
    }

    private fun goToLogin() {
        // Save that onboarding is completed
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}