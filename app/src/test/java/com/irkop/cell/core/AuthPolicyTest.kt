package com.irkop.cell.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthPolicyTest {

    @Test
    fun adminCanAccessAllPages() {
        val user = UserSession(
            id = "1",
            nama = "Admin",
            username = "admin",
            role = "admin",
            permissions = emptyMap()
        )

        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.DASHBOARD))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.TRANSAKSI))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.KASIR))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.LAPORAN))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.GAJI))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.PENGATURAN))
    }

    @Test
    fun granularPermissionControlsNonAdminNavigation() {
        val user = UserSession(
            id = "2",
            nama = "Kasir",
            username = "kasir",
            role = "operator",
            permissions = mapOf(
                AuthPolicy.DASHBOARD to true,
                AuthPolicy.TRANSAKSI to true,
                AuthPolicy.KASIR to false,
                AuthPolicy.LAPORAN to false
            )
        )

        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.DASHBOARD))
        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.TRANSAKSI))
        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.KASIR))
        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.LAPORAN))
    }

    @Test
    fun karyawanIsAlwaysBlockedFromGajiAndPengaturan() {
        val user = UserSession(
            id = "3",
            nama = "Karyawan",
            username = "karyawan",
            role = "karyawan",
            permissions = mapOf(
                AuthPolicy.GAJI to true,
                AuthPolicy.PENGATURAN to true
            )
        )

        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.GAJI))
        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.PENGATURAN))
    }

    @Test
    fun permissionLookupIsCaseInsensitive() {
        val user = UserSession(
            id = "4",
            nama = "Operator",
            username = "operator",
            role = "operator",
            permissions = mapOf(
                "Transaksi" to true
            )
        )

        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.TRANSAKSI))
    }

    @Test
    fun karyawanCanStillUseExplicitlyGrantedNonRestrictedPage() {
        val user = UserSession(
            id = "5",
            nama = "Karyawan",
            username = "karyawan",
            role = "KARYAWAN",
            permissions = mapOf(
                AuthPolicy.DASHBOARD to true
            )
        )

        assertTrue(AuthPolicy.canAccess(user, AuthPolicy.DASHBOARD))
        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.GAJI))
        assertFalse(AuthPolicy.canAccess(user, AuthPolicy.PENGATURAN))
    }
}
