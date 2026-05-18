package com.example.notesapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.model.Category
import com.example.notesapp.model.Note
import kotlinx.coroutines.tasks.await



object FirebaseHelper {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUserId: String get() = auth.currentUser?.uid ?: ""

    // ── Categories ──────────────────────────────────────────────────────────

    suspend fun addCategory(category: Category): Result<String> = runCatching {
        val ref = db.collection("users")
            .document(currentUserId)
            .collection("categories")
            .document()
        ref.set(category.copy(id = ref.id, userId = currentUserId)).await()
        ref.id
    }

    suspend fun getCategories(): Result<List<Category>> = runCatching {
        db.collection("users")
            .document(currentUserId)
            .collection("categories")
            .get().await()
            .toObjects(Category::class.java)
    }

    suspend fun updateCategory(category: Category): Result<Unit> = runCatching {
        db.collection("users")
            .document(currentUserId)
            .collection("categories")
            .document(category.id)
            .set(category).await()
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        val notes = db.collection("users")
            .document(currentUserId)
            .collection("notes")
            .whereEqualTo("categoryId", categoryId)
            .get().await()
        val batch = db.batch()
        notes.documents.forEach { batch.delete(it.reference) }
        batch.delete(
            db.collection("users")
                .document(currentUserId)
                .collection("categories")
                .document(categoryId)
        )
        batch.commit().await()
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    suspend fun addNote(note: Note): Result<String> = runCatching {
        val ref = db.collection("users")
            .document(currentUserId)
            .collection("notes")
            .document()
        ref.set(note.copy(id = ref.id, userId = currentUserId)).await()
        ref.id
    }

    suspend fun getNotesByCategory(categoryId: String): Result<List<Note>> = runCatching {
        db.collection("users")
            .document(currentUserId)
            .collection("notes")
            .whereEqualTo("categoryId", categoryId)
            .get().await()
            .toObjects(Note::class.java)
            .sortedByDescending { it.updatedAt }
    }

    suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        db.collection("users")
            .document(currentUserId)
            .collection("notes")
            .document(note.id)
            .set(note.copy(updatedAt = System.currentTimeMillis())).await()
    }

    suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(currentUserId)
            .collection("notes")
            .document(noteId)
            .delete().await()
    }
}