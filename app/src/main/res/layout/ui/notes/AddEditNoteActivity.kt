package com.example.notesapp.ui.notes


import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.notesapp.databinding.ActivityAddEditNoteBinding
import com.example.notesapp.model.Note
import com.example.notesapp.utils.FirebaseHelper
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private var noteId: String? = null
    private lateinit var categoryId: String
    private lateinit var categoryColor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteId        = intent.getStringExtra("noteId")
        categoryId    = intent.getStringExtra("categoryId") ?: ""
        categoryColor = intent.getStringExtra("categoryColor") ?: "#6750A4"

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

        binding.btnSave.setOnClickListener { saveNote() }
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Enter a title"
            return
        }

        lifecycleScope.launch {
            val result = if (noteId != null) {
                FirebaseHelper.updateNote(
                    Note(id = noteId!!, title = title, description = description, categoryId = categoryId)
                )
            } else {
                FirebaseHelper.addNote(
                    Note(title = title, description = description, categoryId = categoryId)
                ).map { }
            }

            result.onSuccess {
                Toast.makeText(this@AddEditNoteActivity, "Note saved!", Toast.LENGTH_SHORT).show()
                finish()
            }.onFailure {
                Toast.makeText(this@AddEditNoteActivity, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}