package com.irkop.cell.core

/**
 * Aturan akses client-side untuk menjaga UI mengikuti permission
 * yang diberikan backend.
 *
 * Backend tetap menjadi authority utama. Permission di sini hanya
 * menentukan apa yang boleh ditampilkan/dimasuki dari aplikasi.
 */
object AuthPolicy {
    const val DASHBOARD = "dashboard"
    const val TRANSAKSI = "transaksi"
    const val KASIR = "kasir"
    const val LAPORAN = "laporan"

    const val GAJI = "gaji"
    const val PENGATURAN = "pengaturan"

    fun isKaryawan(user: UserSession): Boolean =
        user.role.equals("karyawan", ignoreCase = true)

    fun canAccess(user: UserSession, page: String): Boolean {
        // Admin memiliki seluruh permission.
        if (user.role.equals("admin", ignoreCase = true)) return true

        // Hard-block wajib untuk role karyawan.
        if (isKaryawan(user) && page in setOf(GAJI, PENGATURAN)) {
            return false
        }

        return user.can(page)
    }

    fun canAccessAnyExistingDestination(user: UserSession): Boolean =
        listOf(DASHBOARD, TRANSAKSI, KASIR, LAPORAN)
            .any { canAccess(user, it) }
}
