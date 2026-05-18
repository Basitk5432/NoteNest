package com.example.notesapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Category
import com.example.notesapp.domain.usecase.notes.AddCategoryUseCase
import com.example.notesapp.domain.usecase.notes.DeleteCategoryUseCase
import com.example.notesapp.domain.usecase.notes.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<CategoryState>(CategoryState.Idle)
    val categoriesState: StateFlow<CategoryState> = _categoriesState

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    fun getCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoryState.Loading
            val result = getCategoriesUseCase()
            _categoriesState.value = result.fold(
                onSuccess = { CategoryState.Success(it) },
                onFailure = { CategoryState.Error(it.message ?: "Failed to load categories") }
            )
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = addCategoryUseCase(category)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to add category") }
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = deleteCategoryUseCase(categoryId)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to delete category") }
            )
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }
}

sealed class CategoryState {
    object Idle : CategoryState()
    object Loading : CategoryState()
    data class Success(val categories: List<Category>) : CategoryState()
    data class Error(val message: String) : CategoryState()
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    object Success : ActionState()
    data class Error(val message: String) : ActionState()
}