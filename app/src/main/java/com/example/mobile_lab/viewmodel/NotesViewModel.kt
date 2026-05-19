package com.example.mobile_lab.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobile_lab.database.NotesDatabaseHelper
import com.example.mobile_lab.model.Note
import com.example.mobile_lab.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class QuoteState {
    object Loading : QuoteState()
    data class Success(val quote: String, val author: String) : QuoteState()
    data class Error(val message: String) : QuoteState()
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = NotesDatabaseHelper(application)

    // Remote Quote UI State
    private val _quoteState = MutableLiveData<QuoteState>(QuoteState.Loading)
    val quoteState: LiveData<QuoteState> get() = _quoteState

    // Search Query State
    private var currentSearchQuery: String = ""

    // Full Notes List Cache
    private var allNotesList: List<Note> = emptyList()

    // Observable Filtered Notes
    private val _filteredNotes = MutableLiveData<List<Note>>(emptyList())
    val filteredNotes: LiveData<List<Note>> get() = _filteredNotes

    init {
        loadNotes()
        fetchDailyQuote()
    }

    // SQLite DB CRUD: Read all notes on IO thread
    fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            allNotesList = dbHelper.getAllNotes()
            applyFilter()
        }
    }

    // Helper to retrieve single note from memory cache
    fun getNoteById(id: Int): Note? {
        return allNotesList.find { it.id == id }
    }

    // Retrofit call to fetch daily quote
    fun fetchDailyQuote() {
        _quoteState.value = QuoteState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.quoteApiService.getRandomQuote()
                val content = response.content ?: "No content available."
                val author = response.author ?: "Unknown"
                _quoteState.postValue(QuoteState.Success(content, author))
            } catch (e: Exception) {
                _quoteState.postValue(QuoteState.Error(e.localizedMessage ?: "Failed to retrieve quote."))
            }
        }
    }

    // SQLite DB CRUD: Create
    fun insertNote(title: String, content: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = Note(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                colorHex = colorHex
            )
            dbHelper.insertNote(note)
            loadNotes() // Refresh notes list
        }
    }

    // SQLite DB CRUD: Update
    fun updateNote(id: Int, title: String, content: String, colorHex: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = Note(
                id = id,
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                colorHex = colorHex
            )
            dbHelper.updateNote(note)
            loadNotes() // Refresh notes list
        }
    }

    // SQLite DB CRUD: Delete
    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteNote(note.id)
            loadNotes() // Refresh notes list
        }
    }

    // Update Search Query and Filter
    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = currentSearchQuery
        if (query.isBlank()) {
            _filteredNotes.postValue(allNotesList)
        } else {
            val filtered = allNotesList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
            _filteredNotes.postValue(filtered)
        }
    }
}
