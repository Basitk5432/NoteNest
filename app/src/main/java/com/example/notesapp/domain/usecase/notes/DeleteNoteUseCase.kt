package com.example.notesapp.domain.usecase.notes

import com.example.notesapp.domain.repository.NotesRepository

class DeleteNoteUseCase(private val repository: NotesRepository) {
    suspend operator fun invoke(noteId: String, categoryId: String) =
        repository.deleteNote(noteId, categoryId)
}