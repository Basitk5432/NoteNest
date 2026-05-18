package com.example.notesapp.ui.notes


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.adapter.NoteAdapter
import com.example.notesapp.databinding.ActivityNotesBinding
import com.example.notesapp.model.Note
import com.example.notesapp.utils.FirebaseHelper
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private lateinit var noteAdapter: NoteAdapter
    private val notes = mutableListOf<Note>()

    private lateinit var categoryId: String
    private lateinit var categoryName: String
    private lateinit var categoryColor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId    = intent.getStringExtra("categoryId") ?: ""
        categoryName  = intent.getStringExtra("categoryName") ?: "Notes"
        categoryColor = intent.getStringExtra("categoryColor") ?: "#6750A4"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = categoryName
            setDisplayHomeAsUpEnabled(true)
        }
        binding.toolbar.setBackgroundColor(Color.parseColor(categoryColor))
        binding.fabAddNote.backgroundTintList =
            android.content.res.ColorStateList.valueOf(Color.parseColor(categoryColor))

        setupRecyclerView()

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
        loadNotes()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            notes,
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
            onNoteDelete = { note -> deleteNote(note) }
        )
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = noteAdapter
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = FirebaseHelper.getNotesByCategory(categoryId)
            binding.progressBar.visibility = View.GONE
            result.onSuccess { list ->
                notes.clear()
                notes.addAll(list)
                noteAdapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                Toast.makeText(this@NotesActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            FirebaseHelper.deleteNote(note.id).onSuccess {
                Toast.makeText(this@NotesActivity, "Note deleted", Toast.LENGTH_SHORT).show()
                loadNotes()
            }.onFailure {
                Toast.makeText(this@NotesActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}