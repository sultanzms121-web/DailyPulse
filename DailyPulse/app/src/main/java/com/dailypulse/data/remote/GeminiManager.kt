package com.dailypulse.data.remote

import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.generationConfig

/**
 * 🌟 AI MANAGER (2026 Standard)
 * Handles news summarization and visual prompt generation using Gemini 3 Flash.
 * Securely managed via Firebase Vertex AI.
 */
class GeminiManager {

    // 1. Setup the Config (Balanced for creativity and factual accuracy)
    private val config = generationConfig {
        temperature = 0.5f
        topK = 40
        topP = 0.95f
    }

    // 2. Initialize the Model
    // No apiKey here—it's automatically read from your google-services.json
    private val model = Firebase.vertexAI.generativeModel(
        modelName = "gemini-3-flash",
        generationConfig = config
    )

    /**
     * 🌟 ENHANCED SUMMARY LOGIC
     * Generates a 3-bullet point summary with special context for Geopolitics.
     */
    suspend fun generateSummary(
        title: String,
        description: String,
        language: String,
        category: String? // Nullable to match article data
    ): String? {

        // Custom instruction for your International Affairs focus
        val extraInstruction = if (category == "International Affairs") {
            "Specifically focus on the geopolitical impact and global significance of this news."
        } else ""

        val prompt = """
            Summarize the following news article into exactly 3 bullet points.
            The entire summary MUST be written in $language.
            $extraInstruction
            Do not include any intro like "Here is the summary".
            
            Title: $title
            Content: $description
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 🌟 VISUAL PROMPT LOGIC
     * Strictly sanitizes titles for Pollinations.ai URL compatibility.
     * This fixes the "Unresolved reference" error in your Repositories.kt.
     */
    suspend fun generateVisualDescription(title: String): String {
        val prompt = """
            Describe a simple cinematic photo for this news: "$title". 
            USE ONLY SIMPLE WORDS. NO punctuation. NO quotes. NO symbols. 
            Max 8 words.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            val rawText = response.text ?: ""

            // Remove non-alphanumeric characters
            val sanitized = rawText.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()

            // Replace spaces with '+' for URL safety
            if (sanitized.isNotBlank()) {
                sanitized.replace(" ", "+")
            } else {
                "news+concept+cinematic"
            }
        } catch (e: Exception) {
            "global+news+report"
        }
    }
}