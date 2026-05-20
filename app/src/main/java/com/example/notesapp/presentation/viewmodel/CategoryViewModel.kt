package com.example.notesapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Category
import com.example.notesapp.domain.usecase.notes.AddCategoryUseCase
import com.example.notesapp.domain.usecase.notes.ArchiveCategoryUseCase
import com.example.notesapp.domain.usecase.notes.DeleteCategoryUseCase
import com.example.notesapp.domain.usecase.notes.GetArchivedCategoriesUseCase
import com.example.notesapp.domain.usecase.notes.GetCategoriesUseCase
import com.example.notesapp.domain.usecase.notes.RenameCategoryUseCase
import com.example.notesapp.domain.usecase.notes.UnarchiveCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val addCategoryUseCase: AddCategoryUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val archiveCategoryUseCase: ArchiveCategoryUseCase,
    private val unarchiveCategoryUseCase: UnarchiveCategoryUseCase,
    private val getArchivedCategoriesUseCase: GetArchivedCategoriesUseCase,
    private val renameCategoryUseCase: RenameCategoryUseCase
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
                onFailure = { CategoryState.Error(it.message ?: "Failed to load") }
            )
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = addCategoryUseCase(category)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to add") }
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = deleteCategoryUseCase(categoryId)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to delete") }
            )
        }
    }

    fun archiveCategory(categoryId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            try {
                val result = archiveCategoryUseCase(categoryId)
                if (result.isSuccess) {
                    _actionState.value = ActionState.Success
                } else {
                    _actionState.value = ActionState.Error(
                        result.exceptionOrNull()?.message ?: "Archive failed"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error(e.message ?: "Archive failed")
            }
        }
    }

    fun unarchiveCategory(categoryId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = unarchiveCategoryUseCase(categoryId)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to unarchive") }
            )
        }
    }

    fun getArchivedCategories() {
        viewModelScope.launch {
            _categoriesState.value = CategoryState.Loading
            val result = getArchivedCategoriesUseCase()
            _categoriesState.value = result.fold(
                onSuccess = { CategoryState.Success(it) },
                onFailure = { CategoryState.Error(it.message ?: "Failed to load") }
            )
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = renameCategoryUseCase(oldName, newName)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to rename") }
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