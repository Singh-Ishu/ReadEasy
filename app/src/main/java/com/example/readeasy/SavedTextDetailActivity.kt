package com.example.readeasy

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SavedTextDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_text_detail)

        val fullTextView = findViewById<TextView>(R.id.fullTextView)
        val toggleButton = findViewById<Button>(R.id.readabilityToggleButton)
        val textContent = intent.getStringExtra("textContent")
        fullTextView.text = textContent

        var isReadable = false
        toggleButton.setOnClickListener {
            isReadable = !isReadable
            fullTextView.textSize = if (isReadable) 24f else 18f
            fullTextView.setLineSpacing(if (isReadable) 12f else 0f, 1.2f)
        }
    }
}
