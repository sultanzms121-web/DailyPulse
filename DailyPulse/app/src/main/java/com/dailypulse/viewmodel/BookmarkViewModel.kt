package com.dailypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dailypulse.data.NewsRepository
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarkViewModel(private val repository: NewsRepository) : ViewModel() {

    /**
     * UPDATED: Named to 'bookmarks' to match the BookmarksScreen call.
     * Uses repository.getBookmarkedArticles() which handles the GROUP BY logic
     * so the user doesn't see duplicates in this list.
     */
    val bookmarks: StateFlow<List<NewsArticle>> = repository.getBookmarkedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * UPDATED: Uses global sync logic.
     * When a user removes a bookmark here, it instantly updates the icon
     * in the Home and Search feeds as well.
     */
    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    /**
     * NEW: Mark as Read.
     * Ensures that if a user reads a saved article, the "Read" checkmark
     * appears on this screen and all others.
     */
    fun onArticleClicked(article: NewsArticle) {
        viewModelScope.launch {
            repository.markAsRead(article.contentUrl)
        }
    }
}

class BookmarkViewModelFactory(private val repository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookmarkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookmarkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}