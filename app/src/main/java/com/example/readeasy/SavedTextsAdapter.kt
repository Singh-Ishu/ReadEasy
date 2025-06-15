package com.example.readeasy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class SavedTextsAdapter(
    private val texts: List<SavedText>,
    private val onDelete: (SavedText) -> Unit,
    private val onOpen: (SavedText) -> Unit
) : RecyclerView.Adapter<SavedTextsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentText: TextView = view.findViewById(R.id.contentText)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        val openButton: Button = view.findViewById(R.id.openButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_text_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedText = texts[position]
        val preview = savedText.content.take(40).replace("\n", " ")
        holder.contentText.text = "$preview... (${Date(savedText.timestamp)})"

        holder.deleteButton.setOnClickListener { onDelete(savedText) }
        holder.openButton.setOnClickListener { onOpen(savedText) }
    }

    override fun getItemCount() = texts.size
}
