package com.irkop.cell

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.SessionManager
import com.irkop.cell.data.Repository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class LiveCrudE2ETest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun login_then_live_read_and_crud_smoke() = runBlocking {
        loginThroughUi()

        val session = SessionManager(compose.activity.applicationContext)
        session.load()
        assertNotNull("Login UI berhasil tetapi session JWT tidak tersimpan", session.token)

        val repo = Repository(ApiClient(session).api)
        val failures = mutableListOf<String>()
        val suffix = UUID.randomUUID().toString().take(8)
        val today = LocalDate.now().toString()

        fun check(name: String, block: suspend () -> Unit) {
            try {
                runBlocking { block() }
            } catch (t: Throwable) {
                failures += "$name -> ${t.message ?: t::class.java.simpleName}"
            }
        }

        check("auth/me") { assertTrue(repo.me().username.isNotBlank()) }
        check("dashboard/kasir-current") { repo.kasirCurrent() }
        check("dashboard/reminder-closing") { repo.reminderClosing() }
        check("transaksi/list") { repo.transaksi() }
        check("transaksi/filter-date") { repo.transaksi(tanggal = today) }
        check("transaksi/filter-range") { repo.transaksi(tanggalMulai = today, tanggalSelesai = today) }
        check("kasbon/list") { repo.kasbon() }
        check("pengeluaran/list") { repo.pengeluaran() }
        check("service-hp/list") { repo.serviceHp() }
        check("gaji/list") { repo.gaji() }
        check("gaji/rate") { repo.gajiRate() }
        check("users/list") { repo.users() }
        check("akun/list") { repo.akun() }
        check("settings/read") { repo.settings() }
        check("audit/logs") { repo.logs() }
        check("laporan/bulan") { repo.laporanBulan(today.substring(0, 7)) }
        check("laporan/tahun") { repo.laporanTahun(today.substring(0, 4).toInt()) }
        check("laporan/export") { repo.laporanExport(month = today.substring(0, 7)) }

        var categoryId: String? = null
        var productId: String? = null
        var customerId: String? = null
        var serviceId: String? = null
        var expenseId: String? = null
        var kasbonId: String? = null
        var transactionId: String? = null
        var accountId: String? = null
        var openedByTest = false

        try {
            check("kategori/CRUD") {
                val created = repo.createKategori(
                    buildJsonObject {
                        put("nama", "E2E Kategori $suffix")
                        put("lacak_stok", true)
                    }
                )
                categoryId = created.findId()
                assertNotNull("POST kategori tidak mengembalikan id", categoryId)
                val id = requireNotNull(categoryId)
                repo.kategori()
                repo.updateKategori(id, buildJsonObject {
                    put("nama", "E2E Kategori ${suffix}U")
                    put("lacak_stok", true)
                })
            }

            check("produk/CRUD") {
                val created = repo.createProduk(
                    buildJsonObject {
                        put("kode", "E2E-$suffix")
                        put("nama", "E2E Produk $suffix")
                        categoryId?.toLongOrNull()?.let { put("kategori_id", it) }
                        put("harga", 11000)
                        put("harga_modal", 7000)
                        put("stok", 10)
                        put("stok_minimum", 1)
                        put("satuan", "pcs")
                    }
                )
                productId = created.findId()
                assertNotNull("POST produk tidak mengembalikan id", productId)
                val id = requireNotNull(productId)
                repo.produk()
                repo.updateProduk(id, buildJsonObject {
                    put("kode", "E2E-$suffix")
                    put("nama", "E2E Produk ${suffix}U")
                    categoryId?.toLongOrNull()?.let { put("kategori_id", it) }
                    put("harga", 12000)
                    put("harga_modal", 7000)
                    put("stok", 9)
                    put("stok_minimum", 1)
                    put("satuan", "pcs")
                })
            }

            check("pelanggan/CRUD") {
                val created = repo.createPelanggan(
                    buildJsonObject {
                        put("nama", "E2E Pelanggan $suffix")
                        put("telepon", "0812$suffix")
                    }
                )
                customerId = created.findId()
                assertNotNull("POST pelanggan tidak mengembalikan id", customerId)
                val id = requireNotNull(customerId)
                repo.pelanggan()
                repo.pelanggan("E2E Pelanggan")
                repo.pelangganDetail(id)
                repo.updatePelanggan(id, buildJsonObject {
                    put("nama", "E2E Pelanggan ${suffix}U")
                    put("telepon", "0813$suffix")
                })
            }

            check("akun/CRUD") {
                val created = repo.createAkun(
                    buildJsonObject {
                        put("nama_akun", "E2E Account $suffix")
                        put("tipe", "lainnya")
                    }
                )
                accountId = created.findId()
                assertNotNull("POST akun tidak mengembalikan id", accountId)
                val id = requireNotNull(accountId)
                repo.akun()
                repo.updateAkun(id, buildJsonObject {
                    put("nama_akun", "E2E Account ${suffix}U")
                    put("tipe", "lainnya")
                    put("aktif", true)
                })
            }

            val currentKasir = repo.kasirCurrent()
            if (currentKasir.stringValue("status") == "belum_buka") {
                repo.opening(
                    listOf(
                        "Tunai Laci" to 500000L,
                        "SeaBank" to 100000L,
                        "DANA" to 0L,
                        "OrderKuota" to 0L
                    )
                )
                openedByTest = true
            }

            check("transaksi/CRUD") {
                val product = requireNotNull(productId) { "produk test tidak tersedia" }
                val productNumericId = product.toLongOrNull()
                    ?: error("ID produk bukan angka: $product")
                val created = repo.createTransaksi(
                    buildJsonObject {
                        put("items", buildJsonArray {
                            add(buildJsonObject {
                                put("produk_id", productNumericId)
                                put("qty", 1)
                            })
                        })
                        put("metode_bayar", "tunai")
                    }
                )
                transactionId = created.findId()
                assertNotNull("POST transaksi tidak mengembalikan id", transactionId)
                val id = requireNotNull(transactionId)
                repo.transaksiDetail(id)
                repo.updateTransaksi(id, buildJsonObject {
                    put("items", buildJsonArray {
                        add(buildJsonObject {
                            put("produk_id", productNumericId)
                            put("qty", 1)
                        })
                    })
                    put("metode_bayar", "tunai")
                })
            }

            check("kasbon/CRUD") {
                val customer = requireNotNull(customerId) { "pelanggan test tidak tersedia" }
                val customerNumericId = customer.toLongOrNull()
                    ?: error("ID pelanggan bukan angka: $customer")
                val created = repo.createKasbon(
                    buildJsonObject {
                        put("pelanggan_id", customerNumericId)
                        put("nominal", 15000)
                        put("tanggal", today)
                        put("jatuh_tempo", today)
                        put("catatan", "E2E test")
                    }
                )
                kasbonId = created.findId()
                assertNotNull("POST kasbon tidak mengembalikan id", kasbonId)
                val id = requireNotNull(kasbonId)
                repo.updateKasbon(id, buildJsonObject {
                    put("pelanggan_id", customerNumericId)
                    put("nominal", 16000)
                    put("tanggal", today)
                    put("jatuh_tempo", today)
                    put("catatan", "E2E update")
                })
            }

            check("pengeluaran/CRUD") {
                val created = repo.createPengeluaran(
                    buildJsonObject {
                        put("deskripsi", "E2E Pengeluaran $suffix")
                        put("kategori", "testing")
                        put("nominal", 1000)
                        put("metode_bayar", "tunai")
                        put("akun_sumber", "Tunai Laci")
                        put("tanggal", today)
                    }
                )
                expenseId = created.findId()
                assertNotNull("POST pengeluaran tidak mengembalikan id", expenseId)
                val id = requireNotNull(expenseId)
                repo.pengeluaranDetail(id)
                repo.updatePengeluaran(id, buildJsonObject {
                    put("deskripsi", "E2E Pengeluaran ${suffix}U")
                    put("kategori", "testing")
                    put("nominal", 1100)
                    put("metode_bayar", "tunai")
                    put("akun_sumber", "Tunai Laci")
                    put("tanggal", today)
                })
            }

            check("service-hp/CRUD") {
                val customer = requireNotNull(customerId) { "pelanggan test tidak tersedia" }
                val customerNumericId = customer.toLongOrNull()
                    ?: error("ID pelanggan bukan angka: $customer")
                val created = repo.createServiceHp(
                    buildJsonObject {
                        put("pelanggan_id", customerNumericId)
                        put("nama_device", "E2E Android $suffix")
                        put("deskripsi_kerusakan", "E2E test")
                        put("status", "masuk")
                        put("estimasi_biaya", 25000)
                        put("tanggal_masuk", today)
                    }
                )
                serviceId = created.findId()
                assertNotNull("POST service-hp tidak mengembalikan id", serviceId)
                val id = requireNotNull(serviceId)
                repo.updateServiceHp(id, buildJsonObject {
                    put("pelanggan_id", customerNumericId)
                    put("nama_device", "E2E Android ${suffix}U")
                    put("deskripsi_kerusakan", "E2E update")
                    put("status", "proses")
                    put("estimasi_biaya", 26000)
                    put("tanggal_masuk", today)
                })
            }
        } finally {
            runCatching { transactionId?.let { repo.deleteTransaksi(it, "E2E cleanup") } }
            runCatching { expenseId?.let { repo.deletePengeluaran(it, "E2E cleanup") } }
            runCatching { kasbonId?.let { repo.deleteKasbon(it, "E2E cleanup") } }
            runCatching { serviceId?.let { repo.deleteServiceHp(it) } }
            runCatching { productId?.let { repo.deleteProduk(it) } }
            runCatching { customerId?.let { repo.deletePelanggan(it) } }
            runCatching { accountId?.let { repo.deleteAkun(it) } }
            runCatching { categoryId?.let { repo.deleteKategori(it) } }
            if (openedByTest) {
                runCatching {
                    repo.closing(
                        listOf(
                            "Tunai Laci" to 500000L,
                            "SeaBank" to 100000L,
                            "DANA" to 0L,
                            "OrderKuota" to 0L
                        ),
                        "E2E cleanup"
                    )
                }
            }
        }

        if (failures.isNotEmpty()) {
            assertTrue(
                "LIVE E2E BUGS (${failures.size}):\n- ${failures.joinToString("\n- ")}",
                false
            )
        }
    }

    private fun loginThroughUi() {
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("demo")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("demodemo")
        compose.onNodeWithText("LOGIN").performClick()
        compose.onNodeWithText("Dashboard").assertExists()
    }

    private fun JsonObject.findId(): String? {
        val direct = this["id"]?.jsonPrimitive?.contentOrNull
        if (!direct.isNullOrBlank()) return direct
        for (key in listOf("data", "item", "produk", "kategori", "pelanggan", "kasbon", "pengeluaran", "service")) {
            val nested = this[key] as? JsonObject
            val id = nested?.findId()
            if (!id.isNullOrBlank()) return id
        }
        return null
    }

    private fun JsonObject.stringValue(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull
}
