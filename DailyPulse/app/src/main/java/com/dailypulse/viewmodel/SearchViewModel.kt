package com.dailypulse.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailypulse.Constants
import com.dailypulse.data.NewsRepository
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(
    private val repository: NewsRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = Channel<String>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentLanguage = MutableStateFlow(
        sharedPrefs.getString(Constants.KEY_NEWS_LANGUAGE, "en") ?: "en"
    )

    private var nextPageToken: String? = null

    /**
     * 🌟 THE REACTIVE ENGINE
     * Fetches articles matching the query.
     */
    val searchResults: StateFlow<List<NewsArticle>> = combine(_searchQuery, _currentLanguage) { query, lang ->
        Pair(query, lang)
    }
        .debounce(600)
        .flatMapLatest { (query, lang) ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                val cleanLang = lang.lowercase().take(2)
                performSearchRefresh(query, lang)
                repository.getArticles("Search", cleanLang)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 🌟 THE PAGE MATCHING LOGIC (New!)
     * This Flow looks through the search results to see if any article's 'sourceName'
     * exactly matches the user's search query. If it does, we treat it as a "News Page" result.
     */
    val matchedSource: StateFlow<NewsArticle?> = combine(_searchQuery, searchResults) { query, articles ->
        if (query.length < 2) null
        else articles.find { it.sourceName.equals(query.trim(), ignoreCase = true) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == Constants.KEY_NEWS_LANGUAGE) {
            _currentLanguage.value = prefs.getString(Constants.KEY_NEWS_LANGUAGE, "en") ?: "en"
        }
    }

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isBlank()) {
            nextPageToken = null
            viewModelScope.launch { repository.clearSearch() }
        }
    }

    private suspend fun performSearchRefresh(query: String, lang: String) {
        if (query.isBlank()) return
        _isLoading.value = true
        try {
            nextPageToken = repository.refreshSearchNews(query, lang)
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Search API failed: ${e.message}")
            _errorMessage.trySend("Search results may be outdated.")
        } finally {
            _isLoading.value = false
        }
    }

    fun loadNextSearchPage() {
        val query = _searchQuery.value
        if (_isLoading.value || nextPageToken == null || query.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                nextPageToken = repository.refreshSearchNews(
                    query = query,
                    language = _currentLanguage.value,
                    page = nextPageToken
                )
            } catch (e: Exception) {
                _errorMessage.trySend("Could not load more results.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(article: NewsArticle) = viewModelScope.launch {
        repository.toggleBookmark(article)
    }

    fun onArticleClicked(url: String) = viewModelScope.launch {
        repository.markAsRead(url)
    }

    override fun onCleared() {
        super.onCleared()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}

class SearchViewModelFactory(
    private val repository: NewsRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}