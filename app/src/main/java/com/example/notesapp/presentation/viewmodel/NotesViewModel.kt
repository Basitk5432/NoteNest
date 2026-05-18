package com.example.notesapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.model.Note
import com.example.notesapp.domain.usecase.notes.AddNoteUseCase
import com.example.notesapp.domain.usecase.notes.DeleteNoteUseCase
import com.example.notesapp.domain.usecase.notes.GetNotesUseCase
import com.example.notesapp.domain.usecase.notes.UpdateNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(
    private val addNoteUseCase: AddNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val categoryId: String
) : ViewModel() {

    private val _notesState = MutableStateFlow<NotesState>(NotesState.Idle)
    val notesState: StateFlow<NotesState> = _notesState

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState

    fun getNotes() {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading
            val result = getNotesUseCase(categoryId)
            _notesState.value = result.fold(
                onSuccess = { NotesState.Success(it) },
                onFailure = { NotesState.Error(it.message ?: "Failed to load notes") }
            )
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = addNoteUseCase(note)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to add note") }
            )
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = updateNoteUseCase(note)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to update note") }
            )
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            val result = deleteNoteUseCase(noteId)
            _actionState.value = result.fold(
                onSuccess = { ActionState.Success },
                onFailure = { ActionState.Error(it.message ?: "Failed to delete note") }
            )
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }
}

sealed class NotesState {
    object Idle : NotesState()
    object Loading : NotesState()
    data class Success(val notes: List<Note>) : NotesState()
    data class Error(val message: String) : NotesState()
}