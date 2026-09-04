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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray

private val AppColors = darkColorScheme(
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
private val SurfaceMid = Color(0xFF202020)
private val SurfaceHigh = Color(0xFF2A2A2A)
private val Muted = Color(0xFFBCC9C5)
private val Success = Color(0xFF4EDEA3)

private enum class Page {
    HOME, KASIR, TRANSAKSI, LAPORAN, STOK, KATEGORI, SERVICE,
    KASBON, PELANGGAN, PENGELUARAN, GAJI, LAINNYA, PENGATURAN
}

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = AppColors) { AutoLoginScreen() } }
    }
}

@Composable
private fun AutoLoginScreen() {
    val context = LocalContext.current
    val api = remember(context) { ApiClient(context) }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun loginDemo() {
        scope.launch {
            error = ""
            try {
                api.login("demo", "demodemo")
                ready = true
            } catch (e: Exception) {
                error = e.message ?: "Login gagal"
            }
        }
    }

    LaunchedEffect(Unit) { loginDemo() }

    if (ready) {
        AppShell(api)
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(AppColors.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                CircularProgressIndicator(color = AppColors.primary)
                Text(if (error.isBlank()) "Menyiapkan sesi kasir…" else error, color = if (error.isBlank()) Muted else AppColors.error)
                if (error.isNotBlank()) Button(onClick = ::loginDemo) { Text("Coba Lagi") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(api: ApiClient) {
    var page by remember { mutableStateOf(Page.HOME) }
    var showTransaction by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            NavigationBar(containerColor = SurfaceLow) {
                NavigationBarItem(page == Page.HOME, { page = Page.HOME }, icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") })
                NavigationBarItem(page == Page.KASIR, { page = Page.KASIR }, icon = { Icon(Icons.Default.PointOfSale, "Kasir") }, label = { Text("Kasir") })
                NavigationBarItem(page == Page.LAPORAN, { page = Page.LAPORAN }, icon = { Icon(Icons.Default.Assessment, "Laporan") }, label = { Text("Laporan") })
                NavigationBarItem(page == Page.LAINNYA, { page = Page.LAINNYA }, icon = { Icon(Icons.Default.MoreHoriz, "Lainnya") }, label = { Text("Lainnya") })
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTransaction = true },
                containerColor = AppColors.primary,
                contentColor = AppColors.onPrimary
            ) { Icon(Icons.Default.Add, "Transaksi Baru") }
        }
    ) { padding ->
        when (page) {
            Page.HOME -> HomePage(padding, { showSearch = true }) { page = it }
            Page.KASIR -> CashierPage(padding, api)
            Page.TRANSAKSI -> TransactionPage(padding, api) { page = Page.HOME }
            Page.LAPORAN -> ReportPage(padding, api)
            Page.STOK -> StockPage(padding) { page = Page.KATEGORI }
            Page.KATEGORI -> CategoryPage(padding)
            Page.SERVICE -> ServicePage(padding)
            Page.KASBON -> KasbonPage(padding)
            Page.PELANGGAN -> CustomerPage(padding)
            Page.PENGELUARAN -> ExpensePage(padding)
            Page.GAJI -> PayrollPage(padding)
            Page.LAINNYA -> MorePage(padding) { page = it }
            Page.PENGATURAN -> SettingsPage(padding)
        }
    }

    if (showTransaction) TransactionSheet { showTransaction = false }
    if (showSearch) SearchDialog { showSearch = false }
}

@Composable
private fun Header(title: String, subtitle: String? = null, back: (() -> Unit)? = null, add: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Kembali") }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (add != null) IconButton(onClick = add) { Icon(Icons.Default.Add, "Tambah") }
    }
}

@Composable
private fun Metric(title: String, value: String, note: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Muted, style = MaterialTheme.typography.labelSmall)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(note, color = Success, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color = Success) {
    Surface(color = SurfaceHigh, shape = CircleShape) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = AppColors.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomePage(padding: PaddingValues, search: () -> Unit, go: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Irkop Cell", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Sesi Kasir: Aktif", color = Success, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = search) { Icon(Icons.Default.Search, "Cari") }
                Surface(Modifier.size(40.dp), CircleShape, AppColors.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("D", fontWeight = FontWeight.Bold) } }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(SurfaceMid), shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable { go(Page.KASIR) }) {
                Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("Sesi Kasir Aktif", fontWeight = FontWeight.Bold); Text("Siti Aminah • Shift Pagi • 08:00 WIB", color = Muted, style = MaterialTheme.typography.bodySmall) }
                        StatusPill("AKTIF")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Metric("Saldo Awal", "Rp 1.500.000", "Opening", Modifier.weight(1f))
                        Metric("Saldo Berjalan", "Rp 5.750.000", "Real-time", Modifier.weight(1f))
                    }
                    Text("Kelola sesi & rekonsiliasi", color = AppColors.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Metric("Saldo Kas Laci", "Rp 1.450.000", "Real-time", Modifier.weight(1f))
                Metric("Omzet Hari Ini", "Rp 7.420.000", "82% target", Modifier.weight(1f))
                Metric("Transaksi", "52", "Nota", Modifier.weight(1f))
            }
        }
        item { Text("Perlu Perhatian Segera", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AttentionRow("3 Servis Siap Diambil", "Kirim notifikasi WA", Icons.Default.Build) { go(Page.SERVICE) }
                AttentionRow("Kasbon Jatuh Tempo Hari Ini", "2 pelanggan • Rp 320.000", Icons.Default.ReceiptLong) { go(Page.KASBON) }
                AttentionRow("Selisih Belum Ditoleransi", "Shift malam • -Rp 15.000", Icons.Default.AccountBalanceWallet) { go(Page.KASIR) }
            }
        }
        item { Text("Menu Kasir Utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(Modifier.weight(1f), Icons.Default.Inventory2, "Stok") { go(Page.STOK) }
                QuickAction(Modifier.weight(1f), Icons.Default.ReceiptLong, "Kasbon") { go(Page.KASBON) }
                QuickAction(Modifier.weight(1f), Icons.Default.People, "Pelanggan") { go(Page.PELANGGAN) }
                QuickAction(Modifier.weight(1f), Icons.Default.Wallet, "Pengeluaran") { go(Page.PENGELUARAN) }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(Modifier.weight(1f), Icons.Default.Build, "Servis") { go(Page.SERVICE) }
                QuickAction(Modifier.weight(1f), Icons.Default.Group, "Gaji") { go(Page.GAJI) }
                QuickAction(Modifier.weight(1f), Icons.Default.Assessment, "Laporan") { go(Page.LAPORAN) }
                QuickAction(Modifier.weight(1f), Icons.Default.Settings, "Setting") { go(Page.PENGATURAN) }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(SurfaceMid), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Aktivitas Terakhir", fontWeight = FontWeight.Bold)
                    listOf("Paket Data Telkomsel 50GB • Rp 105.000", "Ganti LCD Samsung A12 • Rp 380.000", "Top Up DANA Saldo 100k • Rp 102.500", "Pulsa Indosat Reguler 25k • Rp 26.500").forEach { Text(it, Modifier.padding(top = 10.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AttentionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(Modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppColors.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }
            Icon(Icons.Default.ArrowForward, null, tint = Muted)
        }
    }
}

@Composable
private fun CashierPage(padding: PaddingValues, api: ApiClient) {
    var status by remember { mutableStateOf("AKTIF") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/kasir/current") }.onSuccess { status = it.optString("status", "AKTIF").uppercase() } }
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Kasir & Rekonsiliasi", "Sesi kasir aktif") }
        item { Card(colors = CardDefaults.cardColors(SurfaceMid), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row { Text("Sesi Kasir", Modifier.weight(1f), fontWeight = FontWeight.Bold); StatusPill(status) }; Text("Kasir 01 • Shift Pagi", color = Muted); Text("Saldo berjalan", color = Muted); Text("Rp 4.820.000", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } } }
        item { Text("Master Akun & E-Wallet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(listOf("Kas Tunai di Laci" to "Rp 1.450.000", "OrderKuota" to "Rp 2.890.000", "DANA Merchant" to "Rp 1.150.000", "SeaBank Operasional" to "Rp 3.420.000", "ShopeePay / QRIS" to "Rp 780.000")) { (name, balance) -> Card(colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountBalanceWallet, null, tint = AppColors.primary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold); Text("Saldo realtime", color = Muted, style = MaterialTheme.typography.bodySmall) }; Text(balance, fontWeight = FontWeight.Bold) } } }
        item { Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Sync, null); Spacer(Modifier.width(8.dp)); Text("Sinkronkan Saldo") } }
        item { OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Rekonsiliasi & Tutup Sesi") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionSheet(close: () -> Unit) {
    var type by remember { mutableStateOf("Transaksi Biasa") }
    var amount by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = close, containerColor = SurfaceMid) {
        LazyColumn(contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            item { Header("Transaksi Baru", "POS Terminal Online") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(type == "Transaksi Biasa", { type = "Transaksi Biasa" }, label = { Text("Transaksi Biasa") }); FilterChip(type == "Servis HP", { type = "Servis HP" }, label = { Text("Servis HP") }) } }
            item { Text("Akses Cepat", fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { QuickAction(Modifier.weight(1f), Icons.Default.QrCodeScanner, "Token PLN") {}; QuickAction(Modifier.weight(1f), Icons.Default.Wifi, "Paket Data") {}; QuickAction(Modifier.weight(1f), Icons.Default.Wallet, "E-Wallet") {}; QuickAction(Modifier.weight(1f), Icons.Default.Build, "Servis") {} } }
            item { OutlinedTextField(amount, { amount = it }, label = { Text("Total Pembayaran (Rp)") }, modifier = Modifier.fillMaxWidth()) }
            item { Text("Metode Pembayaran", fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Tunai", "Transfer", "Bon", "Split Bayar").forEach { FilterChip(false, {}, label = { Text(it) }) } } }
            item { OutlinedTextField("", {}, label = { Text("Pelanggan / Nomor WhatsApp") }, modifier = Modifier.fillMaxWidth()) }
            item { Button(onClick = close, modifier = Modifier.fillMaxWidth()) { Text("Konfirmasi Transaksi") } }
        }
    }
}

@Composable
private fun TransactionPage(padding: PaddingValues, api: ApiClient, back: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var ids by remember { mutableStateOf(listOf("TX-20260905-001", "TX-20260905-002", "TX-20260905-003")) }
    LaunchedEffect(Unit) { runCatching { api.get("/api/transaksi", mapOf("date" to "2026-09-05", "limit" to "100")) }.onSuccess { root -> val a = root.optJSONArray("items") ?: JSONArray(); val loaded = (0 until a.length()).mapNotNull { a.optJSONObject(it)?.optString("id")?.takeIf(String::isNotBlank) }; if (loaded.isNotEmpty()) ids = loaded } }
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Transaksi", "Riwayat transaksi kasir", back) }
        item { OutlinedTextField(query, { query = it }, label = { Text("Cari transaksi") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth()) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Semua", "Tunai", "Transfer", "PPOB").forEach { FilterChip(false, {}, label = { Text(it) }) } } }
        items(ids.filter { query.isBlank() || it.contains(query, true) }) { id -> Card(colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ReceiptLong, null, tint = AppColors.primary); Spacer(Modifier.width(10.dp)); Text(id, Modifier.weight(1f), fontWeight = FontWeight.Bold); IconButton(onClick = {}) { Icon(Icons.Default.Print, "Cetak") }; IconButton(onClick = {}) { Icon(Icons.Default.Delete, "Hapus", tint = AppColors.error) } } } }
    }
}

@Composable
private fun ReportPage(padding: PaddingValues, api: ApiClient) {
    var omzet by remember { mutableStateOf("Rp 7.420.000") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/laporan/bulan", mapOf("bulan" to "2026-09")) }.onSuccess { omzet = "Rp ${it.optLong("omzet", 7420000)}" } }
    SimplePage(padding, "Laporan Kasir", "Hari Ini", listOf("Total Omzet" to omzet, "Total Transaksi" to "52", "Laba Bersih Est." to "Rp 1,15 Jt", "Kasbon" to "Rp 1,84 Jt"), listOf("Cetak PDF", "Export CSV"))
}

@Composable
private fun StockPage(padding: PaddingValues, openCategories: () -> Unit) {
    SimplePage(padding, "Daftar Barang & Stok", "142 Produk • 3 SKU kritis", listOf("Total Aset Fisik" to "Rp 28.450.000", "Margin Rata-rata" to "34%"), listOf("Tambah Produk", "Kelola Kategori"))
}

@Composable
private fun CategoryPage(padding: PaddingValues) {
    SimpleListPage(padding, "Kelola Kategori", listOf("Aksesoris HP • 45 Produk", "Sparepart & LCD • 28 Produk", "Kartu Perdana & Kuota • 32 Produk", "Voucher & PPOB • 19 Produk", "Jasa Servis Hardware • 14 Layanan"))
}

@Composable
private fun ServicePage(padding: PaddingValues) {
    SimpleListPage(padding, "Laporan Servis HP", listOf("SRV-2024-089 • Redmi Note 11 • Proses • Rp 380.000", "SRV-2024-085 • iPhone 11 • Siap Diambil • Rp 450.000", "SRV-2024-091 • Samsung A52 • Baru • Est. Rp 650.000"))
}

@Composable
private fun KasbonPage(padding: PaddingValues) {
    SimpleListPage(padding, "Buku Kasbon", listOf("KB-882 • Pak RT Wawan • Sisa Rp 270.000 • Jatuh Tempo Hari Ini", "KB-879 • Mas Dimas • Rp 450.000 • Belum Lunas", "KB-875 • Bu Sri • Rp 180.000 • Belum Lunas", "KB-860 • Rudi • LUNAS"))
}

@Composable
private fun CustomerPage(padding: PaddingValues) {
    SimpleListPage(padding, "Manajemen Pelanggan", listOf("Dimas Wahyudi • 0812-4455-6677 • Rp 4.850.000 • 38 Transaksi", "Siti Wulandari • 0857-9900-1122 • Rp 2.340.000 • 24 Transaksi", "Haji Wawan • 0813-8877-6655 • Jatuh Tempo 3 Hari"))
}

@Composable
private fun ExpensePage(padding: PaddingValues) {
    SimpleListPage(padding, "Catatan Pengeluaran", listOf("Kulakan Tempered Glass & Kabel Data • Rp 250.000", "Makan Siang Kasir • Rp 50.000", "Kertas Thermal & Nota • Rp 80.000", "Tagihan Listrik PLN • Rp 450.000", "Wifi Counter • Rp 200.000"))
}

@Composable
private fun PayrollPage(padding: PaddingValues) {
    SimpleListPage(padding, "Gaji Karyawan & Shift", listOf("Siti Aminah • Kasir • Total Rp 2.480.000", "Doni Prasetyo • Teknisi • Komisi Rp 1.680.000", "Budi Santoso • Kasir • Bersih Rp 1.640.000"))
}

@Composable
private fun MorePage(padding: PaddingValues, go: (Page) -> Unit) {
    val modules = listOf("Daftar Barang & Stok" to Page.STOK, "Kelola Kategori" to Page.KATEGORI, "Laporan Servis HP" to Page.SERVICE, "Buku Kasbon" to Page.KASBON, "Data Pelanggan" to Page.PELANGGAN, "Catatan Pengeluaran" to Page.PENGELUARAN, "Gaji Karyawan" to Page.GAJI, "Pengaturan" to Page.PENGATURAN)
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Lainnya", "Semua modul Irkop Cell") }
        items(modules) { (name, target) -> Card(Modifier.clickable { go(target) }, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChevronRight, null, tint = AppColors.primary); Spacer(Modifier.width(12.dp)); Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }
    }
}

@Composable
private fun SettingsPage(padding: PaddingValues) {
    SimpleListPage(padding, "Pengaturan", listOf("Tampilan & Mode Gelap • OLED Dark", "NotifHook • Gateway", "Manajemen User & Hak Akses", "Informasi Outlet & Printer", "Master Akun Uang", "Console Log / System Log"))
}

@Composable
private fun SimplePage(padding: PaddingValues, title: String, subtitle: String, metrics: List<Pair<String, String>>, actions: List<String>) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header(title, subtitle) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { metrics.take(2).forEach { (a, b) -> Metric(a, b, "Real-time", Modifier.weight(1f)) } } }
        if (metrics.size > 2) item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { metrics.drop(2).take(2).forEach { (a, b) -> Metric(a, b, "Ringkasan", Modifier.weight(1f)) } } }
        item { OutlinedTextField("", {}, label = { Text("Cari / filter data") }, modifier = Modifier.fillMaxWidth()) }
        items(actions) { action -> Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text(action) } }
    }
}

@Composable
private fun SimpleListPage(padding: PaddingValues, title: String, rows: List<String>) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header(title, "Irkop Cell") }
        item { OutlinedTextField("", {}, label = { Text("Cari") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth()) }
        items(rows) { row -> Card(colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(row, fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedButton(onClick = {}) { Text("Detail") }; Button(onClick = {}) { Text("Kelola") } } } } }
        item { Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Tambah Baru") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDialog(close: () -> Unit) {
    var query by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("Cari di Irkop Cell") }, text = { OutlinedTextField(query, { query = it }, label = { Text("Produk, transaksi, pelanggan, kasbon…") }) }, confirmButton = { Button(onClick = close) { Text("Cari") } }, dismissButton = { TextButton(onClick = close) { Text("Batal") } })
}
