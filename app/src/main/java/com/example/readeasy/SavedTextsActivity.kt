package com.example.readeasy

import android.os.Bundle
import java.util.Date
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.launch

class SavedTextsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_texts)

        val listView = findViewById<ListView>(R.id.savedTextsList)
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "saved_texts_db").build()

        lifecycleScope.launch {
            val savedTexts = db.savedTextDao().getAll()
            val items = savedTexts.map {
                val preview = it.content.take(40).replace("\n", " ")  // Preview first 40 chars
                "$preview... (${Date(it.timestamp)})"
            }

            runOnUiThread {
                val adapter = ArrayAdapter(this@SavedTextsActivity, android.R.layout.simple_list_item_1, items)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    Toast.makeText(this@SavedTextsActivity, savedTexts[position].content, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
