package com.irkop.cell.data

import com.irkop.cell.core.UserSession
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class Repository(
    private val api: ApiService
) {

    // ========================================================
    // AUTH
    // ========================================================

    suspend fun login(
        username: String,
        password: String
    ): Pair<String, UserSession> {
        val result = api.login(
            buildJsonObject {
                put("username", username)
                put("password", password)
            }
        )

        val token = result.string("token")
            ?: error(
                result.obj("error")
                    ?.string("message")
                    ?: "Login gagal"
            )

        val userObj = result.obj("user")
            ?: error("Data user tidak tersedia")

        return token to userObj.toUserSession()
    }

    suspend fun me(): UserSession {
        val result = api.me()
        val userObj = result.obj("user") ?: result
        return userObj.toUserSession()
    }

    suspend fun logout() =
        runCatching { api.logout() }

    // ========================================================
    // KASIR
    // ========================================================

    suspend fun kasirCurrent() =
        api.kasirCurrent()

    suspend fun reminderClosing() =
        api.reminderClosing()

    suspend fun opening(
        accounts: List<Pair<String, Long>>
    ) =
        api.kasirOpening(
            buildJsonObject {
                putJsonArray("saldo_awal") {
                    accounts.forEach { (name, amount) ->
                        add(
                            buildJsonObject {
                                put("nama_akun", name)
                                put("saldo", amount)
                            }
                        )
                    }
                }
            }
        )

    suspend fun closing(
        accounts: List<Pair<String, Long>>,
        note: String?
    ) =
        api.kasirClosing(
            buildJsonObject {
                putJsonArray("saldo_real") {
                    accounts.forEach { (name, amount) ->
                        add(
                            buildJsonObject {
                                put("nama_akun", name)
                                put("saldo_real", amount)
                            }
                        )
                    }

                    note
                        ?.takeIf { it.isNotBlank() }
                        ?.let { put("catatan_closing", it) }
                }
            }
        )

    // ========================================================
    // TRANSAKSI
    // ========================================================

    suspend fun transaksi(
        q: String? = null,
        tanggal: String? = null,
        tanggalMulai: String? = null,
        tanggalSelesai: String? = null,
        metodeBayar: String? = null,
        statusKonfirmasi: String? = null
    ) =
        api.transaksi(
            q = q,
            tanggal = tanggal,
            tanggalMulai = tanggalMulai,
            tanggalSelesai = tanggalSelesai,
            metodeBayar = metodeBayar,
            statusKonfirmasi = statusKonfirmasi
        )

    suspend fun transaksiDetail(id: String) =
        api.transaksiDetail(id)

    suspend fun createTransaksi(body: JsonObject) =
        api.createTransaksi(
            body,
            UUID.randomUUID().toString()
        )

    suspend fun updateTransaksi(
        id: String,
        body: JsonObject
    ) =
        api.updateTransaksi(id, body)

    suspend fun deleteTransaksi(
        id: String,
        reason: String?
    ) =
        api.deleteTransaksi(id, reason)

    // ========================================================
    // PRODUK
    // ========================================================

    suspend fun produk() =
        api.produk()

    suspend fun createProduk(body: JsonObject) =
        api.createProduk(body)

    suspend fun updateProduk(
        id: String,
        body: JsonObject
    ) =
        api.updateProduk(id, body)

    suspend fun deleteProduk(id: String) =
        api.deleteProduk(id)

    // ========================================================
    // KATEGORI
    // ========================================================

    suspend fun kategori() =
        api.kategori()

    suspend fun createKategori(body: JsonObject) =
        api.createKategori(body)

    suspend fun updateKategori(
        id: String,
        body: JsonObject
    ) =
        api.updateKategori(id, body)

    suspend fun deleteKategori(id: String) =
        api.deleteKategori(id)

    // ========================================================
    // PELANGGAN
    // ========================================================

    suspend fun pelanggan(q: String? = null) =
        api.pelanggan(q)

    suspend fun pelangganDetail(id: String) =
        api.pelangganDetail(id)

    suspend fun createPelanggan(body: JsonObject) =
        api.createPelanggan(body)

    suspend fun mergePelanggan(body: JsonObject) =
        api.mergePelanggan(body)

    // ========================================================
    // KASBON
    // ========================================================

    suspend fun kasbon() =
        api.kasbon()

    suspend fun createKasbon(body: JsonObject) =
        api.createKasbon(body)

    suspend fun updateKasbon(
        id: String,
        body: JsonObject
    ) =
        api.updateKasbon(id, body)

    // ========================================================
    // PENGELUARAN
    // ========================================================

    suspend fun pengeluaran() =
        api.pengeluaran()

    suspend fun createPengeluaran(body: JsonObject) =
        api.createPengeluaran(
            body,
            UUID.randomUUID().toString()
        )

    suspend fun pengeluaranDetail(id: String) =
        api.pengeluaranDetail(id)

    suspend fun updatePengeluaran(
        id: String,
        body: JsonObject
    ) =
        api.updatePengeluaran(id, body)

    suspend fun deletePengeluaran(
        id: String,
        reason: String?
    ) =
        api.deletePengeluaran(id, reason)

    // ========================================================
    // SERVICE HP
    // ========================================================

    suspend fun serviceHp() =
        api.serviceHp()

    suspend fun createServiceHp(body: JsonObject) =
        api.createServiceHp(body)

    suspend fun updateServiceHp(
        id: String,
        body: JsonObject
    ) =
        api.updateServiceHp(id, body)

    // ========================================================
    // GAJI
    // ========================================================

    suspend fun gaji() =
        api.gaji()

    suspend fun createGaji(body: JsonObject) =
        api.createGaji(body)

    suspend fun updateGaji(
        id: String,
        body: JsonObject
    ) =
        api.updateGaji(id, body)

    suspend fun gajiRate() =
        api.gajiRate()

    suspend fun updateGajiRate(body: JsonObject) =
        api.updateGajiRate(body)

    // ========================================================
    // USERS
    // ========================================================

    suspend fun users() =
        api.users()

    suspend fun createUser(body: JsonObject) =
        api.createUser(body)

    suspend fun updateUser(
        id: String,
        body: JsonObject
    ) =
        api.updateUser(id, body)

    suspend fun updateUserPermissions(
        id: String,
        body: JsonObject
    ) =
        api.updateUserPermissions(id, body)

    // ========================================================
    // AKUN
    // ========================================================

    suspend fun akun() =
        api.akun()

    suspend fun createAkun(body: JsonObject) =
        api.createAkun(body)

    suspend fun updateAkun(
        id: String,
        body: JsonObject
    ) =
        api.updateAkun(id, body)

    // ========================================================
    // SETTINGS
    // ========================================================

    suspend fun settings() =
        api.settings()

    suspend fun updateSettings(body: JsonObject) =
        api.updateSettings(body)

    suspend fun generateSettings(body: JsonObject = buildJsonObject {}) =
        api.generateSettings(body)

    suspend fun updateNotifhookSource(body: JsonObject) =
        api.updateNotifhookSource(body)

    // ========================================================
    // LOGS
    // ========================================================

    suspend fun logs() =
        api.logs()

    // ========================================================
    // LAPORAN
    // ========================================================

    suspend fun laporanBulan(month: String) =
        api.laporanBulan(month)

    suspend fun laporanTahun(year: Int) =
        api.laporanTahun(year)

    suspend fun laporanExport(
        month: String? = null,
        year: Int? = null
    ) =
        api.laporanExport(
            bulan = month,
            tahun = year
        )

    // ========================================================
    // NOTIFHOOK
    // ========================================================

    suspend fun notifhook(
        body: JsonObject,
        apiKey: String
    ) =
        api.notifhook(
            body = body,
            apiKey = apiKey,
            idempotencyKey = UUID.randomUUID().toString()
        )
}

private fun JsonObject.toUserSession(): UserSession {

    val idValue =
        this["id"]
            ?.jsonPrimitive
            ?.contentOrNull

    val permissions =
        obj("permissions")
            ?.entries
            ?.associate { (key, value) ->
                key to (
                    value.jsonPrimitive.booleanOrNull
                        ?: false
                )
            }
            ?: emptyMap()

    return UserSession(
        id = idValue,
        nama = string("nama").orEmpty(),
        username = string("username").orEmpty(),
        role = string("role").orEmpty(),
        permissions = permissions
    )
}
