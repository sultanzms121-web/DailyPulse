package com.dailypulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailypulse.data.NewsRepository
import com.dailypulse.model.NewsArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleDetailViewModel(private val repository: NewsRepository) : ViewModel() {

    private val _displaySummary = MutableStateFlow("")
    val displaySummary: StateFlow<String> = _displaySummary.asStateFlow()

    private val _displayImageUrl = MutableStateFlow("")
    val displayImageUrl: StateFlow<String> = _displayImageUrl.asStateFlow()

    /**
     * Loads the AI content using a "Parallel Fast-First" approach.
     * 1. Updates UI immediately with existing data or placeholders.
     * 2. Launches AI Summary and AI Image tasks in parallel for speed.
     */
    fun loadArticleContent(article: NewsArticle, languageCode: String) {
        viewModelScope.launch {

            // --- 1. IMMEDIATE UI UPDATE (Fast-First) ---

            // Show cached AI summary if available, otherwise original summary.
            // If both are blank (common in International news), show a professional placeholder.
            _displaySummary.value = article.aiSummary ?: article.summary.ifBlank {
                "Analyzing article for geopolitical insights..."
            }

            // Show original image immediately if it exists.
            _displayImageUrl.value = article.imageUrl ?: ""

            // --- 2. BACKGROUND ENRICHMENT (Parallel AI Tasks) ---

            // Launch AI Summary Enrichment
            launch {
                try {
                    val finalSummary = repository.getEnrichedAiSummary(article, languageCode)
                    if (finalSummary.isNotBlank()) {
                        _displaySummary.value = finalSummary
                    }
                } catch (e: Exception) {
                    // Fail silently: keep the original summary/placeholder
                }
            }

            // Launch AI Image Enrichment
            launch {
                try {
                    val finalImageUrl = repository.getEnrichedImageUrl(article)
                    if (finalImageUrl.isNotBlank()) {
                        _displayImageUrl.value = finalImageUrl
                    }
                } catch (e: Exception) {
                    // Fail silently: keep the original image/error placeholder
                }
            }
        }
    }
}