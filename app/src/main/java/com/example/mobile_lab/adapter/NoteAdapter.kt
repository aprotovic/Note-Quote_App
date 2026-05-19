package com.example.mobile_lab.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_lab.databinding.NoteItemBinding
import com.example.mobile_lab.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: NoteItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteContent.text = note.content

            // Format date
            val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
            binding.tvNoteTime.text = sdf.format(Date(note.timestamp))

            // Safely parse background color with fallback to white
            try {
                (binding.root as CardView).setCardBackgroundColor(Color.parseColor(note.colorHex))
            } catch (e: Exception) {
                (binding.root as CardView).setCardBackgroundColor(Color.WHITE)
            }

            // Click callbacks
            binding.root.setOnClickListener {
                onNoteClick(note)
            }

            binding.btnDeleteNote.setOnClickListener {
                onNoteDelete(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
