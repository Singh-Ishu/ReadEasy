package com.example.readeasy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*
import com.example.readeasy.SavedText

@Dao
interface SavedTextDao {
    @Query("SELECT * FROM saved_texts ORDER BY id DESC")
    suspend fun getAll(): List<SavedText>

    @Insert
    suspend fun insert(text: SavedText)

    @Delete
    suspend fun delete(text: SavedText)

    @Query("DELETE FROM saved_texts")
    suspend fun deleteAll()
}
