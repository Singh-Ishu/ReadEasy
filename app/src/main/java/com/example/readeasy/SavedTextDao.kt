package com.example.readeasy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SavedTextDao {
    @Insert
    suspend fun insert(text: SavedText)

    @Query("SELECT * FROM SavedText ORDER BY timestamp DESC")
    suspend fun getAll(): List<SavedText>
}
