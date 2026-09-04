package com.irkop.cell

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class ApiException(val status: Int, val code: String, override val message: String) : Exception(message)

class ApiClient(context: Context) {
    private val prefs = context.getSharedPreferences("irkop_cell", Context.MODE_PRIVATE)
    private val base = "https://konter.irkop.workers.dev"

    private var token: String?
        get() = prefs.getString("token", null)
        set(value) { prefs.edit().putString("token", value).apply() }

    fun hasToken(): Boolean = !token.isNullOrBlank()
    fun clearToken() { prefs.edit().remove("token").remove("user").apply() }

    suspend fun login(username: String, password: String): JSONObject {
        val result = request(
            method = "POST",
            path = "/api/auth/login",
            body = JSONObject().put("username", username).put("password", password),
            auth = false,
            financial = false
        )
        token = result.optString("token").takeIf { it.isNotBlank() }
        prefs.edit().putString("user", result.optJSONObject("user")?.toString()).apply()
        return result
    }

    suspend fun logout() {
        runCatching { request("POST", "/api/auth/logout", null, true, emptyMap(), false) }
        clearToken()
    }

    suspend fun get(path: String, params: Map<String, String> = emptyMap()): JSONObject =
        request("GET", path, null, true, params, false)

    suspend fun post(path: String, body: JSONObject, financial: Boolean = false): JSONObject =
        request("POST", path, body, true, emptyMap(), financial)

    suspend fun put(path: String, body: JSONObject, financial: Boolean = false): JSONObject =
        request("PUT", path, body, true, emptyMap(), financial)

    suspend fun delete(path: String, body: JSONObject? = null, financial: Boolean = true): JSONObject =
        request("DELETE", path, body, true, emptyMap(), financial)

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject?,
        auth: Boolean,
        params: Map<String, String> = emptyMap(),
        financial: Boolean
    ): JSONObject = withContext(Dispatchers.IO) {
        val query = params.filterValues { it.isNotBlank() }.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val url = URL(base + path + if (query.isBlank()) "" else "?$query")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("Accept", "application/json")
            if (auth && !token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            if (financial) setRequestProperty("Idempotency-Key", "ir-${UUID.randomUUID()}")
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val result = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (status !in 200..299) {
                val error = result.optJSONObject("error") ?: JSONObject()
                if (status == 401) clearToken()
                throw ApiException(status, error.optString("code", "http_$status"), error.optString("message", "Terjadi kesalahan pada server."))
            }
            result
        } finally {
            connection.disconnect()
        }
    }
}
