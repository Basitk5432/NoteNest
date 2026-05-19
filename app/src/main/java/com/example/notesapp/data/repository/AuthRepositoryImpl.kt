package com.example.notesapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.data.model.User
import com.example.notesapp.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun login(email: String, password: String): Result<FirebaseUser> =
        runCatching {
            auth.signInWithEmailAndPassword(email, password).await().user!!
        }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name).build()
        result.user!!.updateProfile(profileUpdates).await()

        // Use EMAIL as document ID instead of UID
        db.collection("users")
            .document(email)           // ← email as document ID
            .set(
                User(
                    uid = result.user!!.uid,
                    name = name,
                    email = email
                )
            ).await()

        result.user!!
    }

    override suspend fun googleSignIn(idToken: String): Result<FirebaseUser> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            // Use EMAIL as document ID
            db.collection("users")
                .document(result.user!!.email ?: result.user!!.uid)
                .set(
                    User(
                        uid = result.user!!.uid,
                        name = result.user!!.displayName ?: "",
                        email = result.user!!.email ?: ""
                    )
                ).await()

            result.user!!
        }

    override fun logout() {
        auth.signOut()
    }
}