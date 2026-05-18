package com.example.notesapp.domain.usecase.notes

import com.example.notesapp.data.model.Category
import com.example.notesapp.domain.repository.NotesRepository

class AddCategoryUseCase(private val repository: NotesRepository) {
    suspend operator fun invoke(category: Category) =
        repository.addCategory(category)
}