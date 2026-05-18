package com.example.notesapp.domain.usecase.auth

import com.example.notesapp.domain.repository.AuthRepository

class GoogleSignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String) =
        repository.googleSignIn(idToken)
}