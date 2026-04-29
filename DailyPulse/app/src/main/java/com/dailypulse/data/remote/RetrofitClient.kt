package com.dailypulse.data.remote

import com.dailypulse.Constants
import com.dailypulse.BuildConfig // 🌟 Added for safety
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 🌟 THE NETWORK ENGINE
 * This handles all outgoing traffic to NewsData.io.
 */
object RetrofitClient {
    // 🔗 It's best practice to pull this from your Constants file
    private const val BASE_URL = "https://newsdata.io/api/1/"

    // 1. THE LOGGER (Debug Only)
    // We only log the body in Debug mode to protect user privacy and performance.
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    // 2. THE AUTH INTERCEPTOR
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // NewsData.io expects the parameter name "apikey" (all lowercase)
        val newUrl = originalRequest.url.newBuilder()
            .addQueryParameter("apikey", Constants.NEWS_API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        chain.proceed(newRequest)
    }

    // 3. THE CLIENT
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(authInterceptor)
        // 20s is perfect for 2026 mobile networks
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // 4. THE SERVICE GATEWAY
    val newsApiService: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NewsApiService::class.java)
    }
}