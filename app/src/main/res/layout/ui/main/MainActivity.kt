package com.example.notesapp.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notesapp.R
import com.example.notesapp.model.Category
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val userName = FirebaseHelper.auth.currentUser?.displayName
            ?: FirebaseHelper.auth.currentUser?.email ?: "User"
        binding.tvWelcome.text = "Hello, ${userName.split(" ").first()} 👋"

        setupRecyclerView()

        binding.fabAddCategory.setOnClickListener {
            AddCategoryDialog(this) { loadCategories() }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            categories,
            onCategoryClick = { category ->
                val intent = Intent(this, NotesActivity::class.java).apply {
                    putExtra("categoryId", category.id)
                    putExtra("categoryName", category.name)
                    putExtra("categoryColor", category.color)
                }
                startActivity(intent)
            },
            onCategoryDelete = { category -> deleteCategory(category) }
        )
        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            val result = FirebaseHelper.getCategories()
            binding.progressBar.visibility = View.GONE
            result.onSuccess { list ->
                categories.clear()
                categories.addAll(list.sortedBy { it.createdAt })
                categoryAdapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            FirebaseHelper.deleteCategory(category.id).onSuccess {
                Toast.makeText(this@MainActivity, "${category.name} deleted", Toast.LENGTH_SHORT).show()
                loadCategories()
            }.onFailure {
                Toast.makeText(this@MainActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
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
                FirebaseHelper.auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}