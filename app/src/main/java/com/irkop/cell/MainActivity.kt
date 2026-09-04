package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF131313)
private val S1 = Color(0xFF1B1B1C)
private val S2 = Color(0xFF202020)
private val S3 = Color(0xFF2A2A2A)
private val Txt = Color(0xFFE5E2E1)
private val Muted = Color(0xFFBCC9C5)
private val Primary = Color(0xFF70D8C8)
private val Green = Color(0xFF4EDEA3)
private val Blue = Color(0xFFADC6FF)
private val OnP = Color(0xFF003731)

private val AppColors = darkColorScheme(
    primary = Primary,
    onPrimary = OnP,
    secondary = Green,
    tertiary = Blue,
    background = Bg,
    onBackground = Txt,
    surface = Bg,
    onSurface = Txt,
    surfaceVariant = S3,
    onSurfaceVariant = Muted
)

private enum class Page {
    HOME, KASIR, REPORT, MORE, STOCK, SERVICE, KASBON, CUSTOMERS, EXPENSE, SALARY, SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColors) {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    var page by remember { mutableStateOf(Page.HOME) }
    var showTransaction by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Bg,
        bottomBar = { Nav(page = page, onNavigate = { page = it }) },
        floatingActionButton = {
            if (page in setOf(Page.HOME, Page.KASIR, Page.REPORT, Page.MORE)) {
                ExtendedFloatingActionButton(
                    onClick = { showTransaction = true },
                    containerColor = Primary,
                    contentColor = OnP,
                    shape = RoundedCornerShape(50.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Transaksi Baru", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (page) {
                Page.HOME -> Home(onNavigate = { page = it })
                Page.KASIR -> Basic("Kasir & Rekonsiliasi", "Sesi kasir hari ini", listOf(
                    "Saldo Kasir" to "Rp 0",
                    "Kas Tunai Laci" to "Rp 0",
                    "QRIS" to "Rp 0",
                    "DANA" to "Rp 0"
                ))
                Page.REPORT -> Basic("Laporan Hari Ini", "Ringkasan transaksi & arus kas", listOf(
                    "Omzet Hari Ini" to "Rp 0",
                    "Laba Kotor" to "Rp 0",
                    "Pengeluaran" to "Rp 0"
                ))
                Page.MORE -> More(onNavigate = { page = it })
                Page.STOCK -> Basic("Daftar Barang & Stok", "Inventaris produk konter", listOf(
                    "Charger Robot Fast Charge 20W" to "Stok 19 • Rp 55.000",
                    "LCD Samsung A12 Original Incell" to "Stok 4 • Rp 280.000",
                    "Kartu Perdana Telkomsel 10GB" to "Stok 25 • Rp 45.000"
                ))
                Page.SERVICE -> Basic("Laporan Service HP", "Tracking servis & diagnostik", listOf(
                    "Samsung A54" to "Ganti LCD • Menunggu Spare Part",
                    "iPhone 11" to "Ganti Battery • Siap Diambil",
                    "Redmi Note 10" to "IC Power • Diagnostik"
                ))
                Page.KASBON -> Basic("Buku Kasbon", "Hutang pelanggan & pembayaran", listOf(
                    "Total Piutang" to "Rp 0",
                    "Pelanggan Baru" to "Jatuh tempo hari ini",
                    "Pelanggan Lama" to "Pembayaran sebagian"
                ))
                Page.CUSTOMERS -> Basic("Manajemen Pelanggan", "Leaderboard & riwayat", listOf(
                    "Pelanggan Aktif" to "0",
                    "Leaderboard" to "0"
                ))
                Page.EXPENSE -> Basic("Catatan Pengeluaran", "Kas keluar toko", listOf(
                    "Pengeluaran Hari Ini" to "Rp 0"
                ))
                Page.SALARY -> Basic("Gaji Karyawan", "Administrasi gaji shift", listOf(
                    "Total Gaji Bulan Ini" to "Rp 0"
                ))
                Page.SETTINGS -> Basic("Pengaturan", "Konfigurasi outlet & aplikasi", listOf(
                    "Outlet" to "Konter Utama",
                    "Akun & Hak Akses" to "Permission mengikuti backend",
                    "Sinkronisasi" to "Cloud POS API"
                ))
            }
        }
    }

    if (showTransaction) {
        TxSheet(onDismiss = { showTransaction = false })
    }
}

@Composable
private fun Header(title: String, subtitle: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Txt, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = Muted)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Muted)
        }
    }
}

@Composable
private fun Home(onNavigate: (Page) -> Unit) {
    val quickActions = listOf(
        "Transaksi" to Page.KASIR,
        "Kasir" to Page.KASIR,
        "Kasbon" to Page.KASBON,
        "Laporan" to Page.REPORT,
        "Pelanggan" to Page.CUSTOMERS,
        "Produk" to Page.STOCK,
        "Service" to Page.SERVICE,
        "Akun Uang" to Page.KASIR
    )

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Header("Irkop Cell", "SESI KASIR: AKTIF") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Selamat datang 👋", color = Muted, fontSize = 18.sp)
                Text("Kelola toko lebih cepat hari ini", color = Txt, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Balance()
                Spacer(Modifier.height(20.dp))
                Text("Perlu Perhatian Segera", color = Txt, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Info("Stok Menipis", "0 produk", Green)
                    Info("Kasbon Jatuh Tempo", "0 pelanggan", Color(0xFFF59E0B))
                    Info("Servis Berjalan", "0 unit", Blue)
                }
                Spacer(Modifier.height(20.dp))
                Text("Menu Kasir Utama", color = Txt, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
            }
        }
        items(quickActions.chunked(3)) { row ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (name, target) ->
                    Quick(name, icon(name), Modifier.weight(1f)) { onNavigate(target) }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
        item {
            Column(Modifier.padding(16.dp)) {
                Spacer(Modifier.height(12.dp))
                Text("Aktivitas Terakhir", color = Txt, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Empty()
            }
        }
    }
}

@Composable
private fun Balance() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = S1),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(Modifier.height(190.dp)) {
            Column(
                Modifier.weight(1f).fillMaxHeight().background(Color(0xFF32A192)).padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Saldo Kasir", color = Color.White)
                Text("Rp 0", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("0 transaksi hari ini", color = Color.White)
                Text("Omzet Rp 0", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.weight(1f).fillMaxHeight().background(S3))
        }
    }
}

@Composable
private fun Info(title: String, value: String, valueColor: Color) {
    Card(
        Modifier.width(165.dp),
        colors = CardDefaults.cardColors(containerColor = S2),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = Txt, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(value, color = valueColor, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Quick(name: String, imageVector: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = S2),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(42.dp).background(Primary.copy(alpha = .1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector, contentDescription = null, tint = Primary)
            }
            Spacer(Modifier.height(7.dp))
            Text(name, color = Txt, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Empty() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = S1),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Primary, modifier = Modifier.size(34.dp))
            Text("Belum ada transaksi", color = Txt, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Transaksi baru akan muncul di sini", color = Muted)
        }
    }
}

@Composable
private fun Basic(title: String, subtitle: String, rows: List<Pair<String, String>>) {
    Column(Modifier.fillMaxSize()) {
        Header(title, subtitle)
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(rows) { (label, value) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = S2),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(label, color = Txt, fontWeight = FontWeight.Bold)
                            Text(value, color = Muted, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
                    }
                }
            }
        }
    }
}

@Composable
private fun More(onNavigate: (Page) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header("Lainnya", "Fitur operasional dan administrasi")
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val actions = listOf(
                "Pelanggan" to Page.CUSTOMERS,
                "Kasbon" to Page.KASBON,
                "Service HP" to Page.SERVICE,
                "Akun Uang" to Page.KASIR,
                "Produk & Operasional" to Page.STOCK,
                "Koreksi" to Page.REPORT
            )
            actions.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (name, target) ->
                        MoreCard(name, Modifier.weight(1f)) { onNavigate(target) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            MoreCard("Pengaturan", Modifier.fillMaxWidth()) { onNavigate(Page.SETTINGS) }
        }
    }
}

@Composable
private fun MoreCard(name: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier.height(128.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = S2),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon(name), contentDescription = null, tint = Primary)
            Text(name, color = Txt, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TxSheet(onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = S3,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Primary) }
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Transaksi Baru", color = Txt, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = customer,
                onValueChange = { customer = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nomor / pelanggan") },
                singleLine = true
            )
            Text("Produk / layanan", color = Txt, fontWeight = FontWeight.Bold)
            listOf("Pulsa 10K", "Pulsa 25K", "Data 10GB", "Token PLN").forEach { product ->
                FilterChip(
                    selected = selected == product,
                    onClick = { selected = product },
                    label = { Text(product) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = { if (selected.isNotBlank()) onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp)
            ) {
                Text("Bayar Sekarang")
            }
        }
    }
}

@Composable
private fun Nav(page: Page, onNavigate: (Page) -> Unit) {
    NavigationBar(containerColor = S1) {
        val items = listOf(
            Triple(Page.HOME, "Beranda", Icons.Default.Home),
            Triple(Page.KASIR, "Kasir", Icons.Default.PointOfSale),
            Triple(Page.REPORT, "Laporan", Icons.Default.BarChart),
            Triple(Page.MORE, "Lainnya", Icons.Default.MoreHoriz)
        )
        items.forEach { (target, label, imageVector) ->
            NavigationBarItem(
                selected = page == target,
                onClick = { onNavigate(target) },
                icon = { Icon(imageVector, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

private fun icon(name: String): ImageVector = when {
    name.contains("Kasir", true) -> Icons.Default.PointOfSale
    name.contains("Kasbon", true) -> Icons.Default.AccountBalanceWallet
    name.contains("Laporan", true) -> Icons.Default.BarChart
    name.contains("Pelanggan", true) -> Icons.Default.Groups
    name.contains("Produk", true) -> Icons.Default.Inventory2
    name.contains("Service", true) -> Icons.Default.Build
    name.contains("Akun", true) -> Icons.Default.AccountBalance
    name.contains("Pengaturan", true) -> Icons.Default.Settings
    else -> Icons.Default.ReceiptLong
}
