package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
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

private val AppColors = darkColorScheme(
    primary = Color(0xFF73E0CF),
    onPrimary = Color(0xFF062D28),
    primaryContainer = Color(0xFF35A996),
    onPrimaryContainer = Color(0xFFE5FFF9),
    secondary = Color(0xFF63E8AE),
    tertiary = Color(0xFFB8CCFF),
    background = Color(0xFF1A1A1A),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF343434),
    onSurfaceVariant = Color(0xFFD0D6D3),
    outline = Color(0xFF9AA7A3),
    outlineVariant = Color(0xFF505956),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF3B0805)
)

private val SurfaceLow = Color(0xFF242424)
private val SurfaceMid = Color(0xFF2B2B2B)
private val SurfaceHigh = Color(0xFF353535)
private val Muted = Color(0xFFD0D6D3)
private val Success = Color(0xFF63E8AE)

private enum class Page { HOME, KASIR, TRANSAKSI, LAPORAN, STOK, KATEGORI, SERVICE, KASBON, PELANGGAN, PENGELUARAN, GAJI, LAINNYA, PENGATURAN }

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
            try { api.login("demo", "demodemo"); ready = true }
            catch (e: Exception) { error = e.message ?: "Login gagal" }
        }
    }
    LaunchedEffect(Unit) { loginDemo() }
    if (ready) AppShell(api) else Box(Modifier.fillMaxSize().background(AppColors.background), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Irkop Cell", color = AppColors.onBackground, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            CircularProgressIndicator(color = AppColors.primary)
            Text(if (error.isBlank()) "Menyiapkan sesi kasir…" else error, color = if (error.isBlank()) Muted else AppColors.error)
            if (error.isNotBlank()) Button(onClick = ::loginDemo) { Text("Coba Lagi") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(api: ApiClient) {
    val activity = LocalContext.current as? ComponentActivity
    var page by remember { mutableStateOf(Page.HOME) }
    var history by remember { mutableStateOf(emptyList<Page>()) }
    var showTransaction by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    fun navigate(target: Page) {
        if (target == page) return
        history = history + page
        page = target
    }
    fun selectRoot(target: Page) {
        page = target
        history = emptyList()
    }
    fun goBack() {
        if (history.isNotEmpty()) {
            page = history.last()
            history = history.dropLast(1)
        } else if (page != Page.HOME) {
            page = Page.HOME
        } else {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }
    BackHandler(enabled = true) { goBack() }

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            NavigationBar(containerColor = SurfaceMid, contentColor = AppColors.onSurface) {
                NavigationBarItem(page == Page.HOME, { selectRoot(Page.HOME) }, icon = { Icon(Icons.Default.Home, "Beranda") }, label = { Text("Beranda") })
                NavigationBarItem(page == Page.KASIR, { selectRoot(Page.KASIR) }, icon = { Icon(Icons.Default.PointOfSale, "Kasir") }, label = { Text("Kasir") })
                NavigationBarItem(page == Page.LAPORAN, { selectRoot(Page.LAPORAN) }, icon = { Icon(Icons.Default.Assessment, "Laporan") }, label = { Text("Laporan") })
                NavigationBarItem(page == Page.LAINNYA, { selectRoot(Page.LAINNYA) }, icon = { Icon(Icons.Default.MoreHoriz, "Lainnya") }, label = { Text("Lainnya") })
            }
        },
        floatingActionButton = { FloatingActionButton(onClick = { showTransaction = true }, containerColor = AppColors.primary, contentColor = AppColors.onPrimary) { Icon(Icons.Default.Add, "Transaksi Baru") } }
    ) { padding ->
        when (page) {
            Page.HOME -> HomePage(padding, { showSearch = true }, ::navigate)
            Page.KASIR -> CashierPage(padding, ::goBack)
            Page.TRANSAKSI -> TransactionPage(padding, ::goBack)
            Page.LAPORAN -> ReportPage(padding, ::goBack)
            Page.STOK -> StockPage(padding, ::goBack, ::navigate)
            Page.KATEGORI -> CategoryPage(padding, ::goBack)
            Page.SERVICE -> ServicePage(padding, ::goBack)
            Page.KASBON -> KasbonPage(padding, ::goBack)
            Page.PELANGGAN -> CustomerPage(padding, ::goBack)
            Page.PENGELUARAN -> ExpensePage(padding, ::goBack)
            Page.GAJI -> PayrollPage(padding, ::goBack)
            Page.LAINNYA -> MorePage(padding, ::navigate, ::goBack)
            Page.PENGATURAN -> SettingsPage(padding, ::goBack)
        }
    }
    if (showTransaction) TransactionSheet { showTransaction = false }
    if (showSearch) SearchDialog { showSearch = false }
}

@Composable
private fun Header(title: String, subtitle: String? = null, back: (() -> Unit)? = null, add: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Kembali", tint = AppColors.onSurface) }
        Column(Modifier.weight(1f)) {
            Text(title, color = AppColors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (add != null) IconButton(onClick = add) { Icon(Icons.Default.Add, "Tambah", tint = AppColors.onSurface) }
    }
}

@Composable
private fun Metric(title: String, value: String, note: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Muted, style = MaterialTheme.typography.labelSmall)
            Text(value, color = AppColors.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(note, color = Success, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = AppColors.primary)
            Text(label, color = AppColors.onSurface, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomePage(padding: PaddingValues, search: () -> Unit, go: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Irkop Cell", color = AppColors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Sesi Kasir: Aktif", color = Success, fontWeight = FontWeight.Bold) }; IconButton(onClick = search) { Icon(Icons.Default.Search, "Cari", tint = AppColors.onSurface) }; Surface(Modifier.size(40.dp), CircleShape, AppColors.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("D", color = AppColors.onPrimaryContainer, fontWeight = FontWeight.Bold) } } } }
        item { Card(colors = CardDefaults.cardColors(SurfaceMid), shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable { go(Page.KASIR) }) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Sesi Kasir Aktif", color = AppColors.onSurface, fontWeight = FontWeight.Bold); Text("Kasir 01 • Shift Pagi • 08:00 WIB", color = Muted, style = MaterialTheme.typography.bodySmall) }; Pill("AKTIF") }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Saldo Awal", "Rp 1.500.000", "Opening", Modifier.weight(1f)); Metric("Saldo Berjalan", "Rp 5.750.000", "Real-time", Modifier.weight(1f)) }; Text("Kelola sesi & rekonsiliasi", color = AppColors.primary, fontWeight = FontWeight.Bold) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Saldo Kas Laci", "Rp 1.450.000", "Real-time", Modifier.weight(1f)); Metric("Omzet Hari Ini", "Rp 7.420.000", "82% target", Modifier.weight(1f)); Metric("Transaksi", "52", "Nota", Modifier.weight(1f)) } }
        item { Text("Perlu Perhatian Segera", color = AppColors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { AttentionRow("3 Servis Siap Diambil", "Kirim notifikasi WA", Icons.Default.Build) { go(Page.SERVICE) }; AttentionRow("Kasbon Jatuh Tempo Hari Ini", "2 pelanggan • Rp 320.000", Icons.Default.ReceiptLong) { go(Page.KASBON) }; AttentionRow("Selisih Belum Ditoleransi", "Shift malam • -Rp 15.000", Icons.Default.AccountBalanceWallet) { go(Page.KASIR) } } }
        item { Text("Menu Kasir Utama", color = AppColors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { QuickAction(Modifier.weight(1f), Icons.Default.Inventory2, "Stok") { go(Page.STOK) }; QuickAction(Modifier.weight(1f), Icons.Default.ReceiptLong, "Kasbon") { go(Page.KASBON) }; QuickAction(Modifier.weight(1f), Icons.Default.People, "Pelanggan") { go(Page.PELANGGAN) }; QuickAction(Modifier.weight(1f), Icons.Default.Wallet, "Pengeluaran") { go(Page.PENGELUARAN) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { QuickAction(Modifier.weight(1f), Icons.Default.Build, "Servis") { go(Page.SERVICE) }; QuickAction(Modifier.weight(1f), Icons.Default.People, "Gaji") { go(Page.GAJI) }; QuickAction(Modifier.weight(1f), Icons.Default.Assessment, "Laporan") { go(Page.LAPORAN) }; QuickAction(Modifier.weight(1f), Icons.Default.Settings, "Setting") { go(Page.PENGATURAN) } } }
    }
}

@Composable
private fun Pill(text: String) { Surface(color = SurfaceHigh, shape = CircleShape) { Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Success, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } }

@Composable
private fun AttentionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(Modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = AppColors.primary, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = AppColors.onSurface, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.ArrowForward, null, tint = Muted) } }
}

@Composable
private fun CashierPage(padding: PaddingValues, back: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Kasir & Rekonsiliasi", "Sesi kasir aktif", back) }
        item { Card(colors = CardDefaults.cardColors(SurfaceMid), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row { Text("Sesi Kasir", Modifier.weight(1f), color = AppColors.onSurface, fontWeight = FontWeight.Bold); Pill("AKTIF") }; Text("Kasir 01 • Shift Pagi", color = Muted); Text("Saldo berjalan", color = Muted); Text("Rp 4.820.000", color = AppColors.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } } }
        item { Text("Master Akun & E-Wallet", color = AppColors.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(listOf("Kas Tunai di Laci" to "Rp 1.450.000", "OrderKuota" to "Rp 2.890.000", "DANA Merchant" to "Rp 1.150.000", "SeaBank Operasional" to "Rp 3.420.000", "ShopeePay / QRIS" to "Rp 780.000")) { (name, balance) -> Card(Modifier.clickable {}, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.AccountBalanceWallet, null, tint = AppColors.primary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, color = AppColors.onSurface, fontWeight = FontWeight.SemiBold); Text("Saldo realtime", color = Muted, style = MaterialTheme.typography.bodySmall) }; Text(balance, color = AppColors.onSurface, fontWeight = FontWeight.Bold) } } }
        item { Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Sync, null); Spacer(Modifier.width(8.dp)); Text("Sinkronkan Saldo") } }
        item { OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Rekonsiliasi & Tutup Sesi") } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionSheet(close: () -> Unit) {
    var type by remember { mutableStateOf("Transaksi Biasa") }
    var method by remember { mutableStateOf("Tunai") }
    var amount by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = close, containerColor = SurfaceMid, contentColor = AppColors.onSurface) {
        LazyColumn(contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 30.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            item { Header("Transaksi Baru", "POS Terminal Online") }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(type == "Transaksi Biasa", { type = "Transaksi Biasa" }, label = { Text("Transaksi Biasa") }); FilterChip(type == "Servis HP", { type = "Servis HP" }, label = { Text("Servis HP") }) } }
            item { Text("Akses Cepat", color = AppColors.onSurface, fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { QuickAction(Modifier.weight(1f), Icons.Default.QrCodeScanner, "Token PLN") { amount = "50000" }; QuickAction(Modifier.weight(1f), Icons.Default.Wifi, "Paket Data") { amount = "100000" }; QuickAction(Modifier.weight(1f), Icons.Default.Wallet, "E-Wallet") { amount = "100000" }; QuickAction(Modifier.weight(1f), Icons.Default.Build, "Servis") { type = "Servis HP" } } }
            item { OutlinedTextField(amount, { amount = it }, label = { Text("Total Pembayaran (Rp)") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            item { Text("Metode Pembayaran", color = AppColors.onSurface, fontWeight = FontWeight.Bold) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Tunai", "Transfer", "Bon", "Split Bayar").forEach { value -> FilterChip(method == value, { method = value }, label = { Text(value) }) } } }
            item { OutlinedTextField(customer, { customer = it }, label = { Text("Pelanggan / Nomor WhatsApp") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            if (done) item { Text("Transaksi siap diproses.", color = Success, fontWeight = FontWeight.Bold) }
            item { Button(onClick = { done = true }, modifier = Modifier.fillMaxWidth()) { Text(if (done) "Tersimpan" else "Konfirmasi Transaksi") } }
        }
    }
}

@Composable
private fun TransactionPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Transaksi", "Riwayat transaksi kasir", listOf("TX-20260905-001", "TX-20260905-002", "TX-20260905-003"), back)
@Composable
private fun ReportPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Laporan Kasir", "Ringkasan penjualan", listOf("Total Omzet • Rp 7.420.000", "Total Transaksi • 52", "Laba Bersih Est. • Rp 1,15 Jt", "Kasbon • Rp 1,84 Jt"), back)
@Composable
private fun CategoryPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Kelola Kategori", "Kategori produk dan layanan", listOf("Aksesoris HP • 45 Produk", "Sparepart & LCD • 28 Produk", "Kartu Perdana & Kuota • 32 Produk", "Voucher & PPOB • 19 Produk", "Jasa Servis Hardware • 14 Layanan"), back)
@Composable
private fun ServicePage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Laporan Servis HP", "Tiket servis", listOf("SRV-2024-089 • Redmi Note 11 • Proses • Rp 380.000", "SRV-2024-085 • iPhone 11 • Siap Diambil • Rp 450.000", "SRV-2024-091 • Samsung A52 • Baru • Est. Rp 650.000"), back)
@Composable
private fun KasbonPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Buku Kasbon", "Piutang pelanggan", listOf("KB-882 • Pak RT Wawan • Sisa Rp 270.000", "KB-879 • Mas Dimas • Rp 450.000 • Belum Lunas", "KB-875 • Bu Sri • Rp 180.000 • Belum Lunas", "KB-860 • Rudi • LUNAS"), back)
@Composable
private fun CustomerPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Manajemen Pelanggan", "Data pelanggan", listOf("Dimas Wahyudi • 0812-4455-6677", "Siti Wulandari • 0857-9900-1122", "Haji Wawan • Jatuh Tempo 3 Hari"), back)
@Composable
private fun ExpensePage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Catatan Pengeluaran", "Pengeluaran toko", listOf("Kulakan Tempered Glass & Kabel Data • Rp 250.000", "Makan Siang Kasir • Rp 50.000", "Kertas Thermal & Nota • Rp 80.000", "Tagihan Listrik PLN • Rp 450.000", "Wifi Counter • Rp 200.000"), back)
@Composable
private fun PayrollPage(padding: PaddingValues, back: () -> Unit) = GenericPage(padding, "Gaji Karyawan & Shift", "Payroll", listOf("Siti Aminah • Kasir • Rp 2.480.000", "Doni Prasetyo • Teknisi • Komisi Rp 1.680.000", "Budi Santoso • Kasir • Rp 1.640.000"), back)

@Composable
private fun StockPage(padding: PaddingValues, back: () -> Unit, go: (Page) -> Unit) {
    GenericPage(padding, "Daftar Barang & Stok", "142 Produk • 3 SKU kritis", listOf("Total Aset Fisik • Rp 28.450.000", "Margin Rata-rata • 34%"), back, listOf("Tambah Produk", "Kelola Kategori"), { if (it == "Kelola Kategori") go(Page.KATEGORI) })
}

@Composable
private fun MorePage(padding: PaddingValues, go: (Page) -> Unit, back: () -> Unit) {
    val modules = listOf("Daftar Barang & Stok" to Page.STOK, "Kelola Kategori" to Page.KATEGORI, "Laporan Servis HP" to Page.SERVICE, "Buku Kasbon" to Page.KASBON, "Data Pelanggan" to Page.PELANGGAN, "Catatan Pengeluaran" to Page.PENGELUARAN, "Gaji Karyawan" to Page.GAJI, "Pengaturan" to Page.PENGATURAN)
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Lainnya", "Semua modul Irkop Cell", back) }
        items(modules) { (name, target) -> Card(Modifier.clickable { go(target) }, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChevronRight, null, tint = AppColors.primary); Spacer(Modifier.width(12.dp)); Text(name, Modifier.weight(1f), color = AppColors.onSurface, fontWeight = FontWeight.SemiBold); Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }
    }
}

@Composable
private fun SettingsPage(padding: PaddingValues, back: () -> Unit) {
    var selected by remember { mutableStateOf<String?>(null) }
    val options = listOf("Tampilan & Mode Gelap" to "Tema tampilan", "NotifHook" to "Gateway notifikasi", "Manajemen User & Hak Akses" to "Permission granular", "Informasi Outlet & Printer" to "Printer dan outlet", "Master Akun Uang" to "Kas dan e-wallet", "Console Log / System Log" to "Aktivitas sistem")
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Pengaturan", "Konfigurasi Irkop Cell", back) }
        items(options) { (title, subtitle) -> Card(Modifier.clickable { selected = title }, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Settings, null, tint = AppColors.primary); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = AppColors.onSurface, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }; Icon(Icons.Default.ChevronRight, null, tint = Muted) } } }
    }
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text(selected!!, color = AppColors.onSurface) }, text = { Text("Modul pengaturan dapat dibuka tanpa keluar dari halaman sebelumnya.", color = Muted) }, confirmButton = { Button(onClick = { selected = null }) { Text("Selesai") } })
}

@Composable
private fun GenericPage(padding: PaddingValues, title: String, subtitle: String, rows: List<String>, back: () -> Unit, actions: List<String> = listOf("Tambah Baru"), onAction: (String) -> Unit = {}) {
    var selected by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header(title, subtitle, back) }
        item { OutlinedTextField(query, { query = it }, label = { Text("Cari / filter data") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
        items(rows.filter { query.isBlank() || it.contains(query, true) }) { row -> Card(Modifier.clickable { selected = row }, colors = CardDefaults.cardColors(SurfaceLow), shape = RoundedCornerShape(16.dp)) { Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text(row, color = AppColors.onSurface, fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { selected = row }) { Text("Detail") }; Button(onClick = { selected = row }) { Text("Kelola") } } } } }
        items(actions) { action -> Button(onClick = { onAction(action) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text(action) } }
    }
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text("Detail", color = AppColors.onSurface) }, text = { Text(selected!!, color = Muted) }, confirmButton = { Button(onClick = { selected = null }) { Text("Selesai") } })
}

@Composable
private fun SearchDialog(close: () -> Unit) {
    var query by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = close, title = { Text("Cari di Irkop Cell", color = AppColors.onSurface) }, text = { OutlinedTextField(query, { query = it }, label = { Text("Produk, transaksi, pelanggan, kasbon…") }) }, confirmButton = { Button(onClick = close) { Text("Cari") } }, dismissButton = { TextButton(onClick = close) { Text("Batal") } })
}
