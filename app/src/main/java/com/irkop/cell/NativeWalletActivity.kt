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
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import com.irkop.cell.features.akun.AkunUangScreen
import com.irkop.cell.features.admin.CorrectionsScreen
import com.irkop.cell.features.admin.OperationsScreen
import com.irkop.cell.features.kasbon.KasbonDetailScreen
import com.irkop.cell.features.laporan.LaporanAnalyticsScreen
import com.irkop.cell.features.pelanggan.PelangganScreen
import com.irkop.cell.features.service.ServiceHpScreen
import com.irkop.cell.ui.AppViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val NativeRp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun JsonObject.nt(vararg keys: String) = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.nn(vararg keys: String) = keys.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L
private fun JsonObject.nitems() = this["items"]?.jsonArray?.filterIsInstance<JsonObject>().orEmpty()
private fun nativeInitials(s: String) = s.trim().split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "IC" }

class NativeWalletActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(applicationContext)
        val repo = Repository(ApiClient(session).api)
        setContent {
            val vm: AppViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppViewModel(session, repo) as T
            })
            IrkopTheme { NativeWalletRoot(vm, repo) }
        }
    }
}

@Composable private fun NativeWalletRoot(vm: AppViewModel, repo: Repository) {
    val state by vm.state.collectAsState()
    when {
        state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        state.user == null -> NativeLogin(state.error, vm::login, vm::clearError)
        else -> NativeShell(state.user!!, vm::logout, repo)
    }
}

@Composable private fun NativeLogin(error: String?, login: (String, String) -> Unit, clear: () -> Unit) {
    var user by remember { mutableStateOf("") }; var pass by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp)) }
            Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("POS & Buku Kas Digital", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(user, { user = it }, Modifier.fillMaxWidth(), label = { Text("Username") }, singleLine = true)
            OutlinedTextField(pass, { pass = it }, Modifier.fillMaxWidth(), label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
            Button({ login(user.trim(), pass) }, enabled = user.isNotBlank() && pass.isNotBlank(), Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Masuk") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error); LaunchedEffect(it) { clear() } }
        }
    }
}

private enum class NativeTab(val label: String, val icon: ImageVector) { HOME("Home", Icons.Default.Home), TX("Transaksi", Icons.Default.ReceiptLong), KASIR("Kasir", Icons.Default.PointOfSale), LAPORAN("Laporan", Icons.Default.Assessment), LAINNYA("Lainnya", Icons.Default.MoreHoriz) }

@Composable private fun NativeShell(user: UserSession, logout: () -> Unit, repo: Repository) {
    var tab by remember { mutableStateOf(NativeTab.HOME) }; var other by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { TopAppBar(title = { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(nativeInitials(user.nama.ifBlank { user.username }), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(10.dp)); Column { Text("IRKOP CELL", fontWeight = FontWeight.Bold); Text(user.nama.ifBlank { user.username }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }, actions = { IconButton({}) { Icon(Icons.Default.NotificationsNone, "Notifikasi") }; IconButton(logout) { Icon(Icons.Default.Logout, "Keluar") } } ) }, bottomBar = { NavigationBar { NativeTab.values().forEach { t -> NavigationBarItem(selected = tab == t, onClick = { tab = t; if (t != NativeTab.LAINNYA) other = null }, icon = { Icon(t.icon, null) }, label = { Text(t.label) }) } } }) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) { when (tab) {
            NativeTab.HOME -> NativeHome(repo, { tab = NativeTab.TX }, { tab = NativeTab.KASIR }, { tab = NativeTab.LAPORAN }, { key -> tab = NativeTab.LAINNYA; other = key })
            NativeTab.TX -> NativeTransactions(repo)
            NativeTab.KASIR -> NativeCashier(repo)
            NativeTab.LAPORAN -> LaporanAnalyticsScreen(repo)
            NativeTab.LAINNYA -> NativeOther(user, repo, other) { other = it }
        } }
    }
}

@Composable private fun NativeHome(repo: Repository, tx: () -> Unit, cashier: () -> Unit, report: () -> Unit, openOther: (String) -> Unit) {
    var cash by remember { mutableStateOf<JsonObject?>(null) }; var transactions by remember { mutableStateOf<JsonObject?>(null) }; var error by remember { mutableStateOf<String?>(null) }; var loading by remember { mutableStateOf(true) }
    fun load() { kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch { loading = true; runCatching { cash = repo.kasirCurrent(); transactions = repo.transaksi(tanggal = LocalDate.now().toString()) }.onFailure { error = ApiError.message(it) }; loading = false } }
    LaunchedEffect(Unit) { load() }
    val rows = transactions?.nitems().orEmpty(); val balance = cash?.nitems()?.sumOf { it.nn("saldo_sistem", "saldo", "total") } ?: cash?.nn("saldo_sistem", "saldo") ?: 0L; val omzet = transactions?.nn("total_nilai", "total_omzet", "omzet") ?: 0L
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)) {
        item { Text("Selamat datang 👋", color = MaterialTheme.colorScheme.onSurfaceVariant); Text("Kelola toko lebih cepat hari ini", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { Card(Modifier.fillMaxWidth().clickable { tx() }, shape = RoundedCornerShape(26.dp)) { Box(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))).padding(22.dp)) { Column { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Saldo kasir", color = MaterialTheme.colorScheme.onPrimary); Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary) }; Spacer(Modifier.height(8.dp)); Text(NativeRp.format(balance), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(14.dp)); Text("$ {rows.size} transaksi hari ini".replace("$ ", ""), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f)); Text("Omzet hari ini ${NativeRp.format(omzet)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium) } } } }
        item { Text("Akses cepat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { NativeQuickGrid(tx, cashier, report, openOther) }
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Aktivitas terbaru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); TextButton(tx) { Text("Lihat semua") } } }
        when { loading -> item { Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) { CircularProgressIndicator() } }; error != null -> item { NativeError(error!!, ::load) }; rows.isEmpty() -> item { NativeEmpty() }; else -> items(rows.take(8)) { NativeActivity(it) } }
    }
}

@Composable private fun NativeQuickGrid(tx: () -> Unit, cashier: () -> Unit, report: () -> Unit, other: (String) -> Unit) {
    val actions = listOf(Triple("Transaksi", Icons.Default.ReceiptLong, { tx() }), Triple("Kasir", Icons.Default.PointOfSale, { cashier() }), Triple("Kasbon", Icons.Default.AccountBalanceWallet, { other("kasbon") }), Triple("Laporan", Icons.Default.Assessment, { report() }), Triple("Pelanggan", Icons.Default.People, { other("pelanggan") }), Triple("Produk", Icons.Default.Inventory2, { other("ops") }), Triple("Service", Icons.Default.Build, { other("service") }), Triple("Akun Uang", Icons.Default.AccountBalance, { other("akun") }))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { actions.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { (label, icon, click) -> Card(Modifier.weight(1f).clickable { click() }, shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) { FilledTonalIconButton(onClick = click, modifier = Modifier.size(46.dp)) { Icon(icon, label) }; Spacer(Modifier.height(5.dp)); Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) } } }; repeat(4 - row.size) { Spacer(Modifier.weight(1f)) } } } }
}
@Composable private fun NativeActivity(x: JsonObject) { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Text(nativeInitials(x.nt("pelanggan_nama", "pelanggan", "nama")), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(x.nt("pelanggan_nama", "pelanggan", "nama"), fontWeight = FontWeight.SemiBold); Text(x.nt("created_at", "tanggal", "waktu"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text(NativeRp.format(x.nn("total", "nominal", "grand_total")), fontWeight = FontWeight.Bold) } } }
@Composable private fun NativeEmpty() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Inbox, null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(8.dp)); Text("Belum ada transaksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Transaksi baru akan muncul di sini", color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
@Composable private fun NativeError(message: String, retry: () -> Unit) { Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) { Column(Modifier.padding(14.dp)) { Text(message, color = MaterialTheme.colorScheme.onErrorContainer); TextButton(retry) { Text("Coba lagi") } } } }

@Composable private fun NativeTransactions(repo: Repository) {
    var q by remember { mutableStateOf("") }; var method by remember { mutableStateOf("") }; var status by remember { mutableStateOf("") }; var rows by remember { mutableStateOf(emptyList<JsonObject>()) }; var error by remember { mutableStateOf<String?>(null) }; var loading by remember { mutableStateOf(true) }; var add by remember { mutableStateOf(false) }; var detail by remember { mutableStateOf<JsonObject?>(null) }; val scope = rememberCoroutineScope()
    fun load() { scope.launch { loading = true; runCatching { repo.transaksi(q = q.trim().takeIf { it.isNotBlank() }, metodeBayar = method.takeIf { it.isNotBlank() }, statusKonfirmasi = status.takeIf { it.isNotBlank() }) }.onSuccess { rows = it.nitems(); error = null }.onFailure { error = ApiError.message(it) }; loading = false } }
    LaunchedEffect(Unit) { load() }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)) {
        item { Row(verticalAlignment = Alignment.CenterVertically) { Text("Transaksi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton(::load) { Icon(Icons.Default.Refresh, "Refresh") } } }
        item { OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(16.dp), placeholder = { Text("Cari ID, pelanggan, transaksi…") }, leadingIcon = { Icon(Icons.Default.Search, null) }) }
        item { NativeFilter(listOf("" to "Semua", "tunai" to "Tunai", "transfer" to "Transfer", "bon" to "Kasbon", "cash_tunai" to "Cash"), method) { method = it } }
        item { NativeFilter(listOf("" to "Semua status", "menunggu" to "Menunggu", "otomatis" to "Otomatis", "manual" to "Manual", "tidak_perlu" to "Tidak perlu"), status) { status = it } }
        item { Button(::load, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Terapkan filter") } }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(30.dp), Alignment.Center) { CircularProgressIndicator() } } else if (rows.isEmpty()) item { NativeEmpty() } else items(rows) { r -> Card(onClick = { detail = r }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(r.nt("nomor", "kode", "id"), fontWeight = FontWeight.Bold); Text(NativeRp.format(r.nn("total", "nominal", "grand_total")), fontWeight = FontWeight.Bold) }; Text(r.nt("pelanggan_nama", "pelanggan", "nama")); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(r.nt("metode_bayar", "metode"), style = MaterialTheme.typography.labelMedium); Text(r.nt("konfirmasi_pembayaran", "status_konfirmasi", "status"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) } } } }
        error?.let { item { NativeError(it, ::load) } }
    }
    FloatingActionButton({ add = true }, Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(18.dp)) { Icon(Icons.Default.Add, "Transaksi baru") }
    if (add) ModernCheckoutDialog(repo, { add = false; load() }, { add = false })
    detail?.let { NativeTransactionDetail(repo, it, { detail = null; load() }, { detail = null }) }
}

@Composable private fun NativeFilter(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { options.forEach { (value, label) -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, label = { Text(label) }) } } }
@Composable private fun NativeTransactionDetail(repo: Repository, row: JsonObject, done: () -> Unit, cancel: () -> Unit) { val scope = rememberCoroutineScope(); var delete by remember { mutableStateOf(false) }; var reason by remember { mutableStateOf("") }; var message by remember { mutableStateOf<String?>(null) }; var confirm by remember { mutableStateOf(row.nt("konfirmasi_pembayaran", "status_konfirmasi").takeUnless { it == "-" } ?: "menunggu") }; AlertDialog(onDismissRequest = cancel, title = { Text("Detail transaksi") }, text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 500.dp)) { item { Text("ID: ${row.nt("id", "nomor", "kode")}") }; item { Text("Pelanggan: ${row.nt("pelanggan_nama", "pelanggan")}") }; item { Text("Total: ${NativeRp.format(row.nn("total", "nominal", "grand_total"))}", fontWeight = FontWeight.Bold) }; item { Text("Metode: ${row.nt("metode_bayar", "metode")}") }; item { NativeFilter(listOf("menunggu" to "Menunggu", "otomatis" to "Otomatis", "manual" to "Manual", "tidak_perlu" to "Tidak perlu"), confirm) { confirm = it } }; item { Button({ scope.launch { runCatching { repo.updateTransaksiKonfirmasi(row.nt("id"), confirm) }.onSuccess { message = "Konfirmasi tersimpan" }.onFailure { message = ApiError.message(it) } } }) { Text("Simpan konfirmasi") } }; message?.let { item { Text(it, color = if (it == "Konfirmasi tersimpan") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) } }; if (delete) item { OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text("Alasan hapus") }) } } }, confirmButton = { if (!delete) Row { TextButton({ delete = true }) { Text("Hapus") }; TextButton(cancel) { Text("Tutup") } } else Button({ scope.launch { runCatching { repo.deleteTransaksi(row.nt("id"), reason.takeIf { it.isNotBlank() }) }.onSuccess { done() }.onFailure { message = ApiError.message(it) } } }) { Text("Konfirmasi hapus") } }, dismissButton = { if (delete) TextButton({ delete = false }) { Text("Batal") } }) }

@Composable private fun NativeCashier(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; var opening by remember { mutableStateOf(false) }; var closing by remember { mutableStateOf(false) }; var checkout by remember { mutableStateOf(false) }; var amount by remember { mutableStateOf("") }; var note by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    fun load() { scope.launch { loading = true; runCatching { data = repo.kasirCurrent() }.onFailure { error = ApiError.message(it) }; loading = false } }; LaunchedEffect(Unit) { load() }
    val state = data?.nt("status") ?: "-"; val accounts = data?.nitems().orEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
        item { Text("Kasir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Opening, transaksi, dan closing dalam satu alur", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("Status sesi", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(if (state == "buka") "Kasir sedang buka" else if (state == "tutup") "Kasir sudah ditutup" else "Belum buka", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Tanggal ${data?.nt("tanggal") ?: "-"}") } } }
        item { if (loading) LinearProgressIndicator(Modifier.fillMaxWidth()) else error?.let { NativeError(it, ::load) } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button({ amount = ""; opening = true }, Modifier.weight(1f), enabled = state != "buka", shape = RoundedCornerShape(14.dp)) { Text("Opening") }; OutlinedButton({ amount = ""; note = ""; closing = true }, Modifier.weight(1f), enabled = state == "buka", shape = RoundedCornerShape(14.dp)) { Text("Closing") } } }
        item { Button({ checkout = true }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.PointOfSale, null); Spacer(Modifier.width(8.dp)); Text("Transaksi penjualan") } }
        item { Text("Saldo per akun", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (accounts.isEmpty()) item { Text("Belum ada data saldo.", color = MaterialTheme.colorScheme.onSurfaceVariant) } else items(accounts) { a -> Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(a.nt("nama_akun", "nama")); Text(NativeRp.format(a.nn("saldo_sistem", "saldo")), fontWeight = FontWeight.Bold) } } }
    }
    if (opening) NativeAmountDialog("Opening Kasir", amount, { amount = it }, { val v = amount.toLongOrNull(); if (v != null && v >= 0) scope.launch { runCatching { repo.opening(listOf("Tunai Laci" to v)) }.onSuccess { opening = false; load() }.onFailure { error = ApiError.message(it) } } }, { opening = false })
    if (closing) AlertDialog(onDismissRequest = { closing = false }, title = { Text("Closing Kasir") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Saldo real tunai") }, singleLine = true); OutlinedTextField(note, { note = it }, label = { Text("Catatan") }, singleLine = true) } }, confirmButton = { Button({ val v = amount.toLongOrNull(); if (v != null && v >= 0) scope.launch { runCatching { repo.closing(listOf("Tunai Laci" to v), note) }.onSuccess { closing = false; load() }.onFailure { error = ApiError.message(it) } } }) { Text("Simpan") } }, dismissButton = { TextButton({ closing = false }) { Text("Batal") } })
    if (checkout) ModernCheckoutDialog(repo, { checkout = false; load() }, { checkout = false })
}
@Composable private fun NativeAmountDialog(title: String, value: String, onValue: (String) -> Unit, confirm: () -> Unit, cancel: () -> Unit) { AlertDialog(onDismissRequest = cancel, title = { Text(title) }, text = { OutlinedTextField(value, onValue, label = { Text("Nominal") }, singleLine = true) }, confirmButton = { Button(confirm) { Text("Simpan") } }, dismissButton = { TextButton(cancel) { Text("Batal") } }) }

@Composable private fun NativeOther(user: UserSession, repo: Repository, selected: String?, onSelect: (String?) -> Unit) {
    if (!selected.isNullOrBlank()) {
        Column(Modifier.fillMaxSize()) { Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { IconButton({ onSelect(null) }) { Icon(Icons.Default.ArrowBack, "Kembali") }; Column { Text("Menu Lainnya", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(nativeOtherTitle(selected), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; HorizontalDivider(); Box(Modifier.fillMaxSize().padding(8.dp)) { when (selected) { "pelanggan" -> PelangganScreen(repo); "kasbon" -> KasbonDetailScreen(repo); "service" -> ServiceHpScreen(repo); "akun" -> AkunUangScreen(repo); "ops" -> OperationsScreen(user, repo); "koreksi" -> CorrectionsScreen(repo) } } }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 30.dp)) {
            item { Text("Lainnya", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Fitur operasional dan administrasi", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { Text("Operasional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { NativeOtherGrid(onSelect, listOf(Triple("pelanggan", "Pelanggan", Icons.Default.People), Triple("kasbon", "Kasbon", Icons.Default.AccountBalanceWallet), Triple("service", "Service HP", Icons.Default.Build), Triple("akun", "Akun Uang", Icons.Default.AccountBalance))) }
            if (user.role.equals("admin", true)) { item { Text("Administrasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }; item { NativeOtherGrid(onSelect, listOf(Triple("ops", "Produk & Operasional", Icons.Default.Inventory2), Triple("koreksi", "Koreksi", Icons.Default.EditNote))) } }
        }
    }
}
@Composable private fun NativeOtherGrid(onSelect: (String?) -> Unit, entries: List<Triple<String, String, ImageVector>>) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { entries.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { (key, label, icon) -> Card(Modifier.weight(1f).clickable { onSelect(key) }, shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }; Spacer(Modifier.width(10.dp)); Text(label, fontWeight = FontWeight.SemiBold) } } }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } } }
private fun nativeOtherTitle(key: String) = when (key) { "pelanggan" -> "Pelanggan"; "kasbon" -> "Kasbon"; "service" -> "Service HP"; "akun" -> "Akun Uang"; "ops" -> "Produk & Operasional"; "koreksi" -> "Koreksi"; else -> "Fitur" }
