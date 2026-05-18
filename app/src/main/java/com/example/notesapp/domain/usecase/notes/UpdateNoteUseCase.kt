package com.example.notesapp.domain.usecase.notes

import com.example.notesapp.data.model.Note
import com.example.notesapp.domain.repository.NotesRepository

class UpdateNoteUseCase(private val repository: NotesRepository) {
    suspend operator fun invoke(note: Note) =
        repository.updateNote(note)
}