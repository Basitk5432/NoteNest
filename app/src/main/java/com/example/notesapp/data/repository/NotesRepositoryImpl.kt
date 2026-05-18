package com.example.notesapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.data.model.Category
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.model.User
import com.example.notesapp.domain.repository.NotesRepository
import kotlinx.coroutines.tasks.await

class NotesRepositoryImpl(
    private val db: FirebaseFirestore,
    private val userId: String
) : NotesRepository {

    private val userDoc get() = db.collection("users").document(userId)

    override suspend fun saveUserToFirestore(user: User): Result<Unit> = runCatching {
        userDoc.set(user).await()
    }

    override suspend fun getUserByUid(uid: String): Result<User> = runCatching {
        db.collection("users")
            .document(uid)
            .get().await()
            .toObject(User::class.java)!!
    }

    override suspend fun getUserByEmail(email: String): Result<User> = runCatching {
        db.collection("users")
            .whereEqualTo("email", email)
            .get().await()
            .toObjects(User::class.java)
            .first()
    }

    override suspend fun addCategory(category: Category): Result<String> = runCatching {
        val ref = userDoc.collection("categories").document()
        ref.set(category.copy(id = ref.id, userId = userId)).await()
        ref.id
    }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        userDoc.collection("categories")
            .get().await()
            .toObjects(Category::class.java)
            .sortedBy { it.createdAt }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        val notes = userDoc.collection("notes")
            .whereEqualTo("categoryId", categoryId)
            .get().await()
        val batch = db.batch()
        notes.documents.forEach { batch.delete(it.reference) }
        batch.delete(userDoc.collection("categories").document(categoryId))
        batch.commit().await()
    }

    override suspend fun addNote(note: Note): Result<String> = runCatching {
        val ref = userDoc.collection("notes").document()
        ref.set(note.copy(id = ref.id, userId = userId)).await()
        ref.id
    }

    override suspend fun getNotesByCategory(categoryId: String): Result<List<Note>> =
        runCatching {
            userDoc.collection("notes")
                .whereEqualTo("categoryId", categoryId)
                .get().await()
                .toObjects(Note::class.java)
                .sortedByDescending { it.updatedAt }
        }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        userDoc.collection("notes")
            .document(note.id)
            .set(note.copy(updatedAt = System.currentTimeMillis())).await()
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> = runCatching {
        userDoc.collection("notes").document(noteId).delete().await()
    }
}