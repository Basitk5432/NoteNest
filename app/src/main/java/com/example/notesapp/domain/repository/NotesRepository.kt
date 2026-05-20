package com.example.notesapp.domain.repository

import com.example.notesapp.data.model.Category
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.model.User

interface NotesRepository {
    suspend fun addCategory(category: Category): Result<String>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun archiveCategory(categoryId: String): Result<Unit>      // ← new
    suspend fun unarchiveCategory(categoryId: String): Result<Unit>    // ← new
    suspend fun getArchivedCategories(): Result<List<Category>>        // ← new
    suspend fun renameCategory(oldName: String, newName: String): Result<Unit> // ← new
    suspend fun addNote(note: Note): Result<String>
    suspend fun getNotesByCategory(categoryId: String): Result<List<Note>>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun deleteNote(noteId: String, categoryId: String): Result<Unit>
    suspend fun saveUserToFirestore(user: User): Result<Unit>
    suspend fun getUserByUid(uid: String): Result<User>
    suspend fun getUserByEmail(email: String): Result<User>
}