# Android API Parity Status

Generated: 2026-09-02T09:53:57+07:00

## Implemented in ApiService
19:    @POST("auth/login")
22:    @GET("auth/me")
25:    @POST("auth/logout")
32:    @GET("kasir/current")
35:    @GET("kasir/reminder-closing")
38:    @POST("kasir/opening")
41:    @POST("kasir/closing")
48:    @GET("transaksi")
58:    @POST("transaksi")
64:    @GET("transaksi/{id}")
69:    @PUT("transaksi/{id}")
75:    @DELETE("transaksi/{id}")
85:    @GET("produk")
88:    @POST("produk")
91:    @PUT("produk/{id}")
97:    @DELETE("produk/{id}")
104:    @GET("kategori")
107:    @POST("kategori")
110:    @PUT("kategori/{id}")
116:    @DELETE("kategori/{id}")
123:    @GET("pelanggan")
128:    @POST("pelanggan")
131:    @GET("pelanggan/{id}")
136:    @POST("pelanggan/merge")
143:    @GET("kasbon")
146:    @POST("kasbon")
149:    @PUT("kasbon/{id}")
159:    @GET("pengeluaran")
162:    @POST("pengeluaran")
168:    @GET("pengeluaran/{id}")
173:    @PUT("pengeluaran/{id}")
179:    @DELETE("pengeluaran/{id}")
189:    @GET("service-hp")
192:    @POST("service-hp")
195:    @PUT("service-hp/{id}")
205:    @GET("gaji")
208:    @POST("gaji")
211:    @PUT("gaji/{id}")
217:    @GET("gaji/rate")
220:    @POST("gaji/rate")
227:    @GET("users")
230:    @POST("users")
233:    @PUT("users/{id}")
239:    @PUT("users/{id}/permissions")
249:    @GET("akun")
252:    @POST("akun")
255:    @PUT("akun/{id}")
265:    @GET("settings")
268:    @PUT("settings")
271:    @POST("settings/generate")
274:    @POST("settings/notifhook-source")
281:    @GET("logs")
288:    @GET("laporan/bulan")
293:    @GET("laporan/tahun")
298:    @GET("laporan/export")
308:    @POST("notifhook")

## Repository methods
18:    suspend fun login(
42:    suspend fun me(): UserSession {
48:    suspend fun logout() =
55:    suspend fun kasirCurrent() =
58:    suspend fun reminderClosing() =
61:    suspend fun opening(
79:    suspend fun closing(
106:    suspend fun transaksi(
123:    suspend fun transaksiDetail(id: String) =
126:    suspend fun createTransaksi(body: JsonObject) =
132:    suspend fun updateTransaksi(
138:    suspend fun deleteTransaksi(
148:    suspend fun produk() =
151:    suspend fun createProduk(body: JsonObject) =
154:    suspend fun updateProduk(
160:    suspend fun deleteProduk(id: String) =
167:    suspend fun kategori() =
170:    suspend fun createKategori(body: JsonObject) =
173:    suspend fun updateKategori(
179:    suspend fun deleteKategori(id: String) =
186:    suspend fun pelanggan(q: String? = null) =
189:    suspend fun pelangganDetail(id: String) =
192:    suspend fun createPelanggan(body: JsonObject) =
195:    suspend fun mergePelanggan(body: JsonObject) =
202:    suspend fun kasbon() =
205:    suspend fun createKasbon(body: JsonObject) =
208:    suspend fun updateKasbon(
218:    suspend fun pengeluaran() =
221:    suspend fun createPengeluaran(body: JsonObject) =
227:    suspend fun pengeluaranDetail(id: String) =
230:    suspend fun updatePengeluaran(
236:    suspend fun deletePengeluaran(
246:    suspend fun serviceHp() =
249:    suspend fun createServiceHp(body: JsonObject) =
252:    suspend fun updateServiceHp(
262:    suspend fun gaji() =
265:    suspend fun createGaji(body: JsonObject) =
268:    suspend fun updateGaji(
274:    suspend fun gajiRate() =
277:    suspend fun updateGajiRate(body: JsonObject) =
284:    suspend fun users() =
287:    suspend fun createUser(body: JsonObject) =
290:    suspend fun updateUser(
296:    suspend fun updateUserPermissions(
306:    suspend fun akun() =
309:    suspend fun createAkun(body: JsonObject) =
312:    suspend fun updateAkun(
322:    suspend fun settings() =
325:    suspend fun updateSettings(body: JsonObject) =
328:    suspend fun generateSettings(body: JsonObject = buildJsonObject {}) =
331:    suspend fun updateNotifhookSource(body: JsonObject) =
338:    suspend fun logs() =
345:    suspend fun laporanBulan(month: String) =
348:    suspend fun laporanTahun(year: Int) =
351:    suspend fun laporanExport(
364:    suspend fun notifhook(

## Required upstream additions
- laporan/export
- notifhook
- transaction filters
- expense delete reason
- complete admin repository
- complete settings repository

## CI
TEST ONLY
APK build disabled
Commit disabled
Push disabled
