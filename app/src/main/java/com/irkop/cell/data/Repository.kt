package com.irkop.cell.data

import com.irkop.cell.core.UserSession
import kotlinx.serialization.json.*
import java.util.UUID

class Repository(private val api: ApiService) {
    suspend fun login(username: String, password: String): Pair<String, UserSession> {
        val result = api.login(buildJsonObject {
            put("username", username)
            put("password", password)
        })
        val token = result.string("token") ?: error(result.obj("error")?.string("message") ?: "Login gagal")
        val userObj = result.obj("user") ?: error("Data user tidak tersedia")
        return token to userObj.toUserSession()
    }

    suspend fun me(): UserSession {
        val result = api.me()
        val userObj = result.obj("user") ?: result
        return userObj.toUserSession()
    }

    suspend fun logout() = runCatching { api.logout() }

    suspend fun kasirCurrent() = api.kasirCurrent()
    suspend fun reminderClosing() = api.reminderClosing()

    suspend fun opening(accounts: List<Pair<String, Long>>) =
        api.kasirOpening(buildJsonObject {
            putJsonArray("saldo_awal") {
                accounts.forEach { (name, amount) ->
                    add(buildJsonObject {
                        put("nama_akun", name)
                        put("saldo", amount)
                    })
                }
            }
        })

    suspend fun closing(accounts: List<Pair<String, Long>>, note: String?) =
        api.kasirClosing(buildJsonObject {
            putJsonArray("saldo_real") {
                accounts.forEach { (name, amount) ->
                    add(buildJsonObject {
                        put("nama_akun", name)
                        put("saldo_real", amount)
                    })
                }
            }
            note?.takeIf { it.isNotBlank() }?.let { put("catatan_closing", it) }
        })

    suspend fun transaksi(q: String? = null) = api.transaksi(q = q)
    suspend fun transaksiDetail(id: String) = api.transaksiDetail(id)

    suspend fun createTransaksi(body: JsonObject) =
        api.createTransaksi(body, UUID.randomUUID().toString())

    suspend fun updateTransaksi(id: String, body: JsonObject) = api.updateTransaksi(id, body)
    suspend fun deleteTransaksi(id: String, reason: String?) =
        api.deleteTransaksi(id)

    suspend fun produk() = api.produk()
    suspend fun createProduk(body: JsonObject) = api.createProduk(body)
    suspend fun updateProduk(id: String, body: JsonObject) = api.updateProduk(id, body)
    suspend fun deleteProduk(id: String) = api.deleteProduk(id)

    suspend fun kategori() = api.kategori()
    suspend fun createKategori(body: JsonObject) = api.createKategori(body)
    suspend fun updateKategori(id: String, body: JsonObject) = api.updateKategori(id, body)
    suspend fun deleteKategori(id: String) = api.deleteKategori(id)

    suspend fun pelanggan(q: String? = null) = api.pelanggan(q)
    suspend fun pelangganDetail(id: String) = api.pelangganDetail(id)
    suspend fun createPelanggan(body: JsonObject) = api.createPelanggan(body)
    suspend fun mergePelanggan(body: JsonObject) = api.mergePelanggan(body)

    suspend fun kasbon() = api.kasbon()
    suspend fun updateKasbon(id: String, body: JsonObject) = api.updateKasbon(id, body)

    suspend fun pengeluaran() = api.pengeluaran()
    suspend fun createPengeluaran(body: JsonObject) =
        api.createPengeluaran(body, UUID.randomUUID().toString())
    suspend fun serviceHp() = api.serviceHp()
    suspend fun createServiceHp(body: JsonObject) = api.createServiceHp(body)
    suspend fun updateServiceHp(id: String, body: JsonObject) = api.updateServiceHp(id, body)
    suspend fun akun() = api.akun()
    suspend fun laporanBulan(month: String) = api.laporanBulan(month)
    suspend fun laporanTahun(year: Int) = api.laporanTahun(year)
}

private fun JsonObject.toUserSession(): UserSession {
    val permissions = obj("permissions")?.mapValues { it.value.jsonPrimitive.booleanOrNull ?: false } ?: emptyMap()
    return UserSession(
        id = string("id"),
        nama = string("nama").orEmpty(),
        username = string("username").orEmpty(),
        role = string("role").orEmpty(),
        permissions = permissions
    )
}
