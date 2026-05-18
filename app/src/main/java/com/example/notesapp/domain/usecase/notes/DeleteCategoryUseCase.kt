package com.example.notesapp.domain.usecase.notes

import com.example.notesapp.domain.repository.NotesRepository

class DeleteCategoryUseCase(private val repository: NotesRepository) {
    suspend operator fun invoke(categoryId: String) =
        repository.deleteCategory(categoryId)
}