package com.example.notesapp.presentation.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt

class NoteAdapter(
    private val items: MutableList<Note>,
    private val categoryColor: String,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    inner class ViewHolder(val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = items[position]
        holder.binding.apply {
            tvTitle.text = note.title
            tvDescription.text = note.description.ifEmpty { "No description" }
            tvDate.text = dateFormat.format(Date(note.updatedAt))
            colorBar.setBackgroundColor(categoryColor.toColorInt())
            root.setOnClickListener { onNoteClick(note) }
            root.setOnLongClickListener {
                AlertDialog.Builder(root.context)
                    .setTitle("Delete note")
                    .setMessage("Delete \"${note.title}\"?")
                    .setPositiveButton("Delete") { _, _ -> onNoteDelete(note) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateNotes(newNotes: List<Note>) {
        items.clear()
        items.addAll(newNotes)
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}