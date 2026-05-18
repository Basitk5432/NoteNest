package com.example.notesapp.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.databinding.ItemCategoryBinding
import com.example.notesapp.model.Category

class CategoryAdapter(
    private val items: List<Category>,
    private val onCategoryClick: (Category) -> Unit,
    private val onCategoryDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = items[position]
        holder.binding.apply {
            tvCategoryName.text = category.name
            tvInitial.text = category.name.first().uppercaseChar().toString()
            cardView.setCardBackgroundColor(Color.parseColor(category.color))

            root.setOnClickListener { onCategoryClick(category) }
            root.setOnLongClickListener {
                AlertDialog.Builder(root.context)
                    .setTitle("Delete category")
                    .setMessage("Delete \"${category.name}\" and all its notes?")
                    .setPositiveButton("Delete") { _, _ -> onCategoryDelete(category) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
        }
    }

    override fun getItemCount() = items.size
}