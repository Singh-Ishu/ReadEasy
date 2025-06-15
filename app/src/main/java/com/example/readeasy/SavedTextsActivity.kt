package com.example.readeasy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.launch

class SavedTextsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_texts)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "saved_texts_db").build()
        recyclerView = findViewById(R.id.savedTextsList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadSavedTexts()
    }

    private fun loadSavedTexts() {
        lifecycleScope.launch {
            val savedTexts = db.savedTextDao().getAll()

            runOnUiThread {
                val adapter = SavedTextsAdapter(
                    savedTexts,
                    onDelete = { text ->
                        lifecycleScope.launch {
                            db.savedTextDao().delete(text)
                            loadSavedTexts() // refresh list after deletion
                        }
                    },
                    onOpen = { text ->
                        val intent = Intent(this@SavedTextsActivity, SavedTextDetailActivity::class.java)
                        intent.putExtra("textContent", text.content)
                        startActivity(intent)
                    }
                )
                recyclerView.adapter = adapter
            }
        }
    }
}
