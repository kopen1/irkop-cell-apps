package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val AppScheme = darkColorScheme(
    primary = Color(0xFF70D8C8),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF32A192),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Color(0xFF4EDEA3),
    tertiary = Color(0xFFADC6FF),
    background = Color(0xFF131313),
    onBackground = Color(0xFFE5E2E1),
    surface = Color(0xFF131313),
    onSurface = Color(0xFFE5E2E1),
    surfaceVariant = Color(0xFF353535),
    onSurfaceVariant = Color(0xFFBCC9C5),
    outline = Color(0xFF879390),
    error = Color(0xFFFFB4AB)
)

private val SurfaceLow = Color(0xFF1B1B1C)
private val SurfaceContainer = Color(0xFF202020)
private val SurfaceHigh = Color(0xFF2A2A2A)
private val Muted = Color(0xFFBCC9C5)
private val Success = Color(0xFF4EDEA3)

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = AppScheme.background, contentColor = AppScheme.onBackground) {
                    AutoLoginScreen()
                }
            }
        }
    }
}

@Composable
private fun AutoLoginScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember(context) { ApiClient(context) }
    var message by remember { mutableStateOf("Menyiapkan sesi demo…") }
    var loggedIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loginDemo() {
        scope.launch {
            message = "Login demo…"
            try {
                api.login("demo", "demodemo")
                loggedIn = true
            } catch (e: Exception) {
                message = e.message ?: "Login demo gagal"
            }
        }
    }

    LaunchedEffect(Unit) { loginDemo() }

    if (loggedIn) {
        DashboardScreen()
    } else {
        Surface(color = AppScheme.background, contentColor = AppScheme.onBackground) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge, color = AppScheme.onBackground)
                Text("Login otomatis akun demo", color = Muted)
                CircularProgressIndicator(color = AppScheme.primary)
                Text(message, color = AppScheme.onBackground)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = AppScheme.background,
        contentColor = AppScheme.onBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = AppScheme.primary,
                contentColor = AppScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Transaksi Baru")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceLow, contentColor = AppScheme.onSurface) {
                val items = listOf(
                    Triple("Beranda", Icons.Default.PointOfSale, 0),
                    Triple("Kasir", Icons.Default.ReceiptLong, 1),
                    Triple("Laporan", Icons.Default.AccountBalanceWallet, 2),
                    Triple("Lainnya", Icons.Default.MoreHoriz, 3)
                )
                items.forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeContent(padding)
            else -> SimpleSection(selectedTab, padding)
        }
    }
}

@Composable
private fun HomeContent(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Irkop Cell", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Success, CircleShape))
                        Spacer(Modifier.width(7.dp))
                        Text("SESI KASIR: AKTIF", style = MaterialTheme.typography.labelMedium, color = Success, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = { }) { Icon(Icons.Default.Search, "Cari") }
                IconButton(onClick = { }) { Icon(Icons.Default.NotificationsNone, "Notifikasi") }
                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = AppScheme.primaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Text("D", fontWeight = FontWeight.Bold, color = AppScheme.onPrimaryContainer) }
                }
            }
        }

        item { SessionCard() }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Penjualan Hari Ini", "Rp 4.250.000", "+12,8%", Modifier.weight(1f))
                MetricCard("Transaksi", "38", "Hari ini", Modifier.weight(1f))
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceLow), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Perlu Perhatian Segera", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("3 item", color = AppScheme.error, style = MaterialTheme.typography.labelMedium)
                    }
                    AttentionRow(Icons.Default.Inventory2, "Stok menipis", "5 produk perlu restock", AppScheme.error)
                    AttentionRow(Icons.Default.AccountBalanceWallet, "Kasir belum direkonsiliasi", "Selesaikan sebelum tutup sesi", Color(0xFFFFC56D))
                    AttentionRow(Icons.Default.Build, "2 servis menunggu", "Periksa status pengerjaan", AppScheme.tertiary)
                }
            }
        }

        item {
            Text("Menu Kasir Utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickMenu(Icons.Default.PointOfSale, "Kasir", Modifier.weight(1f))
                QuickMenu(Icons.Default.Inventory2, "Stok", Modifier.weight(1f))
                QuickMenu(Icons.Default.Build, "Servis", Modifier.weight(1f))
                QuickMenu(Icons.Default.People, "Pelanggan", Modifier.weight(1f))
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Lihat semua", color = AppScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    ActivityRow("Pulsa Telkomsel 50K", "Dina • 2 menit lalu", "Rp 52.000")
                    ActivityRow("Servis LCD iPhone", "Budi • 18 menit lalu", "Rp 450.000")
                    ActivityRow("Top Up DANA", "Andi • 31 menit lalu", "Rp 100.000")
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceLow), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = AppScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ReceiptLong, null, tint = AppScheme.onPrimaryContainer) }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Printer Kasir", fontWeight = FontWeight.SemiBold)
                        Text("Siap digunakan", style = MaterialTheme.typography.bodySmall, color = Success)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun SessionCard() {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceContainer), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sesi Kasir Aktif", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Demo Kasir • Dibuka 08:12", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                Surface(shape = RoundedCornerShape(50), color = Color(0xFF123C31)) {
                    Text("AKTIF", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Success, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saldo awal", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text("Rp 1.500.000", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saldo berjalan", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Text("Rp 5.750.000", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, note: String, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = SurfaceLow), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Muted)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(note, style = MaterialTheme.typography.labelSmall, color = Success)
        }
    }
}

@Composable
private fun AttentionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = SurfaceHigh) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint) }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Icon(Icons.Default.ArrowForward, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun QuickMenu(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier) {
    Card(modifier = modifier.clickable { }, colors = CardDefaults.cardColors(containerColor = SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(icon, null, tint = AppScheme.primary, modifier = Modifier.size(25.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ActivityRow(title: String, subtitle: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(38.dp), shape = RoundedCornerShape(11.dp), color = SurfaceHigh) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.ReceiptLong, null, tint = AppScheme.primary, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Text(amount, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SimpleSection(index: Int, padding: PaddingValues) {
    val title = when (index) { 1 -> "Kasir"; 2 -> "Laporan"; else -> "Lainnya" }
    val icon = when (index) { 1 -> Icons.Default.PointOfSale; 2 -> Icons.Default.AccountBalanceWallet; else -> Icons.Default.Settings }
    Surface(modifier = Modifier.fillMaxSize().padding(padding), color = AppScheme.background, contentColor = AppScheme.onBackground) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = AppScheme.primary, modifier = Modifier.size(36.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Menu ini sudah terhubung ke navigasi utama. Modul detail akan menggunakan alur API Irkop Cell.", color = Muted)
        }
    }
}
