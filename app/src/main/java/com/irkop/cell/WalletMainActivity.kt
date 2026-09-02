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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.ApiError
import com.irkop.cell.core.AuthPolicy
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import com.irkop.cell.ui.AppViewModel
import com.irkop.cell.features.akun.AkunUangScreen
import com.irkop.cell.features.admin.OperationsScreen
import com.irkop.cell.features.admin.CorrectionsScreen
import com.irkop.cell.features.kasbon.KasbonDetailScreen
import com.irkop.cell.features.laporan.LaporanAnalyticsScreen
import com.irkop.cell.features.pelanggan.PelangganScreen
import com.irkop.cell.features.service.ServiceHpScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val Rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun JsonObject.s(vararg keys: String): String = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.n(vararg keys: String): Long = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L
private fun JsonObject.rows(): List<JsonObject> = this["items"]?.jsonArray?.filterIsInstance<JsonObject>().orEmpty()
private fun initials(value: String): String = value.trim().split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "IC" }

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
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(38.dp)) }
            Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("POS & Buku Kas Digital", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(user, { user = it }, label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            Button({ login(user.trim(), pass) }, enabled = user.isNotBlank() && pass.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Masuk") }
            if (!error.isNullOrBlank()) { Text(error, color = MaterialTheme.colorScheme.error); LaunchedEffect(error) { clear() } }
        }
    }
}

private enum class WalletTab(val label: String, val icon: ImageVector) { HOME("Home", Icons.Default.Home), TRANSAKSI("Transaksi", Icons.Default.ReceiptLong), KASIR("Kasir", Icons.Default.PointOfSale), LAPORAN("Laporan", Icons.Default.Assessment), LAINNYA("Lainnya", Icons.Default.MoreHoriz) }

@Composable private fun WalletShell(user: UserSession, logout: () -> Unit, repo: Repository) {
    var tab by remember { mutableStateOf(WalletTab.HOME) }
    var other by remember { mutableStateOf<String?>(null) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(initials(user.nama.ifBlank { user.username }), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                    Spacer(Modifier.width(10.dp)); Column { Text("IRKOP CELL", fontWeight = FontWeight.Bold); Text(user.nama.ifBlank { user.username }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } },
                actions = { IconButton({}) { Icon(Icons.Default.NotificationsNone, "Notifikasi") }; IconButton(logout) { Icon(Icons.Default.Logout, "Keluar") } }
            )
        },
        bottomBar = { NavigationBar { WalletTab.values().forEach { t -> NavigationBarItem(selected = tab == t, onClick = { tab = t; if (t != WalletTab.LAINNYA) other = null }, icon = { Icon(t.icon, null) }, label = { Text(t.label) }) } } }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                WalletTab.HOME -> WalletHome(repo, { tab = WalletTab.TRANSAKSI }, { tab = WalletTab.KASIR }, { tab = WalletTab.LAINNYA; other = "pelanggan" }, { tab = WalletTab.LAPORAN })
                WalletTab.TRANSAKSI -> WalletTransactions(repo)
                WalletTab.KASIR -> WalletCashier(repo)
                WalletTab.LAPORAN -> LaporanAnalyticsScreen(repo)
                WalletTab.LAINNYA -> WalletOther(user, repo, other) { other = it }
            }
        }
    }
}

@Composable private fun WalletHome(repo: Repository, tx: () -> Unit, kasir: () -> Unit, other: () -> Unit, report: () -> Unit) {
    var kasirData by remember { mutableStateOf<JsonObject?>(null) }
    var txData by remember { mutableStateOf<JsonObject?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    suspend fun load() { loading = true; runCatching { kasirData = repo.kasirCurrent(); txData = repo.transaksi(tanggal = LocalDate.now().toString()) }.onFailure { error = ApiError.message(it) }; loading = false }
    LaunchedEffect(Unit) { load() }
    val balance = kasirData?.rows()?.sumOf { it.n("saldo_sistem", "saldo", "total") } ?: kasirData?.n("saldo", "saldo_sistem") ?: 0L
    val transactions = txData?.rows().orEmpty()
    val omzet = txData?.n("total_nilai", "total_omzet", "omzet") ?: 0L
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)) {
        item { Text("Selamat datang 👋", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Kelola toko lebih cepat hari ini", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { BalanceCard(balance, transactions.size, tx) }
        item { SectionTitle("Akses cepat") }
        item { QuickGrid(tx, kasir, other, report) }
        item { SectionTitle("Aktivitas terbaru", tx) }
        when { loading -> item { Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator() } }; error != null -> item { ErrorCard(error!!, ::load) }; transactions.isEmpty() -> item { EmptyWallet() }; else -> items(transactions.take(8)) { ActivityRow(it) } }
        item { OutlinedButton({ load() }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Refresh data") } }
        item { Text("Omzet hari ini: ${Rupiah.format(omzet)}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable private fun BalanceCard(balance: Long, count: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
        Column(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))).padding(22.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Saldo kasir", color = MaterialTheme.colorScheme.onPrimary); Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary) }
            Spacer(Modifier.height(8.dp)); Text(Rupiah.format(balance), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("$count transaksi hari ini", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f)); Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .16f)) { Text("Buka transaksi ›", Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.onPrimary) } }
        }
    }
}

@Composable private fun QuickGrid(tx: () -> Unit, kasir: () -> Unit, other: () -> Unit, report: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { QuickAction("Transaksi", Icons.Default.ReceiptLong, tx, Modifier.weight(1f)); QuickAction("Kasir", Icons.Default.PointOfSale, kasir, Modifier.weight(1f)); QuickAction("Kasbon", Icons.Default.AccountBalanceWallet, other, Modifier.weight(1f)); QuickAction("Laporan", Icons.Default.Assessment, report, Modifier.weight(1f)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { QuickAction("Pelanggan", Icons.Default.People, other, Modifier.weight(1f)); QuickAction("Produk", Icons.Default.Inventory2, other, Modifier.weight(1f)); QuickAction("Service", Icons.Default.Build, other, Modifier.weight(1f)); QuickAction("Lainnya", Icons.Default.GridView, other, Modifier.weight(1f)) }
    }
}

@Composable private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) { Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) { FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(46.dp)) { Icon(icon, label) }; Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.labelMedium) } } }
@Composable private fun SectionTitle(title: String, action: (() -> Unit)? = null) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); if (action != null) TextButton(action) { Text("Lihat semua") } } }
@Composable private fun ActivityRow(x: JsonObject) { val name = x.s("pelanggan_nama", "pelanggan", "nama"); Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(initials(name), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.SemiBold); Text(x.s("created_at", "tanggal", "waktu"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(Rupiah.format(x.n("total", "nominal", "grand_total")), fontWeight = FontWeight.Bold) } } }
@Composable private fun EmptyWallet() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Inbox, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(8.dp)); Text("Belum ada transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Transaksi baru akan muncul di sini", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun ErrorCard(message: String, retry: () -> Unit) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(message, color = MaterialTheme.colorScheme.onErrorContainer); TextButton(retry) { Text("Coba lagi") } } } }

@Composable private fun WalletTransactions(repo: Repository) {
    var query by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var add by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<JsonObject?>(null) }
    val scope = rememberCoroutineScope()
    fun reload() { scope.launch { loading = true; runCatching { repo.transaksi(q = query.trim().takeIf { it.isNotBlank() }, metodeBayar = method.takeIf { it.isNotBlank() }, statusKonfirmasi = confirmation.takeIf { it.isNotBlank() }) }.onSuccess { rows = it.rows(); error = null }.onFailure { error = ApiError.message(it) }; loading = false } }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Transaksi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(::reload) { Icon(Icons.Default.Refresh, "Refresh") } } }
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), placeholder = { Text("Cari ID, pelanggan, transaksi…") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { FilterRow(listOf("" to "Semua", "tunai" to "Tunai", "transfer" to "Transfer", "bon" to "Kasbon", "cash_tunai" to "Cash"), method) { method = it; reload() } }
        item { FilterRow(listOf("" to "Semua status", "menunggu" to "Menunggu", "otomatis" to "Otomatis", "manual" to "Manual", "tidak_perlu" to "Tidak perlu"), confirmation) { confirmation = it; reload() } }
        item { Button(::reload, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Terapkan filter") } }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(30.dp), Alignment.Center) { CircularProgressIndicator() } }
        else if (rows.isEmpty()) item { EmptyWallet() }
        else items(rows) { row -> TransactionRow(row) { detail = row } }
        error?.let { item { ErrorCard(it, ::reload) } }
    }
    FloatingActionButton({ add = true }, Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(18.dp)) { Icon(Icons.Default.Add, "Transaksi baru") }
    if (add) ModernCheckoutDialog(repo, { add = false; reload() }, { add = false })
    detail?.let { TransactionDetail(repo, it, { detail = null; reload() }, { detail = null }) }
}

@Composable private fun FilterRow(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { options.forEach { (v, label) -> FilterChip(selected == v, { onSelect(v) }, label = { Text(label) }) } } }
@Composable private fun TransactionRow(row: JsonObject, onClick: () -> Unit) { Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(row.s("nomor", "kode", "id"), fontWeight = FontWeight.Bold); Text(Rupiah.format(row.n("total", "nominal", "grand_total")), fontWeight = FontWeight.Bold) }; Text(row.s("pelanggan_nama", "pelanggan", "nama")); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(row.s("metode_bayar", "metode"), style = MaterialTheme.typography.labelMedium); Text(row.s("konfirmasi_pembayaran", "status_konfirmasi", "status"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }; Text(row.s("created_at", "tanggal", "tanggal_transaksi"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }

@Composable private fun TransactionDetail(repo: Repository, initial: JsonObject, onDone: () -> Unit, onCancel: () -> Unit) {
    val scope = rememberCoroutineScope(); var data by remember { mutableStateOf(initial) }; var deleting by remember { mutableStateOf(false) }; var reason by remember { mutableStateOf("") }; var status by remember { mutableStateOf(initial.s("konfirmasi_pembayaran", "status_konfirmasi").takeUnless { it == "-" } ?: "menunggu") }; var message by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onCancel, title = { Text("Detail transaksi") }, text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) { item { Text("ID: ${data.s("id", "nomor", "kode")}") }; item { Text("Pelanggan: ${data.s("pelanggan_nama", "pelanggan")}") }; item { Text("Total: ${Rupiah.format(data.n("total", "nominal", "grand_total"))}", fontWeight = FontWeight.Bold) }; item { Text("Metode: ${data.s("metode_bayar", "metode")}") }; item { Text("Status konfirmasi", style = MaterialTheme.typography.labelLarge) }; item { FilterRow(listOf("menunggu" to "Menunggu", "otomatis" to "Otomatis", "manual" to "Manual", "tidak_perlu" to "Tidak perlu"), status) { status = it } }; item { Button({ scope.launch { runCatching { repo.updateTransaksiKonfirmasi(data.s("id"), status) }.onSuccess { message = "Konfirmasi tersimpan" }.onFailure { message = ApiError.message(it) } } }) { Text("Simpan konfirmasi") } }; message?.let { item { Text(it, color = if (it == "Konfirmasi tersimpan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) } }; if (deleting) item { OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text("Alasan hapus") }) } } }, confirmButton = { if (!deleting) Row { TextButton({ deleting = true }) { Text("Hapus") }; TextButton(onCancel) { Text("Tutup") } } else Button({ scope.launch { runCatching { repo.deleteTransaksi(data.s("id"), reason.takeIf { it.isNotBlank() }) }.onSuccess { onDone() }.onFailure { message = ApiError.message(it) } } }) { Text("Konfirmasi hapus") } }, dismissButton = { if (deleting) TextButton({ deleting = false }) { Text("Batal") } })
}

@Composable private fun WalletCashier(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; var opening by remember { mutableStateOf(false) }; var closing by remember { mutableStateOf(false) }; var checkout by remember { mutableStateOf(false) }; var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    fun load() { scope.launch { loading = true; runCatching { data = repo.kasirCurrent() }.onFailure { error = ApiError.message(it) }; loading = false } }
    LaunchedEffect(Unit) { load() }
    val status = data?.s("status") ?: "-"
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
        item { Text("Kasir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Kelola sesi kasir dan transaksi penjualan", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Status sesi", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (status == "buka") "Kasir sedang buka" else if (status == "tutup") "Kasir sudah ditutup" else "Belum buka", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Tanggal: ${data?.s("tanggal") ?: "-"}") } } }
        item { if (loading) LinearProgressIndicator(Modifier.fillMaxWidth()) else if (error != null) ErrorCard(error!!, ::load) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button({ opening = true; amount = "" }, Modifier.weight(1f), shape = RoundedCornerShape(14.dp), enabled = status != "buka") { Text("Opening") }; OutlinedButton({ closing = true; amount = ""; note = "" }, Modifier.weight(1f), shape = RoundedCornerShape(14.dp), enabled = status == "buka") { Text("Closing") } } }
        item { Button({ checkout = true }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.PointOfSale, null); Spacer(Modifier.width(8.dp)); Text("Transaksi penjualan") } }
        item { Text("Saldo per akun", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        val accounts = data?.rows().orEmpty(); if (accounts.isEmpty()) item { Text("Belum ada data saldo.", color = MaterialTheme.colorScheme.onSurfaceVariant) } else items(accounts) { a -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Row(Modifier.padding(14.dp), Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(a.s("nama_akun", "nama")); Text(Rupiah.format(a.n("saldo_sistem", "saldo")), fontWeight = FontWeight.Bold) } } }
    }
    if (opening) AmountDialog("Opening Kasir", amount, { amount = it }, { val v = amount.toLongOrNull(); if (v != null && v >= 0) scope.launch { runCatching { repo.opening(listOf("Tunai Laci" to v)) }.onSuccess { opening = false; load() }.onFailure { error = ApiError.message(it) } } }, { opening = false })
    if (closing) AlertDialog(onDismissRequest = { closing = false }, title = { Text("Closing Kasir") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Saldo real tunai") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Catatan") }, singleLine = true) } }, confirmButton = { Button({ val v = amount.toLongOrNull(); if (v != null && v >= 0) scope.launch { runCatching { repo.closing(listOf("Tunai Laci" to v), note) }.onSuccess { closing = false; load() }.onFailure { error = ApiError.message(it) } } }) { Text("Simpan") } }, dismissButton = { TextButton({ closing = false }) { Text("Batal") } })
    if (checkout) ModernCheckoutDialog(repo, { checkout = false; load() }, { checkout = false })
}

@Composable private fun AmountDialog(title: String, value: String, onValue: (String) -> Unit, onConfirm: () -> Unit, onCancel: () -> Unit) { AlertDialog(onDismissRequest = onCancel, title = { Text(title) }, text = { OutlinedTextField(value, onValue, label = { Text("Nominal") }, singleLine = true) }, confirmButton = { Button(onConfirm) { Text("Simpan") } }, dismissButton = { TextButton(onCancel) { Text("Batal") } }) }

@Composable private fun WalletOther(user: UserSession, repo: Repository, selected: String?, onSelect: (String) -> Unit) {
    if (selected != null) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { IconButton({ onSelect("") }) { Icon(Icons.Default.ArrowBack, "Kembali") }; Column { Text("Menu Lainnya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(otherTitle(selected), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            HorizontalDivider()
            Box(Modifier.fillMaxSize().padding(10.dp)) { when (selected) {
                "pelanggan" -> PelangganScreen(repo)
                "kasbon" -> KasbonDetailScreen(repo)
                "service" -> ServiceHpScreen(repo)
                "akun" -> AkunUangScreen(repo)
                "ops" -> OperationsScreen(user, repo)
                "koreksi" -> CorrectionsScreen(repo)
            } }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
            item { Text("Lainnya", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Semua fitur operasional dalam satu tempat", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { OtherSection("Operasional") { OtherGrid(user, onSelect, listOf(Triple("pelanggan", "Pelanggan", Icons.Default.People), Triple("kasbon", "Kasbon", Icons.Default.AccountBalanceWallet), Triple("service", "Service HP", Icons.Default.Build), Triple("akun", "Akun Uang", Icons.Default.AccountBalance))) } }
            if (user.role.equals("admin", true)) item { OtherSection("Administrasi") { OtherGrid(user, onSelect, listOf(Triple("ops", "Produk & Operasional", Icons.Default.Inventory2), Triple("koreksi", "Koreksi", Icons.Default.EditNote))) } }
        }
    }
}

private fun otherTitle(key: String) = when (key) { "pelanggan" -> "Pelanggan"; "kasbon" -> "Kasbon"; "service" -> "Service HP"; "akun" -> "Akun Uang"; "ops" -> "Produk & Operasional"; "koreksi" -> "Koreksi"; else -> "Fitur" }
@Composable private fun OtherSection(title: String, content: @Composable () -> Unit) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); content() } }
@Composable private fun OtherGrid(user: UserSession, onSelect: (String) -> Unit, items: List<Triple<String, String, ImageVector>>) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { items.chunked(2).forEach { pair -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { pair.forEach { (key, label, icon) -> Card(Modifier.weight(1f).clickable { onSelect(key) }, shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(12.dp)); Text(label, fontWeight = FontWeight.SemiBold) } } }; if (pair.size == 1) Spacer(Modifier.weight(1f)) } } } }

