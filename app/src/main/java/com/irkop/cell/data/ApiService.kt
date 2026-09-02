package com.irkop.cell.data

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login") suspend fun login(@Body body: JsonObject): JsonObject
    @GET("auth/me") suspend fun me(): JsonObject
    @POST("auth/logout") suspend fun logout(): JsonObject
    @GET("kasir/current") suspend fun kasirCurrent(): JsonObject
    @GET("kasir/reminder-closing") suspend fun reminderClosing(): JsonObject
    @POST("kasir/opening") suspend fun kasirOpening(@Body body: JsonObject): JsonObject
    @POST("kasir/closing") suspend fun kasirClosing(@Body body: JsonObject): JsonObject
    @GET("transaksi") suspend fun transaksi(@Query("q") q:String?=null,@Query("tanggal") tanggal:String?=null,@Query("tanggal_mulai") tanggalMulai:String?=null,@Query("tanggal_selesai") tanggalSelesai:String?=null,@Query("metode_bayar") metodeBayar:String?=null,@Query("status_konfirmasi") statusKonfirmasi:String?=null):JsonObject
    @POST("transaksi") suspend fun createTransaksi(@Body body:JsonObject,@Header("Idempotency-Key") idempotencyKey:String):JsonObject
    @GET("transaksi/{id}") suspend fun transaksiDetail(@Path("id") id:String):JsonObject
    @PUT("transaksi/{id}") suspend fun updateTransaksi(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("transaksi/{id}") suspend fun deleteTransaksi(@Path("id") id:String,@Query("reason") reason:String?=null):JsonObject
    @PUT("transaksi/{id}/konfirmasi") suspend fun updateTransaksiKonfirmasi(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("produk") suspend fun produk():JsonObject
    @POST("produk") suspend fun createProduk(@Body body:JsonObject):JsonObject
    @PUT("produk/{id}") suspend fun updateProduk(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("produk/{id}") suspend fun deleteProduk(@Path("id") id:String):JsonObject
    @GET("kategori") suspend fun kategori():JsonObject
    @POST("kategori") suspend fun createKategori(@Body body:JsonObject):JsonObject
    @PUT("kategori/{id}") suspend fun updateKategori(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("kategori/{id}") suspend fun deleteKategori(@Path("id") id:String):JsonObject
    @GET("pelanggan") suspend fun pelanggan(@Query("q") q:String?=null):JsonObject
    @POST("pelanggan") suspend fun createPelanggan(@Body body:JsonObject):JsonObject
    @GET("pelanggan/{id}") suspend fun pelangganDetail(@Path("id") id:String):JsonObject
    @PUT("pelanggan/{id}") suspend fun updatePelanggan(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("pelanggan/{id}") suspend fun deletePelanggan(@Path("id") id:String):JsonObject
    @POST("pelanggan/merge") suspend fun mergePelanggan(@Body body:JsonObject):JsonObject
    @GET("kasbon") suspend fun kasbon():JsonObject
    @POST("kasbon") suspend fun createKasbon(@Body body:JsonObject):JsonObject
    @PUT("kasbon/{id}") suspend fun updateKasbon(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("pengeluaran") suspend fun pengeluaran():JsonObject
    @POST("pengeluaran") suspend fun createPengeluaran(@Body body:JsonObject,@Header("Idempotency-Key") idempotencyKey:String):JsonObject
    @GET("pengeluaran/{id}") suspend fun pengeluaranDetail(@Path("id") id:String):JsonObject
    @PUT("pengeluaran/{id}") suspend fun updatePengeluaran(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("pengeluaran/{id}") suspend fun deletePengeluaran(@Path("id") id:String,@Query("reason") reason:String?=null):JsonObject
    @GET("service-hp") suspend fun serviceHp():JsonObject
    @POST("service-hp") suspend fun createServiceHp(@Body body:JsonObject):JsonObject
    @PUT("service-hp/{id}") suspend fun updateServiceHp(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("gaji") suspend fun gaji():JsonObject
    @POST("gaji") suspend fun createGaji(@Body body:JsonObject):JsonObject
    @PUT("gaji/{id}") suspend fun updateGaji(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("gaji/rate") suspend fun gajiRate():JsonObject
    @POST("gaji/rate") suspend fun updateGajiRate(@Body body:JsonObject):JsonObject
    @GET("users") suspend fun users():JsonElement
    @POST("users") suspend fun createUser(@Body body:JsonObject):JsonObject
    @PUT("users/{id}") suspend fun updateUser(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @PUT("users/{id}/permissions") suspend fun updateUserPermissions(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("akun") suspend fun akun():JsonObject
    @POST("akun") suspend fun createAkun(@Body body:JsonObject):JsonObject
    @PUT("akun/{id}") suspend fun updateAkun(@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("settings") suspend fun settings():JsonObject
    @PUT("settings") suspend fun updateSettings(@Body body:JsonObject):JsonObject
    @POST("settings/generate") suspend fun generateSettings(@Body body:JsonObject=JsonObject(emptyMap())):JsonObject
    @POST("settings/notifhook-source") suspend fun updateNotifhookSource(@Body body:JsonObject):JsonObject
    @GET("logs") suspend fun logs():JsonObject
    @GET("laporan/bulan") suspend fun laporanBulan(@Query("bulan") bulan:String):JsonObject
    @GET("laporan/tahun") suspend fun laporanTahun(@Query("tahun") tahun:Int):JsonObject
    @GET("laporan/export") suspend fun laporanExport(@Query("bulan") bulan:String?=null,@Query("tahun") tahun:Int?=null):retrofit2.Response<okhttp3.ResponseBody>
    @POST("notifhook") suspend fun notifhook(@Body body:JsonObject,@Header("X-API-Key") apiKey:String,@Header("Idempotency-Key") idempotencyKey:String):JsonObject
}
