package com.irkop.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    IrkopTheme {
        ParityExtrasContent(user, repo)
    }
}

@Composable
private fun ParityExtrasContent(user: UserSession, repo: Repository) {
    var tab by remember { mutableStateOf("pelanggan") }
    val tabs = buildList {
        if (AuthPolicy.canAccess(user, "pelanggan")) add("pelanggan" to ("Pelanggan" to Icons.Default.People))
        if (AuthPolicy.canAccess(user, "kasbon")) add("kasbon" to ("Kasbon" to Icons.Default.AccountBalanceWallet))
        if (AuthPolicy.canAccess(user, "laporan_service_hp")) add("service" to ("Service HP" to Icons.Default.Build))
        if (user.role.equals("admin", true)) add("akun" to ("Akun Uang" to Icons.Default.AccountBalance))
        if (AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)) add("laporan" to ("Laporan" to Icons.Default.Assessment))
        if (user.role.equals("admin", true)) add("admin" to ("User" to Icons.Default.Group))
        if (user.role.equals("admin", true)) add("ops" to ("Operasional" to Icons.Default.Inventory2))
        if (user.role.equals("admin", true)) add("koreksi" to ("Koreksi" to Icons.Default.EditNote))
    }

    LaunchedEffect(tabs) {
        if (tabs.none { it.first == tab }) {
            tab = tabs.firstOrNull()?.first ?: "pelanggan"
        }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Menu Lainnya", style = MaterialTheme.typography.headlineMedium)
        Text("Semua fitur operasional dalam satu tempat", style = MaterialTheme.typography.bodyMedium)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
        ) {
            items(tabs.size) { index ->
                val (key, meta) = tabs[index]
                val (label, icon) = meta
                FilterChip(
                    selected = tab == key,
                    onClick = { tab = key },
                    label = { Text(label) },
                    leadingIcon = { Icon(icon, null) },
                )
            }
        }

        HorizontalDivider()

        Box(Modifier.fillMaxSize()) {
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
}
