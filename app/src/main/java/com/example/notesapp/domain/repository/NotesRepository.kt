package com.example.notesapp.domain.repository

import com.example.notesapp.data.model.Category
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.model.User

interface NotesRepository {

    // ── Existing ────────────────────────────────────────────────────────────
    suspend fun addCategory(category: Category): Result<String>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun addNote(note: Note): Result<String>
    suspend fun getNotesByCategory(categoryId: String): Result<List<Note>>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String): Result<Unit>

    // ── New — fetch user data ────────────────────────────────────────────────
    suspend fun saveUserToFirestore(user: User): Result<Unit>
    suspend fun getUserByUid(uid: String): Result<User>
    suspend fun getUserByEmail(email: String): Result<User>
}