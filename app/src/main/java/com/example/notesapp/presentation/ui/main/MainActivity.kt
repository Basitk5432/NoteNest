package com.example.notesapp.presentation.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.example.notesapp.R
import com.example.notesapp.data.model.Category
import com.example.notesapp.databinding.ActivityMainBinding
import com.example.notesapp.presentation.adapter.CategoryAdapter
import com.example.notesapp.presentation.ui.auth.LoginActivity
import com.example.notesapp.presentation.ui.category.AddCategoryDialog
import com.example.notesapp.presentation.ui.notes.NotesActivity
import com.example.notesapp.presentation.viewmodel.ActionState
import com.example.notesapp.presentation.viewmodel.CategoryState
import com.example.notesapp.presentation.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.jvm.java

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
        setSupportActionBar(binding.toolbar)

        val userName = auth.currentUser?.displayName
            ?: auth.currentUser?.email ?: "User"
        binding.tvWelcome.text = "Hello, ${userName.split(" ").first()} 👋"

        setupRecyclerView()
        observeStates()

        binding.fabAddCategory.setOnClickListener {
            AddCategoryDialog(this) {
                viewModel.addCategory(it)
            }.show()
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
                viewModel.deleteCategory(category.id)
            }
        )
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeStates() {
        lifecycleScope.launch {
            viewModel.categoriesState.collect { state ->
                when (state) {
                    is CategoryState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is CategoryState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        categories.clear()
                        categories.addAll(state.categories)
                        categoryAdapter.notifyDataSetChanged()
                        binding.tvEmpty.visibility =
                            if (state.categories.isEmpty()) View.VISIBLE else View.GONE
                    }

                    is CategoryState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collect { state ->
                when (state) {
                    is ActionState.Success -> {
                        viewModel.getCategories()
                        viewModel.resetActionState()
                    }

                    is ActionState.Error -> {
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetActionState()
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}