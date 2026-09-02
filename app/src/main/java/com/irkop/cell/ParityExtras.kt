package com.irkop.cell

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.core.AuthPolicy
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import com.irkop.cell.features.admin.CorrectionsScreen
import com.irkop.cell.features.admin.OperationsScreen
import com.irkop.cell.features.akun.AkunUangScreen
import com.irkop.cell.features.kasbon.KasbonDetailScreen
import com.irkop.cell.features.laporan.LaporanAnalyticsScreen
import com.irkop.cell.features.pelanggan.PelangganScreen
import com.irkop.cell.features.pengaturan.UserManagementScreen
import com.irkop.cell.features.service.ServiceHpScreen

@Composable
fun ParityExtrasScreen(user: UserSession, repo: Repository) {
    var tab by remember { mutableStateOf("pelanggan") }
    val tabs = buildList {
        if (AuthPolicy.canAccess(user, "pelanggan")) add("pelanggan" to "Pelanggan")
        if (AuthPolicy.canAccess(user, "kasbon")) add("kasbon" to "Kasbon")
        if (AuthPolicy.canAccess(user, "laporan_service_hp")) add("service" to "Service HP")
        if (user.role.equals("admin", true)) add("akun" to "Akun Uang")
        if (AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)) add("laporan" to "Laporan")
        if (user.role.equals("admin", true)) add("admin" to "User & Permission")
        if (user.role.equals("admin", true)) add("ops" to "Operasional")
        if (user.role.equals("admin", true)) add("koreksi" to "Koreksi")
    }
    LaunchedEffect(tabs) { if (tabs.none { it.first == tab }) tab = tabs.firstOrNull()?.first ?: "pelanggan" }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            tabs.forEach { (key, label) -> FilterChip(tab == key, { tab = key }, label = { Text(label) }) }
        }
        when (tab) {
            "pelanggan" -> PelangganScreen(repo)
            "kasbon" -> KasbonDetailScreen(repo)
            "service" -> ServiceHpScreen(repo)
            "akun" -> AkunUangScreen(repo)
            "laporan" -> LaporanAnalyticsScreen(repo)
            "admin" -> UserManagementScreen(repo)
            "ops" -> OperationsScreen(user, repo)
            "koreksi" -> CorrectionsScreen(repo)
        }
    }
}
