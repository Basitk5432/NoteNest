package com.example.notesapp.domain.usecase.auth

import com.example.notesapp.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String) =
        repository.register(name, email, password)
}