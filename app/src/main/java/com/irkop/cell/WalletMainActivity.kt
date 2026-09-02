@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.ApiError
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import com.irkop.cell.ui.AppViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.util.Locale

private val Rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun JsonObject.s(vararg keys: String): String = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.n(vararg keys: String): Long = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L
private fun JsonObject.rows(): List<JsonObject> = this["items"]?.jsonArray?.mapNotNull { it as? JsonObject }.orEmpty()
private fun initials(value: String) = value.trim().split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "IC" }

class WalletMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(applicationContext)
        val repo = Repository(ApiClient(session).api)
        setContent {
            val vm: AppViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppViewModel(session, repo) as T
            })
            IrkopTheme { WalletRoot(vm, repo) }
        }
    }
}

@Composable private fun WalletRoot(vm: AppViewModel, repo: Repository) {
    val state by vm.state.collectAsState()
    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.user == null -> WalletLogin(state.error, vm::login, vm::clearError)
        else -> WalletShell(state.user!!, vm::logout, repo)
    }
}

@Composable private fun WalletLogin(error: String?, login: (String, String) -> Unit, clear: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(68.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp)) }
            Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Kasir, transaksi, dan operasional toko", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(user, { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            Button({ login(user.trim(), pass) }, enabled = user.isNotBlank() && pass.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Masuk") }
            if (!error.isNullOrBlank()) { Text(error, color = MaterialTheme.colorScheme.error); LaunchedEffect(error) { clear() } }
        }
    }
}

private enum class WalletTab(val label: String, val icon: ImageVector) { HOME("Home", Icons.Default.Home), TRANSAKSI("Transaksi", Icons.Default.ReceiptLong), KASIR("Kasir", Icons.Default.PointOfSale), LAPORAN("Laporan", Icons.Default.Assessment), LAINNYA("Lainnya", Icons.Default.MoreHoriz) }

@Composable private fun WalletShell(user: UserSession, logout: () -> Unit, repo: Repository) {
    var tab by remember { mutableStateOf(WalletTab.HOME) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (tab != WalletTab.KASIR) TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(initials(user.nama.ifBlank { user.username }), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(10.dp)); Column { Text("IRKOP CELL", fontWeight = FontWeight.Bold); Text(user.nama.ifBlank { user.username }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } },
                navigationIcon = {},
                actions = { IconButton({}) { Icon(Icons.Default.NotificationsNone, "Notifikasi") }; IconButton(logout) { Icon(Icons.Default.Logout, "Keluar") } }
            )
        },
        bottomBar = { NavigationBar { WalletTab.values().forEach { t -> NavigationBarItem(selected = tab == t, onClick = { tab = t }, icon = { Icon(t.icon, null) }, label = { Text(t.label) }) } } }
    ) { padding -> Box(Modifier.fillMaxSize().padding(padding)) { when (tab) {
        WalletTab.HOME -> WalletHome(repo, { tab = WalletTab.TRANSAKSI }, { tab = WalletTab.KASIR }, { tab = WalletTab.LAINNYA }, { tab = WalletTab.LAPORAN })
        WalletTab.TRANSAKSI -> WalletTransactions(repo)
        WalletTab.KASIR -> WalletCashier(repo)
        WalletTab.LAPORAN, WalletTab.LAINNYA -> ParityExtrasScreen(user, repo)
    } } }
}

@Composable private fun WalletHome(repo: Repository, tx: () -> Unit, kasir: () -> Unit, other: () -> Unit, report: () -> Unit) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { runCatching { repo.transaksi() }.onSuccess { data = it }; loading = false }
    val rows = data?.rows().orEmpty()
    val balance = data?.n("total_nilai", "total_omzet", "omzet") ?: rows.sumOf { it.n("total", "nominal", "grand_total") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp), contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)) {
        item { Text("Selamat datang 👋", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Kelola toko lebih cepat hari ini", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { BalanceCard(balance, rows.size, tx) }
        item { SectionTitle("Akses cepat") }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Shortcut("Transfer", Icons.Default.Send, other); Shortcut("Top Up", Icons.Default.AddCircle, other); Shortcut("Bayar", Icons.Default.Payment, kasir); Shortcut("Kasbon", Icons.Default.AccountBalanceWallet, other) }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { Shortcut("Pulsa", Icons.Default.PhoneAndroid, kasir); Shortcut("Data", Icons.Default.Wifi, kasir); Shortcut("Laporan", Icons.Default.Assessment, report); Shortcut("Lainnya", Icons.Default.GridView, other) } } }
        item { SectionTitle("Aktivitas terbaru", tx) }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(30.dp), Alignment.Center) { CircularProgressIndicator() } } else if (rows.isEmpty()) item { EmptyWallet() } else items(rows.take(8)) { ActivityRow(it) }
    }
}

@Composable private fun SectionTitle(title: String, action: (() -> Unit)? = null) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); if (action != null) TextButton(action) { Text("Lihat semua") } } }

@Composable private fun BalanceCard(balance: Long, count: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Column(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))).padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Saldo operasional", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Medium); Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary) }
            Spacer(Modifier.height(7.dp)); Text(Rupiah.format(balance), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("$count transaksi", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f), style = MaterialTheme.typography.labelMedium); Text("Hari ini", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold) }; Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .15f)) { Text("Lihat transaksi  ›", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium) } }
        }
    }
}

@Composable private fun Shortcut(label: String, icon: ImageVector, onClick: () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) { FilledTonalIconButton(onClick, Modifier.size(52.dp), shape = CircleShape) { Icon(icon, label) }; Spacer(Modifier.height(6.dp)); Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1) } }

@Composable private fun ActivityRow(x: JsonObject) { val name = x.s("pelanggan_nama", "pelanggan", "nama"); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(initials(name), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold); Text(x.s("created_at", "tanggal", "waktu"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(Rupiah.format(x.n("total", "nominal", "grand_total")), fontWeight = FontWeight.Bold) } } }
@Composable private fun EmptyWallet() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Inbox, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)); Spacer(Modifier.height(8.dp)); Text("Belum ada transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Transaksi baru akan muncul di sini", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable private fun WalletTransactions(repo: Repository) {
    var query by remember { mutableStateOf("") }; var rows by remember { mutableStateOf(emptyList<JsonObject>()) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; var add by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    fun reload() { scope.launch { loading = true; runCatching { repo.transaksi(q = query.trim().takeIf { it.isNotBlank() }) }.onSuccess { rows = it.rows(); error = null }.onFailure { error = ApiError.message(it) }; loading = false } }
    LaunchedEffect(Unit) { reload() }
    Box(Modifier.fillMaxSize()) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Transaksi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(::reload) { Icon(Icons.Default.Refresh, "Refresh") } } }
        item { OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), placeholder = { Text("Cari transaksi") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Semua", "Penjualan", "Pembelian", "Kasbon").forEach { FilterChip(it == "Semua", {}, label = { Text(it) }) } } }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator() } } else if (rows.isEmpty()) item { EmptyWallet() } else items(rows) { ActivityRow(it) }
        error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
    }; FloatingActionButton({ add = true }, Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Icon(Icons.Default.Add, "Tambah transaksi") } }
    if (add) ModernCheckoutDialog(repo, { add = false; reload() }, { add = false })
}

@Composable private fun WalletCashier(repo: Repository) { var open by remember { mutableStateOf(false) }; Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Kasir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp)) { Icon(Icons.Default.PointOfSale, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp)); Text("Mulai transaksi penjualan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Pilih produk, jumlah, pelanggan dan metode pembayaran.", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(16.dp)); Button({ open = true }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Transaksi Baru") } } } }; if (open) ModernCheckoutDialog(repo, { open = false }, { open = false }) }
