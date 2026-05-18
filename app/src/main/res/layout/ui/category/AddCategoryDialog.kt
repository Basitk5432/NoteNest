package com.example.notesapp.ui.category

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import com.example.notesapp.databinding.DialogAddCategoryBinding
import com.example.notesapp.model.Category
import com.example.notesapp.utils.FirebaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddCategoryDialog(
    context: Context,
    private val onCategoryAdded: () -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddCategoryBinding

    private val colorOptions = listOf(
        "#6750A4", "#B5264C", "#006E1C",
        "#904D00", "#006496", "#6B5778"
    )
    private var selectedColor = colorOptions[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setupColorChips()

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener { saveCategory() }
    }

    private fun setupColorChips() {
        val colorViews = listOf(
            binding.color1, binding.color2, binding.color3,
            binding.color4, binding.color5, binding.color6
        )
        colorViews.forEachIndexed { index, view ->
            view.setBackgroundColor(Color.parseColor(colorOptions[index]))
            view.setOnClickListener {
                selectedColor = colorOptions[index]
                colorViews.forEach { it.alpha = 0.4f }
                view.alpha = 1.0f
            }
        }
        colorViews[0].alpha = 1.0f
        colorViews.drop(1).forEach { it.alpha = 0.4f }
    }

    private fun saveCategory() {
        val name = binding.etCategoryName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etCategoryName.error = "Enter a category name"
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            val result = FirebaseHelper.addCategory(
                Category(name = name, color = selectedColor)
            )
            result.onSuccess {
                onCategoryAdded()
                dismiss()
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}