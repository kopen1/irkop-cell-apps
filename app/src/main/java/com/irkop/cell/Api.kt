package com.irkop.cell

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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
        set(value) {
            if (value.isNullOrBlank()) prefs.edit().remove("token").apply()
            else prefs.edit().putString("token", value).apply()
        }

    fun hasToken(): Boolean = !token.isNullOrBlank()
    fun cachedUser(): JSONObject? = prefs.getString("user", null)?.let { runCatching { JSONObject(it) }.getOrNull() }
    fun clearToken() { prefs.edit().remove("token").remove("user").apply() }

    suspend fun login(username: String, password: String): JSONObject = request(
        "POST", "/api/auth/login", JSONObject().put("username", username).put("password", password), false, emptyMap(), false
    ).also { result ->
        token = result.optString("token")
        result.optJSONObject("user")?.let { prefs.edit().putString("user", it.toString()).apply() }
    }

    suspend fun me(): JSONObject = get("/api/auth/me")

    suspend fun logout() {
        runCatching { request("POST", "/api/auth/logout", null, true, emptyMap(), false) }
        clearToken()
    }

    suspend fun get(path: String, params: Map<String, String> = emptyMap()): JSONObject =
        request("GET", path, null, true, params, false)

    suspend fun post(path: String, body: JSONObject = JSONObject(), financial: Boolean = false): JSONObject =
        request("POST", path, body, true, emptyMap(), financial)

    suspend fun put(path: String, body: JSONObject = JSONObject(), financial: Boolean = false): JSONObject =
        request("PUT", path, body, true, emptyMap(), financial)

    suspend fun delete(path: String, body: JSONObject? = null, financial: Boolean = true): JSONObject =
        request("DELETE", path, body, true, emptyMap(), financial)

    suspend fun kasirCurrent(): JSONObject = get("/api/kasir/current")
    suspend fun kasirReminder(): JSONObject = get("/api/kasir/reminder-closing")
    suspend fun kasirOpening(saldoAwal: JSONArray): JSONObject = post(
        "/api/kasir/opening", JSONObject().put("saldo_awal", saldoAwal), true
    )
    suspend fun kasirClosing(saldoReal: JSONArray, catatan: String? = null): JSONObject = post(
        "/api/kasir/closing", JSONObject().put("saldo_real", saldoReal).apply {
            if (!catatan.isNullOrBlank()) put("catatan_closing", catatan)
        }, true
    )

    suspend fun transaksi(params: Map<String, String> = emptyMap()): JSONObject = get("/api/transaksi", params)
    suspend fun createTransaksi(body: JSONObject): JSONObject = post("/api/transaksi", body, true)
    suspend fun updateTransaksi(id: String, body: JSONObject): JSONObject = put("/api/transaksi/$id", body, true)
    suspend fun deleteTransaksi(id: String, reason: String? = null): JSONObject = delete(
        "/api/transaksi/$id", JSONObject().apply { if (!reason.isNullOrBlank()) put("deleted_reason", reason) }, true
    )

    suspend fun laporanBulan(bulan: String): JSONObject = get("/api/laporan/bulan", mapOf("bulan" to bulan))
    suspend fun laporanTahun(tahun: String): JSONObject = get("/api/laporan/tahun", mapOf("tahun" to tahun))
    suspend fun laporanExport(cakupan: String, bulan: String? = null, tahun: String? = null): JSONObject = get(
        "/api/laporan/export", buildMap {
            put("cakupan", cakupan)
            if (!bulan.isNullOrBlank()) put("bulan", bulan)
            if (!tahun.isNullOrBlank()) put("tahun", tahun)
        }
    )

    suspend fun kasbon(params: Map<String, String> = emptyMap()): JSONObject = get("/api/kasbon", params)
    suspend fun createKasbon(body: JSONObject): JSONObject = post("/api/kasbon", body, true)
    suspend fun updateKasbon(id: String, body: JSONObject): JSONObject = put("/api/kasbon/$id", body, true)

    suspend fun gaji(params: Map<String, String> = emptyMap()): JSONObject = get("/api/gaji", params)
    suspend fun createGaji(body: JSONObject): JSONObject = post("/api/gaji", body, true)
    suspend fun updateGaji(id: String, body: JSONObject): JSONObject = put("/api/gaji/$id", body, true)
    suspend fun gajiRates(): JSONObject = get("/api/gaji/rate")
    suspend fun createGajiRate(body: JSONObject): JSONObject = post("/api/gaji/rate", body, true)

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject?,
        auth: Boolean,
        params: Map<String, String>,
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
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
            if (financial) setRequestProperty("Idempotency-Key", "ir-${UUID.randomUUID()}")
        }
        try {
            if (body != null) connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val result = runCatching { JSONObject(text) }.getOrElse { JSONObject().put("raw", text) }
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
