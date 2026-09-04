package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Colors = darkColorScheme(
    primary = Color(0xFF70D8C8), onPrimary = Color(0xFF06332E),
    primaryContainer = Color(0xFF32A192), onPrimaryContainer = Color(0xFFE5FFF9),
    secondary = Color(0xFF4EDEA3), onSecondary = Color(0xFF062B1C), tertiary = Color(0xFFADC6FF),
    background = Color(0xFF202020), onBackground = Color.White,
    surface = Color(0xFF202020), onSurface = Color.White,
    surfaceVariant = Color(0xFF353535), onSurfaceVariant = Color(0xFFD7E0DC),
    outline = Color(0xFFAAB7B3), outlineVariant = Color(0xFF596460),
    error = Color(0xFFFFB4AB), onError = Color(0xFF3B0805)
)
private val CardBg = Color(0xFF2A2A2A)
private val CardBg2 = Color(0xFF303030)
private val CardBg3 = Color(0xFF383838)
private val Muted = Color(0xFFD7E0DC)
private val Good = Color(0xFF4EDEA3)
private enum class Page { HOME, KASIR, TRANSAKSI, LAPORAN, STOK, KATEGORI, SERVICE, KASBON, PELANGGAN, PENGELUARAN, GAJI, LAINNYA, PENGATURAN }

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme(colorScheme = Colors) { AutoLogin() } } }
}

@Composable
private fun AutoLogin() {
    val context = LocalContext.current
    val api = remember(context) { ApiClient(context) }
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    fun login() = scope.launch { error = ""; try { api.login("demo", "demodemo"); ready = true } catch (e: Exception) { error = e.message ?: "Login gagal" } }
    LaunchedEffect(Unit) { login() }
    if (ready) AppShell(api) else Box(Modifier.fillMaxSize().background(Colors.background), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Irkop Cell", color = Colors.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            CircularProgressIndicator(color = Colors.primary)
            Text(if (error.isBlank()) "Menyiapkan sesi kasir…" else error, color = if (error.isBlank()) Muted else Colors.error)
            if (error.isNotBlank()) Button(onClick = { login() }) { Text("Coba Lagi") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(api: ApiClient) {
    var page by remember { mutableStateOf(Page.HOME) }
    var stack by remember { mutableStateOf(listOf<Page>()) }
    var sheet by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf(false) }
    fun open(target: Page) { if (target != page) { stack = stack + page; page = target } }
    fun root(target: Page) { page = target; stack = emptyList() }
    fun back() { if (stack.isNotEmpty()) { page = stack.last(); stack = stack.dropLast(1) } else if (page != Page.HOME) page = Page.HOME }
    BackHandler(enabled = true) { back() }
    Scaffold(
        containerColor = Colors.background,
        bottomBar = { NavigationBar(containerColor = Color(0xFF292929)) {
            NavItem(page == Page.HOME, { root(Page.HOME) }, Icons.Default.Home, "Beranda")
            NavItem(page == Page.KASIR, { root(Page.KASIR) }, Icons.Default.PointOfSale, "Kasir")
            NavItem(page == Page.LAPORAN, { root(Page.LAPORAN) }, Icons.Default.Assessment, "Laporan")
            NavItem(page == Page.LAINNYA, { root(Page.LAINNYA) }, Icons.Default.MoreHoriz, "Lainnya")
        } },
        floatingActionButton = { FloatingActionButton(onClick = { sheet = true }, containerColor = Colors.primary, contentColor = Colors.onPrimary) { Icon(Icons.Default.Add, "Transaksi Baru") } }
    ) { pad ->
        when (page) {
            Page.HOME -> HomePage(pad, { search = true }, ::open)
            Page.KASIR -> CashierPage(pad, api, ::back)
            Page.TRANSAKSI -> TransactionPage(pad, api, ::back)
            Page.LAPORAN -> ReportPage(pad, api, ::back)
            Page.STOK -> StockPage(pad, ::back, ::open)
            Page.KATEGORI -> SimpleListPage(pad, "Kelola Kategori", listOf("Aksesoris HP", "Sparepart & LCD", "Kartu Perdana & Kuota", "Voucher & PPOB", "Jasa Servis Hardware"), ::back)
            Page.SERVICE -> SimpleListPage(pad, "Laporan Servis HP", listOf("SRV-2024-089 • Redmi Note 11 • Proses", "SRV-2024-085 • iPhone 11 • Siap Diambil", "SRV-2024-091 • Samsung A52 • Baru"), ::back)
            Page.KASBON -> KasbonPage(pad, api, ::back)
            Page.PELANGGAN -> SimpleListPage(pad, "Data Pelanggan", listOf("Dimas Wahyudi • 0812-4455-6677", "Siti Wulandari • 0857-9900-1122", "Haji Wawan • 0813-8877-6655"), ::back)
            Page.PENGELUARAN -> SimpleListPage(pad, "Catatan Pengeluaran", listOf("Kulakan aksesoris • Rp 250.000", "Makan siang kasir • Rp 50.000", "Kertas thermal • Rp 80.000", "Listrik PLN • Rp 450.000"), ::back)
            Page.GAJI -> PayrollPage(pad, api, ::back)
            Page.LAINNYA -> MorePage(pad, ::open, ::back)
            Page.PENGATURAN -> SettingsPage(pad, ::back)
        }
    }
    if (sheet) TransactionSheet(api) { sheet = false }
    if (search) SearchDialog { search = false }
}

@Composable private fun NavItem(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(selected, onClick, icon = { Icon(icon, null) }, label = { Text(label) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Colors.onPrimary, selectedTextColor = Colors.primary, indicatorColor = Color(0xFF454545), unselectedIconColor = Muted, unselectedTextColor = Muted))
}

@Composable private fun Header(title: String, subtitle: String? = null, back: (() -> Unit)? = null, add: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Kembali", tint = Colors.onSurface) }
        Column(Modifier.weight(1f)) { Text(title, color = Colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); if (subtitle != null) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodyMedium) }
        if (add != null) IconButton(onClick = add) { Icon(Icons.Default.Add, "Tambah", tint = Colors.onSurface) }
    }
}

@Composable private fun Metric(title: String, value: String, note: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, color = Muted, style = MaterialTheme.typography.labelSmall); Text(value, color = Colors.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium); Text(note, color = Good, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } }
}

@Composable private fun Status(text: String, good: Boolean = true) { Surface(color = CardBg3, shape = CircleShape) { Text(text, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = if (good) Good else Colors.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) } }

@Composable private fun Quick(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) { Icon(icon, null, tint = Colors.primary, modifier = Modifier.size(25.dp)); Text(label, color = Colors.onSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } }
}

@Composable private fun HomePage(pad: PaddingValues, search: () -> Unit, open: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Irkop Cell", color = Colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Sesi Kasir: Aktif", color = Good, fontWeight = FontWeight.Bold) }; IconButton(onClick = search) { Icon(Icons.Default.Search, "Cari", tint = Colors.onSurface) }; Surface(Modifier.size(42.dp), CircleShape, Colors.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("D", color = Colors.onPrimaryContainer, fontWeight = FontWeight.Bold) } } } }
        item { Card(Modifier.clickable { open(Page.KASIR) }, colors = CardDefaults.cardColors(CardBg2), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Sesi Kasir", color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Kasir 01 • Shift Pagi", color = Muted) }; Status("AKTIF") }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Saldo Awal", "Rp 1.500.000", "Opening", Modifier.weight(1f)); Metric("Saldo Berjalan", "Rp 5.750.000", "Real-time", Modifier.weight(1f)) }; Text("Kelola sesi & rekonsiliasi →", color = Colors.primary, fontWeight = FontWeight.Bold) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Saldo Kas Laci", "Rp 1.450.000", "Real-time", Modifier.weight(1f)); Metric("Omzet Hari Ini", "Rp 7.420.000", "82% target", Modifier.weight(1f)); Metric("Transaksi", "52", "Nota", Modifier.weight(1f)) } }
        item { Text("Perlu Perhatian Segera", color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { AlertRow("3 Servis Siap Diambil", "Kirim notifikasi WhatsApp", Icons.Default.Build) { open(Page.SERVICE) }; AlertRow("Kasbon Jatuh Tempo Hari Ini", "2 pelanggan • Rp 320.000", Icons.Default.ReceiptLong) { open(Page.KASBON) }; AlertRow("Selisih Belum Ditoleransi", "Shift malam • -Rp 15.000", Icons.Default.AccountBalanceWallet) { open(Page.KASIR) } } }
        item { Text("Menu Kasir Utama", color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Quick(Modifier.weight(1f), Icons.Default.Inventory2, "Stok") { open(Page.STOK) }; Quick(Modifier.weight(1f), Icons.Default.ReceiptLong, "Kasbon") { open(Page.KASBON) }; Quick(Modifier.weight(1f), Icons.Default.People, "Pelanggan") { open(Page.PELANGGAN) }; Quick(Modifier.weight(1f), Icons.Default.Wallet, "Pengeluaran") { open(Page.PENGELUARAN) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Quick(Modifier.weight(1f), Icons.Default.Build, "Servis") { open(Page.SERVICE) }; Quick(Modifier.weight(1f), Icons.Default.Group, "Gaji") { open(Page.GAJI) }; Quick(Modifier.weight(1f), Icons.Default.Assessment, "Laporan") { open(Page.LAPORAN) }; Quick(Modifier.weight(1f), Icons.Default.Settings, "Pengaturan") { open(Page.PENGATURAN) } } }
        item { Card(colors = CardDefaults.cardColors(CardBg2), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(16.dp)) { Text("Aktivitas Terakhir", color = Colors.onSurface, fontWeight = FontWeight.Bold); listOf("Paket Data Telkomsel 50GB • Rp 105.000", "Ganti LCD Samsung A12 • Rp 380.000", "Top Up DANA 100k • Rp 102.500").forEach { Text(it, Modifier.padding(top = 11.dp), color = Colors.onSurface) } } } }
    }
}

@Composable private fun AlertRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) { Card(Modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Colors.primary, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = Colors.onSurface, fontWeight = FontWeight.Bold); Text(subtitle, color = Muted) }; Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }

@Composable private fun CashierPage(pad: PaddingValues, api: ApiClient, back: () -> Unit) {
    var status by remember { mutableStateOf("MEMUAT") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/kasir/current") }.onSuccess { status = it.optString("status", "AKTIF").uppercase() } }
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Kasir & Rekonsiliasi", "Sesi kasir aktif", back) }
        item { Card(colors = CardDefaults.cardColors(CardBg2), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("Sesi Kasir", Modifier.weight(1f), color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Status(status, status != "TUTUP") }; Text("Kasir 01 • Shift Pagi", color = Muted); Text("Saldo berjalan", color = Muted); Text("Rp 4.820.000", color = Colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } } }
        item { Text("Master Akun & E-Wallet", color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(listOf("Kas Tunai di Laci" to "Rp 1.450.000", "OrderKuota" to "Rp 2.890.000", "DANA Merchant" to "Rp 1.150.000", "SeaBank Operasional" to "Rp 3.420.000", "QRIS" to "Rp 780.000")) { (n, v) -> Card(colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountBalanceWallet, null, tint = Colors.primary); Spacer(Modifier.width(12.dp)); Text(n, Modifier.weight(1f), color = Colors.onSurface, fontWeight = FontWeight.SemiBold); Text(v, color = Colors.onSurface, fontWeight = FontWeight.Bold) } } }
        item { Button(onClick = {}, Modifier.fillMaxWidth()) { Icon(Icons.Default.Sync, null); Spacer(Modifier.width(8.dp)); Text("Sinkronkan Saldo") } }
        item { OutlinedButton(onClick = {}, Modifier.fillMaxWidth()) { Text("Rekonsiliasi & Tutup Sesi") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TransactionSheet(api: ApiClient, close: () -> Unit) {
    var type by remember { mutableStateOf("Transaksi Biasa") }; var amount by remember { mutableStateOf("") }; var method by remember { mutableStateOf("Tunai") }; var customer by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var result by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = close, containerColor = Color(0xFF303030), contentColor = Colors.onSurface) {
        LazyColumn(contentPadding = PaddingValues(20.dp, 6.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            item { Text("Transaksi Baru", color = Colors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("POS Terminal Online", color = Muted) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(type == "Transaksi Biasa", { type = "Transaksi Biasa" }, label = { Text("Transaksi Biasa") }); FilterChip(type == "Servis HP", { type = "Servis HP" }, label = { Text("Servis HP") }) } }
            item { Text("Akses Cepat", color = Colors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { Quick(Modifier.weight(1f), Icons.Default.QrCodeScanner, "Token PLN") { amount = "50000" }; Quick(Modifier.weight(1f), Icons.Default.Wifi, "Paket Data") { amount = "100000" }; Quick(Modifier.weight(1f), Icons.Default.Wallet, "E-Wallet") { amount = "100000" }; Quick(Modifier.weight(1f), Icons.Default.Build, "Servis") { type = "Servis HP" } } }
            item { OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Total Pembayaran (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Colors.onSurface, unfocusedTextColor = Colors.onSurface, focusedLabelColor = Colors.primary, unfocusedLabelColor = Muted, focusedBorderColor = Colors.primary, unfocusedBorderColor = Colors.outline)) }
            item { Text("Metode Pembayaran", color = Colors.onSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Tunai", "Transfer", "Bon", "Split Bayar").forEach { m -> FilterChip(method == m, { method = m }, label = { Text(m) }) } } }
            item { OutlinedTextField(customer, { customer = it }, label = { Text("Pelanggan / Nomor WhatsApp") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Colors.onSurface, unfocusedTextColor = Colors.onSurface, focusedLabelColor = Colors.primary, unfocusedLabelColor = Muted, focusedBorderColor = Colors.primary, unfocusedBorderColor = Colors.outline)) }
            if (result.isNotBlank()) item { Text(result, color = if (result.startsWith("Berhasil")) Good else Colors.error, fontWeight = FontWeight.Bold) }
            item { Button(enabled = !busy, onClick = { scope.launch { busy = true; result = try { api.post("/api/transaksi", JSONObject().put("items", org.json.JSONArray()).put("metode_bayar", method).put("manual_entry", true).put("nominal", amount.toLongOrNull() ?: 0), true); "Berhasil menyimpan transaksi" } catch (e: Exception) { e.message ?: "Transaksi gagal" }; busy = false } }, Modifier.fillMaxWidth()) { if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Konfirmasi Transaksi") } }
        }
    }
}

@Composable private fun TransactionPage(pad: PaddingValues, api: ApiClient, back: () -> Unit) {
    var text by remember { mutableStateOf("Memuat transaksi…") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/transaksi", mapOf("date" to "2026-09-05", "limit" to "100")) }.onSuccess { text = it.toString().take(2000) }.onFailure { text = it.message ?: "Gagal memuat transaksi" } }
    DataPage(pad, "Transaksi", "Riwayat transaksi kasir", listOf(text), "", back)
}

@Composable private fun ReportPage(pad: PaddingValues, api: ApiClient, back: () -> Unit) {
    var text by remember { mutableStateOf("Memuat laporan…") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/laporan/bulan", mapOf("bulan" to "2026-09")) }.onSuccess { text = it.toString().take(2000) }.onFailure { text = it.message ?: "Gagal memuat laporan" } }
    DataPage(pad, "Laporan Kasir", "Ringkasan bulan berjalan", listOf(text), "", back)
}

@Composable private fun KasbonPage(pad: PaddingValues, api: ApiClient, back: () -> Unit) {
    var text by remember { mutableStateOf("Memuat kasbon…") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/kasbon") }.onSuccess { text = it.toString().take(2000) }.onFailure { text = it.message ?: "Gagal memuat kasbon" } }
    DataPage(pad, "Buku Kasbon", "Piutang pelanggan", listOf(text), "", back)
}

@Composable private fun PayrollPage(pad: PaddingValues, api: ApiClient, back: () -> Unit) {
    var text by remember { mutableStateOf("Memuat data gaji…") }
    LaunchedEffect(Unit) { runCatching { api.get("/api/gaji") }.onSuccess { text = it.toString().take(2000) }.onFailure { text = it.message ?: "Gagal memuat gaji" } }
    DataPage(pad, "Gaji Karyawan", "Modul owner / admin", listOf(text), "", back)
}

@Composable private fun StockPage(pad: PaddingValues, back: () -> Unit, open: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Daftar Barang & Stok", "Kelola katalog produk", back, {}) }
        item { OutlinedTextField("", {}, label = { Text("Cari produk / SKU") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Colors.onSurface, unfocusedTextColor = Colors.onSurface, focusedLabelColor = Colors.primary, unfocusedLabelColor = Muted, focusedBorderColor = Colors.primary, unfocusedBorderColor = Colors.outline)) }
        item { Button(onClick = {}, Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Tambah Produk") } }
        item { OutlinedButton(onClick = { open(Page.KATEGORI) }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Category, null); Spacer(Modifier.width(8.dp)); Text("Kelola Kategori") } }
        item { Text("Endpoint produk belum ada di kontrak API yang diberikan, jadi tidak dibuat endpoint fiktif.", color = Muted) }
    }
}

@Composable private fun MorePage(pad: PaddingValues, open: (Page) -> Unit, back: () -> Unit) {
    val modules = listOf("Daftar Barang & Stok" to Page.STOK, "Kelola Kategori" to Page.KATEGORI, "Laporan Servis HP" to Page.SERVICE, "Buku Kasbon" to Page.KASBON, "Data Pelanggan" to Page.PELANGGAN, "Catatan Pengeluaran" to Page.PENGELUARAN, "Gaji Karyawan" to Page.GAJI, "Pengaturan" to Page.PENGATURAN)
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Lainnya", "Semua modul Irkop Cell", back) }
        items(modules) { (name, target) -> Card(Modifier.clickable { open(target) }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChevronRight, null, tint = Colors.primary); Spacer(Modifier.width(14.dp)); Text(name, Modifier.weight(1f), color = Colors.onSurface, fontWeight = FontWeight.Bold); Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }
    }
}

@Composable private fun SettingsPage(pad: PaddingValues, back: () -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    val options = listOf("Tampilan & Mode Gelap", "NotifHook", "Manajemen User & Hak Akses", "Informasi Outlet & Printer", "Master Akun Uang", "Console Log / System Log")
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Pengaturan", "Konfigurasi Irkop Cell", back) }
        items(options) { title -> Card(Modifier.clickable { selected = title }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = Colors.primary); Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) { Text(title, color = Colors.onSurface, fontWeight = FontWeight.Bold); Text("Buka konfigurasi", color = Muted) }; Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }
    }
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text(selected!!, color = Colors.onSurface) }, text = { Text("Modul dapat dibuka. Endpoint khusus akan diterapkan hanya berdasarkan kontrak backend yang tersedia.", color = Muted) }, confirmButton = { Button(onClick = { selected = null }) { Text("Tutup") } })
}

@Composable private fun SimpleListPage(pad: PaddingValues, title: String, rows: List<String>, back: () -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header(title, "Irkop Cell", back, { selected = "Tambah Baru" }) }
        item { OutlinedTextField("", {}, label = { Text("Cari") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Colors.onSurface, unfocusedTextColor = Colors.onSurface, focusedLabelColor = Colors.primary, unfocusedLabelColor = Muted, focusedBorderColor = Colors.primary, unfocusedBorderColor = Colors.outline)) }
        items(rows) { row -> Card(Modifier.clickable { selected = row }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(row, color = Colors.onSurface, fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { selected = row }) { Text("Detail") }; Button(onClick = { selected = row }) { Text("Kelola") } } } } }
        item { Button(onClick = { selected = "Tambah Baru" }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Tambah Baru") } }
    }
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text(if (selected == "Tambah Baru") "Tambah Data" else "Detail", color = Colors.onSurface) }, text = { Text(selected!!, color = Muted) }, confirmButton = { Button(onClick = { selected = null }) { Text("Selesai") } })
}

@Composable private fun DataPage(pad: PaddingValues, title: String, subtitle: String, rows: List<String>, error: String, back: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header(title, subtitle, back) }
        if (error.isNotBlank()) item { Text(error, color = Colors.error) }
        items(rows) { row -> Card(colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(16.dp)) { Text(row, Modifier.fillMaxWidth().padding(16.dp), color = Colors.onSurface) } }
    }
}

@Composable private fun SearchDialog(close: () -> Unit) {
    var q by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("Cari di Irkop Cell", color = Colors.onSurface) }, text = { OutlinedTextField(q, { q = it }, label = { Text("Produk, transaksi, pelanggan, kasbon…") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Colors.onSurface, unfocusedTextColor = Colors.onSurface, focusedLabelColor = Colors.primary, unfocusedLabelColor = Muted, focusedBorderColor = Colors.primary, unfocusedBorderColor = Colors.outline)) }, confirmButton = { Button(onClick = close) { Text("Cari") } }, dismissButton = { TextButton(onClick = close) { Text("Batal") } })
}
