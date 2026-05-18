package com.example.notesapp.presentation.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.databinding.ActivityRegisterBinding
import com.example.notesapp.presentation.ui.main.MainActivity
import com.example.notesapp.presentation.viewmodel.AuthState
import com.example.notesapp.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAuthState()

        binding.btnRegister.setOnClickListener { register() }
        binding.tvLogin.setOnClickListener { finish() }
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> setLoading(true)
                    is AuthState.Success -> {
                        setLoading(false)
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                    is AuthState.Error -> {
                        setLoading(false)
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetState()
                    }
                    else -> setLoading(false)
                }
            }
        }
    }

    private fun register() {
        val name     = binding.etName.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm  = binding.etConfirmPassword.text.toString().trim()

        // Name — only alphabets A-Z and spaces allowed
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }
        if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
            Toast.makeText(this, "Name can only contain letters A-Z", Toast.LENGTH_SHORT).show()
            return
        }

        // Email — only letters, numbers, @ and . allowed
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }
        if (!email.matches(Regex("^[a-zA-Z0-9@.]+$"))) {
            Toast.makeText(this, "Email can only contain letters, numbers, @ and .", Toast.LENGTH_SHORT).show()
            return
        }

        // Password fields filled
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show()
            return
        }

        // Passwords match
        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Minimum length
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Must have at least 1 capital letter
        if (!password.any { it.isUpperCase() }) {
            Toast.makeText(this, "Password must contain at least one capital letter", Toast.LENGTH_SHORT).show()
            return
        }

        // Must have at least 1 number
        if (!password.any { it.isDigit() }) {
            Toast.makeText(this, "Password must contain at least one number", Toast.LENGTH_SHORT).show()
            return
        }

        // Must have at least 1 special character
        if (!password.any { !it.isLetterOrDigit() }) {
            Toast.makeText(this, "Password must contain at least one special character e.g. @, #, !", Toast.LENGTH_SHORT).show()
            return
        }

        // All validations passed
        viewModel.register(name, email, password)
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}