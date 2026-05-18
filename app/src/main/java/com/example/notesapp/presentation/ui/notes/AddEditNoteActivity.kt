package com.example.notesapp.presentation.ui.notes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.ActivityAddEditNoteBinding
import com.example.notesapp.presentation.viewmodel.ActionState
import com.example.notesapp.presentation.viewmodel.NotesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private var noteId: String? = null
    private lateinit var categoryId: String
    private lateinit var categoryColor: String

    private val viewModel: NotesViewModel by viewModel {
        parametersOf(categoryId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        categoryId    = intent.getStringExtra("categoryId") ?: ""
        categoryColor = intent.getStringExtra("categoryColor") ?: "#6750A4"
        noteId        = intent.getStringExtra("noteId")

        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = if (noteId != null) "Edit Note" else "New Note"
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setBackgroundColor(Color.parseColor(categoryColor))
        binding.btnSave.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(categoryColor))

        if (noteId != null) {
            binding.etTitle.setText(intent.getStringExtra("noteTitle"))
            binding.etDescription.setText(intent.getStringExtra("noteDescription"))
        }

        observeActionState()
        binding.btnSave.setOnClickListener { saveNote() }
    }

    private fun observeActionState() {
        lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is ActionState.Loading -> binding.btnSave.isEnabled = false
                    is ActionState.Success -> {
                        Toast.makeText(this@AddEditNoteActivity, "Note saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is ActionState.Error -> {
                        binding.btnSave.isEnabled = true
                        Toast.makeText(this@AddEditNoteActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.resetActionState()
                    }
                    else -> binding.btnSave.isEnabled = true
                }
            }
        }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Enter a title"
            return
        }

        if (noteId != null) {
            viewModel.updateNote(
                Note(id = noteId!!, title = title, description = description, categoryId = categoryId)
            )
        } else {
            viewModel.addNote(
                Note(title = title, description = description, categoryId = categoryId)
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}