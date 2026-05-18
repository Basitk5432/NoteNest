package com.example.notesapp.data


data class Category(
    val id: String = "",
    val name: String = "",
    val color: String = "#6750A4",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)