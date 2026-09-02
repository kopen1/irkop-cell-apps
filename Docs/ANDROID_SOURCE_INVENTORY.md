# Current Android Source Inventory

Generated: 2026-09-02T09:50:32+07:00

## Kotlin files
app/src/main/java/com/irkop/cell/MainActivity.kt
app/src/main/java/com/irkop/cell/core/ApiClient.kt
app/src/main/java/com/irkop/cell/core/ApiError.kt
app/src/main/java/com/irkop/cell/core/SessionManager.kt
app/src/main/java/com/irkop/cell/core/UserSession.kt
app/src/main/java/com/irkop/cell/data/ApiService.kt
app/src/main/java/com/irkop/cell/data/JsonExt.kt
app/src/main/java/com/irkop/cell/data/Repository.kt
app/src/main/java/com/irkop/cell/ui/AppViewModel.kt
app/src/main/java/com/irkop/cell/util/ReceiptShare.kt

## API declarations
app/src/main/java/com/irkop/cell/data/ApiService.kt:13:    @POST("auth/login")
app/src/main/java/com/irkop/cell/data/ApiService.kt:16:    @GET("auth/me")
app/src/main/java/com/irkop/cell/data/ApiService.kt:19:    @POST("auth/logout")
app/src/main/java/com/irkop/cell/data/ApiService.kt:22:    @GET("kasir/current")
app/src/main/java/com/irkop/cell/data/ApiService.kt:25:    @GET("kasir/reminder-closing")
app/src/main/java/com/irkop/cell/data/ApiService.kt:28:    @POST("kasir/opening")
app/src/main/java/com/irkop/cell/data/ApiService.kt:31:    @POST("kasir/closing")
app/src/main/java/com/irkop/cell/data/ApiService.kt:34:    @GET("transaksi")
app/src/main/java/com/irkop/cell/data/ApiService.kt:47:    @POST("transaksi")
app/src/main/java/com/irkop/cell/data/ApiService.kt:53:    @GET("transaksi/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:56:    @PUT("transaksi/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:62:    @DELETE("transaksi/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:65:    @GET("produk")
app/src/main/java/com/irkop/cell/data/ApiService.kt:68:    @POST("produk")
app/src/main/java/com/irkop/cell/data/ApiService.kt:71:    @PUT("produk/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:74:    @DELETE("produk/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:77:    @GET("kategori")
app/src/main/java/com/irkop/cell/data/ApiService.kt:80:    @POST("kategori")
app/src/main/java/com/irkop/cell/data/ApiService.kt:83:    @PUT("kategori/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:86:    @DELETE("kategori/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:89:    @GET("pelanggan")
app/src/main/java/com/irkop/cell/data/ApiService.kt:92:    @POST("pelanggan")
app/src/main/java/com/irkop/cell/data/ApiService.kt:95:    @GET("pelanggan/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:98:    @POST("pelanggan/merge")
app/src/main/java/com/irkop/cell/data/ApiService.kt:101:    @GET("kasbon")
app/src/main/java/com/irkop/cell/data/ApiService.kt:104:    @POST("kasbon")
app/src/main/java/com/irkop/cell/data/ApiService.kt:107:    @PUT("kasbon/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:110:    @GET("pengeluaran")
app/src/main/java/com/irkop/cell/data/ApiService.kt:113:    @POST("pengeluaran")
app/src/main/java/com/irkop/cell/data/ApiService.kt:119:    @GET("pengeluaran/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:122:    @PUT("pengeluaran/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:125:    @DELETE("pengeluaran/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:128:    @GET("service-hp")
app/src/main/java/com/irkop/cell/data/ApiService.kt:131:    @POST("service-hp")
app/src/main/java/com/irkop/cell/data/ApiService.kt:134:    @PUT("service-hp/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:137:    @GET("gaji")
app/src/main/java/com/irkop/cell/data/ApiService.kt:140:    @POST("gaji")
app/src/main/java/com/irkop/cell/data/ApiService.kt:143:    @PUT("gaji/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:146:    @GET("gaji/rate")
app/src/main/java/com/irkop/cell/data/ApiService.kt:149:    @POST("gaji/rate")
app/src/main/java/com/irkop/cell/data/ApiService.kt:152:    @GET("users")
app/src/main/java/com/irkop/cell/data/ApiService.kt:155:    @POST("users")
app/src/main/java/com/irkop/cell/data/ApiService.kt:158:    @PUT("users/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:161:    @PUT("users/{id}/permissions")
app/src/main/java/com/irkop/cell/data/ApiService.kt:164:    @GET("akun")
app/src/main/java/com/irkop/cell/data/ApiService.kt:167:    @POST("akun")
app/src/main/java/com/irkop/cell/data/ApiService.kt:170:    @PUT("akun/{id}")
app/src/main/java/com/irkop/cell/data/ApiService.kt:173:    @GET("settings")
app/src/main/java/com/irkop/cell/data/ApiService.kt:176:    @PUT("settings")
app/src/main/java/com/irkop/cell/data/ApiService.kt:179:    @POST("settings/generate")
app/src/main/java/com/irkop/cell/data/ApiService.kt:182:    @POST("settings/notifhook-source")
app/src/main/java/com/irkop/cell/data/ApiService.kt:185:    @GET("logs")
app/src/main/java/com/irkop/cell/data/ApiService.kt:188:    @GET("laporan/bulan")
app/src/main/java/com/irkop/cell/data/ApiService.kt:191:    @GET("laporan/tahun")

## Repository methods
8:    suspend fun login(username: String, password: String): Pair<String, UserSession> {
18:    suspend fun me(): UserSession {
24:    suspend fun logout() = runCatching { api.logout() }
26:    suspend fun kasirCurrent() = api.kasirCurrent()
27:    suspend fun reminderClosing() = api.reminderClosing()
29:    suspend fun opening(accounts: List<Pair<String, Long>>) =
41:    suspend fun closing(accounts: List<Pair<String, Long>>, note: String?) =
54:    suspend fun transaksi(q: String? = null) = api.transaksi(q = q)
55:    suspend fun transaksiDetail(id: String) = api.transaksiDetail(id)
57:    suspend fun createTransaksi(body: JsonObject) =
60:    suspend fun updateTransaksi(id: String, body: JsonObject) = api.updateTransaksi(id, body)
61:    suspend fun deleteTransaksi(id: String, reason: String?) =
64:    suspend fun produk() = api.produk()
65:    suspend fun createProduk(body: JsonObject) = api.createProduk(body)
66:    suspend fun updateProduk(id: String, body: JsonObject) = api.updateProduk(id, body)
67:    suspend fun deleteProduk(id: String) = api.deleteProduk(id)
69:    suspend fun kategori() = api.kategori()
70:    suspend fun createKategori(body: JsonObject) = api.createKategori(body)
71:    suspend fun updateKategori(id: String, body: JsonObject) = api.updateKategori(id, body)
72:    suspend fun deleteKategori(id: String) = api.deleteKategori(id)
74:    suspend fun pelanggan(q: String? = null) = api.pelanggan(q)
75:    suspend fun pelangganDetail(id: String) = api.pelangganDetail(id)
76:    suspend fun createPelanggan(body: JsonObject) = api.createPelanggan(body)
77:    suspend fun mergePelanggan(body: JsonObject) = api.mergePelanggan(body)
79:    suspend fun kasbon() = api.kasbon()
80:    suspend fun updateKasbon(id: String, body: JsonObject) = api.updateKasbon(id, body)
82:    suspend fun pengeluaran() = api.pengeluaran()
83:    suspend fun createPengeluaran(body: JsonObject) =
85:    suspend fun serviceHp() = api.serviceHp()
86:    suspend fun createServiceHp(body: JsonObject) = api.createServiceHp(body)
87:    suspend fun updateServiceHp(id: String, body: JsonObject) = api.updateServiceHp(id, body)
88:    suspend fun akun() = api.akun()
89:    suspend fun laporanBulan(month: String) = api.laporanBulan(month)
90:    suspend fun laporanTahun(year: Int) = api.laporanTahun(year)

## Workflow
.github/workflows/android-build.yml

## Forbidden API v1 references
./.github/workflows/android-build.yml:38:          echo "Checking forbidden /api/v1 references..."
./.github/workflows/android-build.yml:43:            'konter.irkop.workers.dev/api/v1' \
./.github/workflows/android-build.yml:45:            echo "ERROR: /api/v1 masih ditemukan."
./Docs/pre-overhaul-20260902-094944/README.md:10:`gradle assembleDebug -PAPI_BASE_URL=https://example/api/v1/`
./Docs/pre-overhaul-20260902-095031/android-build.yml:38:          echo "Checking forbidden /api/v1 references..."
./Docs/pre-overhaul-20260902-095031/android-build.yml:43:            'konter.irkop.workers.dev/api/v1' \
./Docs/pre-overhaul-20260902-095031/android-build.yml:45:            echo "ERROR: /api/v1 masih ditemukan."
./fix/check-login.sh:60:          URL="https://konter.irkop.workers.dev/api/v1/"
./fix/fix-api-and-build.sh:39:old = "https://konter.irkop.workers.dev/api/v1/"
./fix/next-all-local.sh:19:old = "https://konter.irkop.workers.dev/api/v1/"
./fix/complete-all.sh:87:    'https://konter.irkop.workers.dev/api/v1/',
./fix/complete-all.sh:92:    'https://konter.irkop.workers.dev/api/v1',
./fix/complete-all.sh:705:echo "[G] OLD API /api/v1 REFERENCES"
./fix/complete-all.sh:707:  'konter.irkop.workers.dev/api/v1' \
./fix/complete-all.sh:710:  echo "WARNING: masih ada referensi /api/v1."
./fix/complete-all.sh:712:  echo "OK: tidak ada /api/v1."
./fix/finalize-all.sh:42:        "https://konter.irkop.workers.dev/api/v1/",
./fix/finalize-all.sh:47:        "https://konter.irkop.workers.dev/api/v1",
./fix/finalize-all.sh:82:  'konter.irkop.workers.dev/api/v1' \
./fix/finalize-all.sh:87:    echo "ERROR: masih ada /api/v1."
./fix/finalize-all.sh:90:    echo "OK: /api/v1 sudah bersih."
./overhaul-phase1.sh:38:  sed -i 's#https://konter\.irkop\.workers\.dev/api/v1/#https://konter.irkop.workers.dev/api/#g' \
./overhaul-phase1.sh:43:  sed -i 's#https://konter\.irkop\.workers\.dev/api/v1/#https://konter.irkop.workers.dev/api/#g' \
./overhaul-phase1.sh:46:  sed -i 's#gradle assembleDebug -PAPI_BASE_URL=https://example/api/v1/#gradle assembleDebug -PAPI_BASE_URL=https://example/api/#g' \
./overhaul-phase1.sh:96:          echo "Checking forbidden /api/v1 references..."
./overhaul-phase1.sh:101:            'konter.irkop.workers.dev/api/v1' \
./overhaul-phase1.sh:103:            echo "ERROR: /api/v1 masih ditemukan."
./overhaul-phase1.sh:514:    'api/v1' \
./overhaul-phase1.sh:525:echo "  - /api/v1 -> /api/"
