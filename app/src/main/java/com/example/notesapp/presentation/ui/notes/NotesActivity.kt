package com.example.notesapp.presentation.ui.notes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.databinding.ActivityNotesBinding
import com.example.notesapp.presentation.adapter.NoteAdapter
import com.example.notesapp.presentation.viewmodel.ActionState
import com.example.notesapp.presentation.viewmodel.NotesState
import com.example.notesapp.presentation.viewmodel.NotesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import androidx.core.graphics.toColorInt

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var categoryId: String
    private lateinit var categoryColor: String
    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: NotesViewModel by viewModel {
        parametersOf(categoryId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        categoryId = intent.getStringExtra("categoryId") ?: ""
        categoryColor = intent.getStringExtra("categoryColor") ?: "#6750A4"
        val categoryName = intent.getStringExtra("categoryName") ?: "Notes"

        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = categoryName
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setBackgroundColor(categoryColor.toColorInt())
        binding.fabAddNote.backgroundTintList =
            android.content.res.ColorStateList.valueOf(categoryColor.toColorInt())

        setupRecyclerView()
        observeStates()

        binding.fabAddNote.setOnClickListener {
            startActivity(
                Intent(this, AddEditNoteActivity::class.java).apply {
                    putExtra("categoryId", categoryId)
                    putExtra("categoryColor", categoryColor)
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getNotes()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            mutableListOf(),
            categoryColor,
            onNoteClick = { note ->
                startActivity(
                    Intent(this, AddEditNoteActivity::class.java).apply {
                        putExtra("noteId", note.id)
                        putExtra("noteTitle", note.title)
                        putExtra("noteDescription", note.description)
                        putExtra("categoryId", categoryId)
                        putExtra("categoryColor", categoryColor)
                    }
                )
            },
            onNoteDelete = { note -> viewModel.deleteNote(note.id) }
        )
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = noteAdapter
        }
    }

    private fun observeStates() {
        lifecycleScope.launch {
            viewModel.notesState.collect { state ->
                when (state) {
                    is NotesState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is NotesState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        noteAdapter.updateNotes(state.notes)
                        binding.tvEmpty.visibility =
                            if (state.notes.isEmpty()) View.VISIBLE else View.GONE
                    }

                    is NotesState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@NotesActivity, state.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is ActionState.Success -> {
                        viewModel.getNotes()
                        viewModel.resetActionState()
                    }

                    is ActionState.Error -> {
                        Toast.makeText(this@NotesActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetActionState()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}