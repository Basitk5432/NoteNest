package com.example.notesapp.domain.usecase.notes

import com.example.notesapp.domain.repository.NotesRepository

class GetCategoriesUseCase(private val repository: NotesRepository) {
    suspend operator fun invoke() =
        repository.getCategories()
}