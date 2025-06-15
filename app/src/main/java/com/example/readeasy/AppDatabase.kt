package com.example.readeasy

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedText::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun savedTextDao(): SavedTextDao
}
