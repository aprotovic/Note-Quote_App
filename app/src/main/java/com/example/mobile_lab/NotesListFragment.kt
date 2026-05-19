package com.example.mobile_lab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mobile_lab.adapter.NoteAdapter
import com.example.mobile_lab.databinding.FragmentNotesListBinding
import com.example.mobile_lab.viewmodel.NotesViewModel
import com.example.mobile_lab.viewmodel.QuoteState

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Activity-scoped ViewModel to share database and quote state completely
        viewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        setupRecyclerView()
        setupQuoteBanner()
        setupSearchBar()
        observeNotes()

        // FAB navigates to Editor screen for creating a new note (noteId = -1)
        binding.fabAddNote.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("noteId", -1)
            }
            findNavController().navigate(
                R.id.action_notesListFragment_to_editNoteFragment,
                bundle
            )
        }
    }

    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note ->
                // Card click navigates to Editor screen to edit existing note
                val bundle = Bundle().apply {
                    putInt("noteId", note.id)
                }
                findNavController().navigate(
                    R.id.action_notesListFragment_to_editNoteFragment,
                    bundle
                )
            },
            onNoteDelete = { note ->
                viewModel.deleteNote(note)
                Toast.makeText(requireContext(), "Note deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvNotes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rvNotes.adapter = adapter
    }

    private fun setupQuoteBanner() {
        // Refresh quotes
        binding.btnRefreshQuote.setOnClickListener {
            viewModel.fetchDailyQuote()
        }

        // Live quote states
        viewModel.quoteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuoteState.Loading -> {
                    binding.btnRefreshQuote.visibility = View.GONE
                    binding.pbQuoteLoading.visibility = View.VISIBLE
                    binding.tvQuoteText.text = "\"Syncing with the universe for an inspiring quote...\""
                    binding.tvQuoteAuthor.text = "- Syncing"
                }
                is QuoteState.Success -> {
                    binding.pbQuoteLoading.visibility = View.GONE
                    binding.btnRefreshQuote.visibility = View.VISIBLE
                    binding.tvQuoteText.text = "\"${state.quote}\""
                    binding.tvQuoteAuthor.text = "- ${state.author}"
                }
                is QuoteState.Error -> {
                    binding.pbQuoteLoading.visibility = View.GONE
                    binding.btnRefreshQuote.visibility = View.VISIBLE
                    binding.tvQuoteText.text = "\"Believe you can and you're halfway there.\""
                    binding.tvQuoteAuthor.text = "- Theodore Roosevelt (Offline)"
                    Toast.makeText(requireContext(), "API Offline: Loaded offline quote.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeNotes() {
        viewModel.filteredNotes.observe(viewLifecycleOwner) { notesList ->
            adapter.submitList(notesList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
