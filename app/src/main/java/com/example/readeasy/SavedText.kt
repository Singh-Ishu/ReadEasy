package com.example.readeasy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_texts")
data class SavedText(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
