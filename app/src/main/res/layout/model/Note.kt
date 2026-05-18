package com.example.notesapp.data

data class Note(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)