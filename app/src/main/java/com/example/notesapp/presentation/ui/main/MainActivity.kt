package com.example.notesapp.presentation.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.example.notesapp.data.model.Category
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.presentation.adapter.CategoryAdapter
import com.example.notesapp.presentation.ui.archive.ArchiveActivity
import com.example.notesapp.presentation.ui.auth.LoginActivity
import com.example.notesapp.presentation.ui.category.AddCategoryDialog
import com.example.notesapp.presentation.ui.notes.NotesActivity
import com.example.notesapp.presentation.viewmodel.ActionState
import com.example.notesapp.presentation.viewmodel.CategoryState
import com.example.notesapp.presentation.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CategoryViewModel by viewModel()
    private val auth: FirebaseAuth by inject()
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = auth.currentUser?.displayName
            ?: auth.currentUser?.email ?: "User"
        binding.tvWelcome.text = "Hello, ${userName.split(" ").first()} 👋"

        setupRecyclerView()
        observeStates()

        // Logout
        binding.ivLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }


        // Add category
        binding.fabAddCategory.setOnClickListener {
            AddCategoryDialog(this) {
                viewModel.addCategory(it)
            }.show()
        }

        // Archive screen
        binding.ivArchive.setOnClickListener {
            startActivity(Intent(this, ArchiveActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories,
            onCategoryClick = { category ->
                startActivity(
                    Intent(this, NotesActivity::class.java).apply {
                        putExtra("categoryId", category.id)
                        putExtra("categoryName", category.name)
                        putExtra("categoryColor", category.color)
                    }
                )
            },
            onCategoryDelete = { category ->
                android.app.AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Delete \"${category.name}\" and all notes?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteCategory(category.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onCategoryArchive = { category ->
                android.app.AlertDialog.Builder(this)
                    .setTitle("Archive")
                    .setMessage("Archive \"${category.name}\"?")
                    .setPositiveButton("Archive") { _, _ ->
                        viewModel.archiveCategory(category.id)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onCategoryRename = { category ->
                showRenameDialog(category)
            }
        )
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
    }

    private fun showRenameDialog(category: Category) {
        val editText = EditText(this).apply {
            setText(category.name)
            selectAll()
            setPadding(48, 32, 48, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename Category")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (newName == category.name) {
                    Toast.makeText(this, "Name is the same", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.renameCategory(category.name, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeStates() {
        lifecycleScope.launch {
            viewModel.categoriesState.collect { state ->
                when (state) {
                    is CategoryState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is CategoryState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        categories.clear()
                        categories.addAll(
                            state.categories.sortedByDescending { it.createdAt }
                        )
                        categoryAdapter.notifyDataSetChanged()
                        binding.tvEmpty.visibility =
                            if (state.categories.isEmpty()) View.VISIBLE else View.GONE
                    }
                    is CategoryState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@MainActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is ActionState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ActionState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        // Force refresh categories
                        viewModel.getCategories()
                        viewModel.resetActionState()
                    }
                    is ActionState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@MainActivity,
                            state.message,
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetActionState()
                    }
                    else -> {}
                }
            }
        }
    }
}