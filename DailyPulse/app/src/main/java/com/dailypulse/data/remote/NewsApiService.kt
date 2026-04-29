package com.dailypulse.data.remote

import com.dailypulse.model.NewsDataResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 🌟 NEWSDATA API CONTRACT
 * Interceptor in RetrofitClient handles the API Key automatically.
 */
interface NewsApiService {

    @GET("news")
    suspend fun getNews(
        @Query("q") query: String? = null,
        @Query("language") language: String? = null,
        @Query("category") category: String? = null,
        @Query("country") country: String? = null,
        @Query("page") page: String? = null,
        @Query("size") size: Int = 10
    ): NewsDataResponse

    // Optional: Only keep this if you want a distinct function for the Search Screen
    @GET("news")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("language") language: String? = null,
        @Query("page") page: String? = null
    ): NewsDataResponse
}