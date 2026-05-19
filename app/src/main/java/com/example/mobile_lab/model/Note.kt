package com.example.mobile_lab.model

data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: String = "#FFFFFF"
)
