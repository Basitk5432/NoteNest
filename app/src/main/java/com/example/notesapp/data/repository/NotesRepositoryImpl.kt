package com.example.notesapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.notesapp.data.model.Category
import com.example.notesapp.data.model.Note
import com.example.notesapp.domain.repository.NotesRepository
import kotlinx.coroutines.tasks.await

class NotesRepositoryImpl(
    private val db: FirebaseFirestore,
    private val userId: String
) : NotesRepository {

    // userDoc now points to email document
    private val userDoc get() = db.collection("users").document(userId)


    override suspend fun addCategory(category: Category): Result<String> = runCatching {
        // Check if category name already exists
        val existing = userDoc.collection("categories")
            .document(category.name)
            .get().await()

        if (existing.exists()) {
            throw Exception("Category '${category.name}' already exists!")
        }

        val ref = userDoc.collection("categories").document(category.name)
        ref.set(category.copy(id = category.name, userId = userId)).await()
        category.name
    }

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        userDoc.collection("categories")
            .get().await()
            .toObjects(Category::class.java)
            .sortedBy { it.createdAt }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> = runCatching {
        // Delete all notes inside this category first
        val notes = userDoc.collection("categories")
            .document(categoryId)
            .collection("notes")
            .get().await()

        val batch = db.batch()
        notes.documents.forEach { batch.delete(it.reference) }
        batch.delete(
            userDoc.collection("categories").document(categoryId)
        )
        batch.commit().await()
    }


    override suspend fun addNote(note: Note): Result<String> = runCatching {
        val existing = userDoc
            .collection("categories")
            .document(note.categoryId)
            .collection("notes")
            .document(note.title)
            .get().await()

        if (existing.exists()) {
            throw Exception("A note with title '${note.title}' already exists!")
        }

        val ref = userDoc
            .collection("categories")
            .document(note.categoryId)
            .collection("notes")
            .document(note.title)

        ref.set(note.copy(id = note.title, userId = userId)).await()
        note.title
    }

    override suspend fun getNotesByCategory(categoryId: String): Result<List<Note>> =
        runCatching {
            userDoc.collection("categories")
                .document(categoryId)
                .collection("notes")
                .get().await()
                .toObjects(Note::class.java)
                .sortedByDescending { it.updatedAt }
        }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        userDoc.collection("categories")
            .document(note.categoryId)
            .collection("notes")
            .document(note.id)   // ← note.id is already the title
            .set(note.copy(updatedAt = System.currentTimeMillis())).await()
    }

    override suspend fun deleteNote(noteId: String, categoryId: String): Result<Unit> =
        runCatching {
            userDoc.collection("categories")
                .document(categoryId)
                .collection("notes")
                .document(noteId)    // ← noteId is the title
                .delete().await()
        }


    override suspend fun saveUserToFirestore(user: com.example.notesapp.data.model.User): Result<Unit> =
        runCatching {
            userDoc.set(user).await()
        }

    override suspend fun getUserByUid(uid: String): Result<com.example.notesapp.data.model.User> =
        runCatching {
            db.collection("users")
                .whereEqualTo("uid", uid)
                .get().await()
                .toObjects(com.example.notesapp.data.model.User::class.java)
                .first()
        }

    override suspend fun getUserByEmail(email: String): Result<com.example.notesapp.data.model.User> =
        runCatching {
            db.collection("users")
                .document(email)
                .get().await()
                .toObject(com.example.notesapp.data.model.User::class.java)!!
        }
}