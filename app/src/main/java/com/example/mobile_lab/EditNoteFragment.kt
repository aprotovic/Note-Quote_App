package com.example.mobile_lab

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mobile_lab.databinding.FragmentEditNoteBinding
import com.example.mobile_lab.model.Note
import com.example.mobile_lab.viewmodel.NotesViewModel

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private var noteId: Int = -1
    private var selectedColor = "#FFFFFF"
    private var editingNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        // Retrieve arguments
        arguments?.let {
            noteId = it.getInt("noteId", -1)
        }

        setupEditor()
        setupColorSelectors()
        setupActionListeners()
    }

    private fun setupEditor() {
        if (noteId == -1) {
            // Screen state: Create New Note
            binding.tvEditorHeader.text = "New Note"
            binding.btnDelete.visibility = View.GONE
            selectedColor = "#FFFFFF"
            binding.editorCard.setCardBackgroundColor(Color.WHITE)
        } else {
            // Screen state: Edit Existing Note
            binding.tvEditorHeader.text = "Edit Note"
            binding.btnDelete.visibility = View.VISIBLE
            
            editingNote = viewModel.getNoteById(noteId)
            editingNote?.let { note ->
                binding.etNoteTitle.setText(note.title)
                binding.etNoteContent.setText(note.content)
                selectedColor = note.colorHex
                
                try {
                    binding.editorCard.setCardBackgroundColor(Color.parseColor(selectedColor))
                } catch (e: Exception) {
                    binding.editorCard.setCardBackgroundColor(Color.WHITE)
                }
            }
        }
    }

    private fun setupColorSelectors() {
        val colorsMap = mapOf(
            binding.colorWhite to "#FFFFFF",
            binding.colorPeach to "#FFE3D8",
            binding.colorYellow to "#FFF7C2",
            binding.colorGreen to "#D4F0D5",
            binding.colorBlue to "#D3E9FA",
            binding.colorPurple to "#EAD6FD"
        )

        for ((view, hex) in colorsMap) {
            view.setOnClickListener {
                selectedColor = hex
                try {
                    // Instantly tints the canvas card for visual excellence!
                    binding.editorCard.setCardBackgroundColor(Color.parseColor(hex))
                } catch (e: Exception) {
                    binding.editorCard.setCardBackgroundColor(Color.WHITE)
                }
            }
        }
    }

    private fun setupActionListeners() {
        // Back Navigation chevron click
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Delete button click
        binding.btnDelete.setOnClickListener {
            editingNote?.let { note ->
                viewModel.deleteNote(note)
                Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        // Save note checkmark click
        binding.btnSave.setOnClickListener {
            val title = binding.etNoteTitle.text.toString().trim()
            val content = binding.etNoteContent.text.toString().trim()

            if (title.isEmpty()) {
                binding.etNoteTitle.error = "Title required"
                return@setOnClickListener
            }

            if (noteId == -1) {
                // CRUD: Create note
                viewModel.insertNote(title, content, selectedColor)
                Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show()
            } else {
                // CRUD: Update note
                viewModel.updateNote(noteId, title, content, selectedColor)
                Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show()
            }

            // Close screen and return to notes list grid
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear memory reference
    }
}
