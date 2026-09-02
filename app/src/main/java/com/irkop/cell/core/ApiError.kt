package com.irkop.cell.core

import retrofit2.HttpException

object ApiError {
    fun message(t: Throwable): String {
        if (t is HttpException) {
            return when (t.code()) {
                400 -> "Permintaan tidak valid. Periksa data yang dimasukkan."
                401 -> "Sesi login tidak valid. Silakan login kembali."
                403 -> "Akses ditolak. Permission tidak mencukupi."
                404 -> "Data atau endpoint tidak ditemukan."
                409 -> "Data bentrok/duplikat. Periksa kembali."
                500 -> "Server sedang bermasalah. Coba lagi."
                else -> "Request gagal (HTTP ${t.code()})."
            }
        }
        return t.message ?: "Terjadi kesalahan."
    }
}
