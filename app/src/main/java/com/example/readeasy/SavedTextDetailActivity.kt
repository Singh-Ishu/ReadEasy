package com.example.readeasy

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SavedTextDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_text_detail)

        val fullTextView = findViewById<TextView>(R.id.fullTextView)
        val textContent = intent.getStringExtra("textContent")
        fullTextView.text = textContent
    }
}
