package com.irkop.cell.data

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FeatureApiService {
    @GET("produk") suspend fun produk(@Header("Authorization") auth: String,@Query("q") q:String?=null,@Query("kategori_id") kategoriId:Long?=null):JsonObject
    @POST("produk") suspend fun createProduk(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("produk/{id}") suspend fun updateProduk(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("produk/{id}") suspend fun deleteProduk(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @GET("kategori") suspend fun kategori(@Header("Authorization") auth:String):JsonObject
    @POST("kategori") suspend fun createKategori(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("kategori/{id}") suspend fun updateKategori(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("kategori/{id}") suspend fun deleteKategori(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @GET("pelanggan") suspend fun pelanggan(@Header("Authorization") auth:String,@Query("q") q:String?=null):JsonObject
    @GET("pelanggan/{id}") suspend fun pelangganDetail(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @POST("pelanggan") suspend fun createPelanggan(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("pelanggan/{id}") suspend fun updatePelanggan(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("pelanggan/{id}") suspend fun deletePelanggan(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @POST("pelanggan/merge") suspend fun mergePelanggan(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @GET("kasbon") suspend fun kasbon(@Header("Authorization") auth:String,@Query("status") status:String?=null):JsonObject
    @POST("kasbon") suspend fun createKasbon(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("kasbon/{id}") suspend fun updateKasbon(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("kasbon/{id}") suspend fun deleteKasbon(@Header("Authorization") auth:String,@Path("id") id:String,@Query("reason") reason:String?=null):JsonObject
    @GET("pengeluaran") suspend fun pengeluaran(@Header("Authorization") auth:String,@Query("q") q:String?=null,@Query("metode_bayar") metode:String?=null,@Query("akun_sumber") akun:String?=null):JsonObject
    @GET("pengeluaran/{id}") suspend fun pengeluaranDetail(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @POST("pengeluaran") suspend fun createPengeluaran(@Header("Authorization") auth:String,@Header("Idempotency-Key") key:String,@Body body:JsonObject):JsonObject
    @PUT("pengeluaran/{id}") suspend fun updatePengeluaran(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("pengeluaran/{id}") suspend fun deletePengeluaran(@Header("Authorization") auth:String,@Path("id") id:String,@Query("reason") reason:String?=null):JsonObject
    @GET("service-hp") suspend fun serviceHp(@Header("Authorization") auth:String,@Query("status") status:String?=null):JsonObject
    @POST("service-hp") suspend fun createServiceHp(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("service-hp/{id}") suspend fun updateServiceHp(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("service-hp/{id}") suspend fun deleteServiceHp(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @GET("gaji") suspend fun gaji(@Header("Authorization") auth:String):JsonObject
    @POST("gaji") suspend fun createGaji(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("gaji/{id}") suspend fun updateGaji(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("gaji/rate") suspend fun gajiRate(@Header("Authorization") auth:String):JsonObject
    @POST("gaji/rate") suspend fun updateGajiRate(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @GET("users") suspend fun users(@Header("Authorization") auth:String):JsonElement
    @POST("users") suspend fun createUser(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("users/{id}") suspend fun updateUser(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @PUT("users/{id}/permissions") suspend fun updateUserPermissions(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @GET("akun") suspend fun akun(@Header("Authorization") auth:String):JsonObject
    @POST("akun") suspend fun createAkun(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @PUT("akun/{id}") suspend fun updateAkun(@Header("Authorization") auth:String,@Path("id") id:String,@Body body:JsonObject):JsonObject
    @DELETE("akun/{id}") suspend fun deleteAkun(@Header("Authorization") auth:String,@Path("id") id:String):JsonObject
    @GET("settings") suspend fun settings(@Header("Authorization") auth:String):JsonObject
    @PUT("settings") suspend fun updateSettings(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @POST("settings/generate") suspend fun generateSettings(@Header("Authorization") auth:String,@Body body:JsonObject=JsonObject(emptyMap())):JsonObject
    @POST("settings/notifhook-source") suspend fun updateNotifhookSource(@Header("Authorization") auth:String,@Body body:JsonObject):JsonObject
    @GET("logs") suspend fun logs(@Header("Authorization") auth:String):JsonObject
    @GET("laporan/export") suspend fun laporanExport(@Header("Authorization") auth:String,@Query("bulan") bulan:String?=null,@Query("tahun") tahun:Int?=null):Response<ResponseBody>
    @POST("notifhook") suspend fun notifhook(@Header("X-API-Key") apiKey:String,@Header("Idempotency-Key") key:String,@Body body:JsonObject):JsonObject
}
