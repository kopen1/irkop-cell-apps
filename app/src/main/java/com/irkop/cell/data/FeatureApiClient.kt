package com.irkop.cell.data

import com.irkop.cell.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class FeatureApiClient {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }
    val api: FeatureApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.ensureSlash())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FeatureApiService::class.java)

    private fun String.ensureSlash() = if (endsWith("/")) this else "$this/"
}
