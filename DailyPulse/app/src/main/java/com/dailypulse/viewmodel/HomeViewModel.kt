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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: NewsRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModel() {

    private val _isManualRefreshing = MutableStateFlow(false)
    val isManualRefreshing: StateFlow<Boolean> = _isManualRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    val isRefreshing: StateFlow<Boolean> = combine(_isManualRefreshing, _isLoadingMore) { manual, more ->
        manual || more
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _errorMessage = Channel<String>()
    val errorMessage = _errorMessage.receiveAsFlow()

    private val categoryPageTokens = mutableMapOf<String, String?>()

    private val _currentLanguage = MutableStateFlow(
        sharedPrefs.getString(Constants.KEY_NEWS_LANGUAGE, "en") ?: "en"
    )

    val selectedLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    val bookmarkedArticles: StateFlow<List<NewsArticle>> = repository.getBookmarkedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 🌟 THE STORIES FLOW
     * Collects latest headlines from followed sources for the Story Bar.
     */
    val followedStories: StateFlow<List<NewsArticle>> = repository.getFollowedStories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == Constants.KEY_NEWS_LANGUAGE) {
            val newLang = prefs.getString(Constants.KEY_NEWS_LANGUAGE, "en") ?: "en"
            _currentLanguage.value = newLang
            categoryPageTokens.clear()
            refreshNewsFromApi("All")
        }
    }

    init {
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
        refreshNewsFromApi("All")
    }

    /**
     * 🌟 FOLLOW LOGIC
     */
    fun followSource(sourceId: String, sourceName: String) {
        viewModelScope.launch {
            repository.followSource(sourceId, sourceName)
        }
    }

    fun unfollowSource(sourceId: String) {
        viewModelScope.launch {
            repository.unfollowSource(sourceId)
        }
    }

    /**
     * Returns a flow checking if a specific source is followed.
     */
    fun isFollowing(sourceId: String): Flow<Boolean> = repository.isFollowing(sourceId)

    /**
     * 🌟 NEW: SOURCE PROFILE DATA
     * Fetches articles belonging to a specific source (e.g., Rtv, BBC).
     */
    fun getArticlesBySource(sourceId: String): Flow<List<NewsArticle>> =
        repository.getArticlesBySource(sourceId)

    /**
     * 🌟 PRIORITIZED FEED
     */
    fun getArticles(category: String, language: String): Flow<List<NewsArticle>> {
        val cleanLanguage = language.take(2).lowercase()
        return repository.getArticles(category, cleanLanguage)
    }

    fun getBookmarkedArticles(): Flow<List<NewsArticle>> {
        return repository.getBookmarkedArticles()
    }

    fun manualRefresh(category: String) {
        if (_isManualRefreshing.value) return
        viewModelScope.launch {
            _isManualRefreshing.value = true
            categoryPageTokens[category] = null
            val newToken = refreshNewsInternal(category, null)
            categoryPageTokens[category] = newToken
            _isManualRefreshing.value = false
        }
    }

    fun refreshNewsFromApi(category: String) {
        if (_isManualRefreshing.value) return
        viewModelScope.launch {
            _isManualRefreshing.value = true
            categoryPageTokens[category] = null
            val newToken = refreshNewsInternal(category, null)
            categoryPageTokens[category] = newToken
            _isManualRefreshing.value = false
        }
    }

    fun loadNextPage(category: String) {
        val currentToken = categoryPageTokens[category]
        if (_isLoadingMore.value || currentToken == null) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            delay(500)
            val newToken = refreshNewsInternal(category, currentToken)
            categoryPageTokens[category] = newToken
            _isLoadingMore.value = false
        }
    }

    private suspend fun refreshNewsInternal(category: String, token: String?): String? {
        return try {
            repository.refreshNews(
                category = category,
                language = _currentLanguage.value,
                page = token
            )
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Refresh Failed: ${e.message}")
            _errorMessage.trySend("Failed to load news. Please check your connection.")
            null
        }
    }

    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch { repository.toggleBookmark(article) }
    }

    fun onArticleClicked(url: String) {
        viewModelScope.launch { repository.markAsRead(url) }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}

class HomeViewModelFactory(
    private val repository: NewsRepository,
    private val sharedPrefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, sharedPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}