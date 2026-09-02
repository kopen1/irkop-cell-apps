package com.irkop.cell.core

import com.irkop.cell.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class ApiClient(private val session: SessionManager) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .apply {
                session.token?.let { header("Authorization", "Bearer $it") }
            }
            .build()
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.ensureTrailingSlash())
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ApiService::class.java)

    private fun String.ensureTrailingSlash() =
        if (endsWith("/")) this else "$this/"
}
