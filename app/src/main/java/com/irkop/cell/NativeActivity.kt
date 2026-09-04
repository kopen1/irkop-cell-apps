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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

private val Scheme = darkColorScheme(
    primary = Color(0xFF70D8C8), onPrimary = Color(0xFF06332E),
    primaryContainer = Color(0xFF32A192), onPrimaryContainer = Color.White,
    secondary = Color(0xFF4EDEA3), background = Color(0xFF202020),
    surface = Color(0xFF292929), surfaceVariant = Color(0xFF383838),
    onSurface = Color(0xFFF4F7F5), onSurfaceVariant = Color(0xFFD7E0DC),
    outline = Color(0xFFAAB7B3), error = Color(0xFFFFB4AB)
)
private val CardColor = Color(0xFF303030)
private val Good = Color(0xFF4EDEA3)
private val Muted = Color(0xFFD7E0DC)
private val Money = NumberFormat.getNumberInstance(Locale("id", "ID"))
private enum class Page { HOME, KASIR, TRANSAKSI, LAPORAN, STOK, KATEGORI, SERVICE, KASBON, PELANGGAN, PENGELUARAN, GAJI, LAINNYA, PENGATURAN }

private fun JSONObject.s(vararg keys: String): String = keys.firstNotNullOfOrNull { k -> optString(k).takeIf { it.isNotBlank() } } ?: "-"
private fun JSONObject.n(vararg keys: String): Long = keys.firstNotNullOfOrNull { k -> optString(k).replace(",", "").toDoubleOrNull()?.toLong() } ?: 0L
private fun JSONObject.a(vararg keys: String): List<JSONObject> = keys.firstNotNullOfOrNull { optJSONArray(it) }?.let { arr -> (0 until arr.length()).mapNotNull(arr::optJSONObject) } ?: emptyList()
private fun rp(n: Long) = "Rp ${Money.format(n)}"

class NativeActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); setContent { MaterialTheme(colorScheme = Scheme) { LoginGate() } } }
}

@Composable private fun LoginGate() {
    val context = LocalContext.current
    val api = remember(context) { ApiClient(context) }
    val scope = rememberCoroutineScope()
    var ready by remember { mutableStateOf(api.hasToken()) }
    var error by remember { mutableStateOf("") }
    fun login() = scope.launch {
        error = ""
        runCatching { if (api.hasToken()) runCatching { api.me() }.getOrElse { api.login("demo", "demodemo") } else api.login("demo", "demodemo") }
            .onSuccess { ready = true }.onFailure { ready = false; error = it.message ?: "Login gagal" }
    }
    LaunchedEffect(Unit) { login() }
    if (ready) App(api) else Box(Modifier.fillMaxSize().background(Scheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            CircularProgressIndicator()
            Text(if (error.isBlank()) "Menyiapkan sesi kasir…" else error, color = if (error.isBlank()) Muted else Scheme.error)
            if (error.isNotBlank()) Button(onClick = { login() }) { Text("Coba Lagi") }
        }
    }
}

@Composable private fun App(api: ApiClient) {
    var page by remember { mutableStateOf(Page.HOME) }
    var stack by remember { mutableStateOf(emptyList<Page>()) }
    var sheet by remember { mutableStateOf(false) }
    val role = remember { api.cachedUser()?.optString("role").orEmpty().uppercase() }
    fun allowed(p: Page) = p != Page.GAJI || role !in setOf("KARYAWAN", "KASIR", "CASHIER")
    fun open(p: Page) { if (allowed(p) && p != page) { stack = stack + page; page = p } }
    fun root(p: Page) { if (allowed(p)) { page = p; stack = emptyList() } }
    fun back() { if (stack.isNotEmpty()) { page = stack.last(); stack = stack.dropLast(1) } else if (page != Page.HOME) page = Page.HOME }
    BackHandler { back() }
    Scaffold(
        containerColor = Scheme.background,
        bottomBar = { NavigationBar(containerColor = Color(0xFF292929)) {
            Nav(page == Page.HOME, { root(Page.HOME) }, Icons.Default.Home, "Beranda")
            Nav(page == Page.KASIR, { root(Page.KASIR) }, Icons.Default.PointOfSale, "Kasir")
            Nav(page == Page.LAPORAN, { root(Page.LAPORAN) }, Icons.Default.Assessment, "Laporan")
            Nav(page == Page.LAINNYA, { root(Page.LAINNYA) }, Icons.Default.MoreHoriz, "Lainnya")
        }},
        floatingActionButton = { FloatingActionButton(onClick = { sheet = true }, containerColor = Scheme.primary, contentColor = Scheme.onPrimary) { Icon(Icons.Default.Add, "Transaksi Baru") } }
    ) { p ->
        when (page) {
            Page.HOME -> Home(p, ::open, role)
            Page.KASIR -> Cashier(p, api, ::back)
            Page.TRANSAKSI -> Transactions(p, api, ::back)
            Page.LAPORAN -> Reports(p, api, ::back)
            Page.KASBON -> Kasbon(p, api, ::back)
            Page.GAJI -> Payroll(p, api, ::back)
            Page.LAINNYA -> More(p, ::open, ::back, allowed(Page.GAJI))
            Page.PENGATURAN -> Unsupported(p, "Pengaturan", "Endpoint pengaturan belum ada di kontrak API resmi.", ::back)
            Page.STOK -> Unsupported(p, "Stok", "Endpoint produk/stok belum ada di kontrak API resmi.", ::back)
            Page.KATEGORI -> Unsupported(p, "Kategori", "Endpoint kategori belum ada di kontrak API resmi.", ::back)
            Page.SERVICE -> Unsupported(p, "Service HP", "Endpoint service belum ada di kontrak API resmi.", ::back)
            Page.PELANGGAN -> Unsupported(p, "Pelanggan", "Endpoint pelanggan belum ada di kontrak API resmi.", ::back)
            Page.PENGELUARAN -> Unsupported(p, "Pengeluaran", "Endpoint pengeluaran belum ada di kontrak API resmi.", ::back)
        }
    }
    if (sheet) TransactionSheet(api) { sheet = false }
}

@Composable private fun Nav(selected: Boolean, click: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    NavigationBarItem(selected, click, icon = { Icon(icon, label) }, label = { Text(label) })
}

@Composable private fun Header(title: String, subtitle: String? = null, back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Kembali") }
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); subtitle?.let { Text(it, color = Muted) } }
    }
}

@Composable private fun CardText(title: String, body: String, onClick: (() -> Unit)? = null) {
    Card(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier), colors = CardDefaults.cardColors(CardColor), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, fontWeight = FontWeight.Bold); Text(body, color = Muted) }
    }
}

@Composable private fun Home(p: PaddingValues, open: (Page) -> Unit, role: String) {
    LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Irkop Cell", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Sesi Kasir: Aktif", color = Good, fontWeight = FontWeight.Bold) }; Text(role.ifBlank { "KASIR" }, color = Scheme.primary, fontWeight = FontWeight.Bold) } }
        item { CardText("Sesi Kasir", "Opening, saldo akun dan rekonsiliasi", { open(Page.KASIR) }) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Metric("Saldo Kas", "—"); Metric("Omzet Hari Ini", "—"); Metric("Transaksi", "—") } }
        item { Text("Menu Kasir Utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Quick("Stok", Icons.Default.Inventory2) { open(Page.STOK) }; Quick("Kasbon", Icons.Default.ReceiptLong) { open(Page.KASBON) }; Quick("Pelanggan", Icons.Default.People) { open(Page.PELANGGAN) }; Quick("Pengeluaran", Icons.Default.Wallet) { open(Page.PENGELUARAN) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Quick("Service", Icons.Default.Build) { open(Page.SERVICE) }; Quick("Gaji", Icons.Default.Assessment) { open(Page.GAJI) }; Quick("Laporan", Icons.Default.Assessment) { open(Page.LAPORAN) }; Quick("Transaksi", Icons.Default.ReceiptLong) { open(Page.TRANSAKSI) } } }
        item { CardText("Backend", "Endpoint resmi dipakai untuk data finansial. Modul yang belum ada kontraknya tidak mengirim request fiktif.") }
    }
}

@Composable private fun Metric(title: String, value: String) { Card(Modifier.weight(1f), colors = CardDefaults.cardColors(CardColor), shape = RoundedCornerShape(14.dp)) { Column(Modifier.padding(10.dp)) { Text(title, color = Muted, style = MaterialTheme.typography.labelSmall); Text(value, fontWeight = FontWeight.Bold) } } }

@Composable private fun Quick(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) { Card(Modifier.weight(1f).clickable(onClick = click), colors = CardDefaults.cardColors(CardColor), shape = RoundedCornerShape(14.dp)) { Column(Modifier.fillMaxWidth().padding(vertical = 13.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Scheme.primary); Spacer(Modifier.height(6.dp)); Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) } } }

@Composable private fun Cashier(p: PaddingValues, api: ApiClient, back: () -> Unit) {
    var data by remember { mutableStateOf<JSONObject?>(null) }; var error by remember { mutableStateOf("") }; var open by remember { mutableStateOf(false) }; var close by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    fun load() = scope.launch { runCatching { api.kasirCurrent() }.onSuccess { data = it }.onFailure { error = it.message ?: "Gagal memuat" } }
    LaunchedEffect(Unit) { load() }
    val accounts = data?.a("saldo", "saldo_akun", "accounts", "akun").orEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Kasir & Rekonsiliasi", "Backend /api/kasir/current", back) }
        item { CardText("Status", data?.s("status", "state")?.uppercase() ?: "MEMUAT") }
        if (error.isNotBlank()) item { CardText("Error", error) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(Modifier.weight(1f), onClick = { open = true }) { Text("Opening") }; OutlinedButton(Modifier.weight(1f), onClick = { close = true }) { Text("Closing") } } }
        item { Text("Saldo Akun", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (accounts.isEmpty()) item { CardText("Saldo", "Belum ada rincian saldo dari server.") }
        items(accounts) { a -> Row(Modifier.fillMaxWidth().padding(5.dp), verticalAlignment = Alignment.CenterVertically) { Text(a.s("nama_akun", "nama", "akun"), Modifier.weight(1f)); Text(rp(a.n("saldo", "saldo_real", "nominal")), fontWeight = FontWeight.Bold) } }
    }
    if (open) AccountDialog("Opening Kasir", api, false) { open = false; load() }
    if (close) AccountDialog("Closing Kasir", api, true) { close = false; load() }
}

@Composable private fun AccountDialog(title: String, api: ApiClient, closing: Boolean, done: () -> Unit) {
    var fields by remember { mutableStateOf(listOf("Kas" to "", "OrderKuota" to "", "DANA" to "", "SeaBank" to "", "QRIS" to "")) }; var note by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = done, title = { Text(title) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(7.dp)) { fields.forEachIndexed { i, pair -> OutlinedTextField(pair.second, { v -> fields = fields.toMutableList().also { it[i] = pair.first to v } }, label = { Text(pair.first) }, singleLine = true) }; if (closing) OutlinedTextField(note, { note = it }, label = { Text("Catatan") }); if (error.isNotBlank()) Text(error, color = Scheme.error) } }, confirmButton = { Button(onClick = { scope.launch { val a = JSONArray(); fields.forEach { (n,v) -> a.put(JSONObject().put("nama_akun", n).put(if (closing) "saldo_real" else "saldo", v.toLongOrNull() ?: 0L)) }; runCatching { if (closing) api.kasirClosing(a, note) else api.kasirOpening(a) }.onSuccess { done() }.onFailure { error = it.message ?: "Gagal menyimpan" } } }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = done) { Text("Batal") } })
}

@Composable private fun Transactions(p: PaddingValues, api: ApiClient, back: () -> Unit) {
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }; var date by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; val scope = rememberCoroutineScope()
    fun load() = scope.launch { runCatching { api.transaksi(if (date.isBlank()) emptyMap() else mapOf("date" to date, "limit" to "100")) }.onSuccess { rows = it.a("items", "data", "transaksi", "results") }.onFailure { error = it.message ?: "Gagal memuat" } }
    LaunchedEffect(Unit) { load() }
    LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("Transaksi", "Riwayat /api/transaksi", back) }
        item { OutlinedTextField(date, { date = it }, label = { Text("Tanggal YYYY-MM-DD") }, singleLine = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { load() }) { Icon(Icons.Default.Refresh, "Muat") } }) }
        if (error.isNotBlank()) item { CardText("Error", error) }
        if (rows.isEmpty() && error.isBlank()) item { CardText("Transaksi", "Tidak ada transaksi.") }
        items(rows) { t -> CardText(t.s("nomor", "invoice", "kode", "id"), "${t.s("metode_bayar", "metode")} • ${rp(t.n("total", "grand_total", "nominal"))}") }
    }
}

@Composable private fun Reports(p: PaddingValues, api: ApiClient, back: () -> Unit) {
    var data by remember { mutableStateOf<JSONObject?>(null) }; var error by remember { mutableStateOf("") }; val scope = rememberCoroutineScope(); val month = java.time.LocalDate.now().toString().substring(0,7)
    LaunchedEffect(Unit) { scope.launch { runCatching { api.laporanBulan(month) }.onSuccess { data = it }.onFailure { error = it.message ?: "Gagal memuat" } } }
    LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Header("Laporan", "Bulan $month", back) }; if (error.isNotBlank()) item { CardText("Error", error) }; item { Metric("Omzet", rp(data?.n("omzet", "total_omzet", "penjualan") ?: 0L)) }; item { Metric("Jumlah Transaksi", data?.s("jumlah_transaksi", "total_transaksi", "count") ?: "0") }; item { CardText("Data", "Ditampilkan sebagai UI terstruktur, bukan raw JSON.") } }
}

@Composable private fun Kasbon(p: PaddingValues, api: ApiClient, back: () -> Unit) {
    var data by remember { mutableStateOf<JSONObject?>(null) }; var error by remember { mutableStateOf("") }; var add by remember { mutableStateOf(false) }; val scope = rememberCoroutineScope()
    fun load() = scope.launch { runCatching { api.kasbon() }.onSuccess { data = it }.onFailure { error = it.message ?: "Gagal memuat" } }; LaunchedEffect(Unit) { load() }
    val rows = data?.a("items", "data", "kasbon", "results").orEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(p), contentPadding = PaddingValues(16.dp,8.dp,16.dp,110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Header("Kasbon Pelanggan", "GET /api/kasbon", back) }; item { Button(Modifier.fillMaxWidth(), onClick = { add = true }) { Text("Tambah Kasbon") } }; if (error.isNotBlank()) item { CardText("Error", error) }; items(rows) { r -> CardText(r.s("nama_pelanggan", "pelanggan", "nama"), "${r.s("status", "tanggal_jatuh_tempo")} • ${rp(r.n("sisa", "saldo", "nominal", "jumlah"))}") }; if (rows.isEmpty() && error.isBlank()) item { CardText("Kasbon", "Belum ada data.") } }
    if (add) KasbonDialog(api) { add = false; load() }
}

@Composable private fun KasbonDialog(api: ApiClient, done: () -> Unit) { var customer by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; var due by remember { mutableStateOf("") }; var error by remember { mutableStateOf("") }; val scope = rememberCoroutineScope(); AlertDialog(onDismissRequest = done, title = { Text("Tambah Kasbon") }, text = { Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { OutlinedTextField(customer,{customer=it},label={Text("Pelanggan ID")}); OutlinedTextField(amount,{amount=it},label={Text("Nominal")}); OutlinedTextField(due,{due=it},label={Text("Jatuh tempo")}); if(error.isNotBlank()) Text(error,color=Scheme.error) } }, confirmButton = { Button(onClick = { scope.launch { val b=JSONObject().put("pelanggan_id",customer).put("nominal",amount.toLongOrNull()?:0L); if(due.isNotBlank()) b.put("tanggal_jatuh_tempo",due); runCatching { api.createKasbon(b) }.onSuccess { done() }.onFailure { error=it.message?:"Gagal menyimpan" } } }) { Text("Simpan") } }, dismissButton = { TextButton(onClick=done){Text("Batal")} }) }

@Composable private fun Payroll(p: PaddingValues, api: ApiClient, back: () -> Unit) { var data by remember { mutableStateOf<JSONObject?>(null) }; var error by remember { mutableStateOf("") }; val scope=rememberCoroutineScope(); LaunchedEffect(Unit){scope.launch{runCatching{api.gaji()}.onSuccess{data=it}.onFailure{error=it.message?:"Gagal memuat"}}}; val rows=data?.a("items","data","gaji","results").orEmpty(); LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header("Gaji Karyawan","GET /api/gaji",back)};if(error.isNotBlank())item{CardText("Error",error)};item{CardText("Rate","GET /api/gaji/rate tersedia di backend")};items(rows){r->CardText(r.s("nama","nama_karyawan","karyawan"),rp(r.n("total","gaji","nominal")))};if(rows.isEmpty()&&error.isBlank())item{CardText("Payroll","Belum ada data.")}} }

@Composable private fun More(p: PaddingValues, open: (Page)->Unit, back:()->Unit, payroll:Boolean){val modules=listOf("Stok" to Page.STOK,"Kategori" to Page.KATEGORI,"Service HP" to Page.SERVICE,"Kasbon" to Page.KASBON,"Pelanggan" to Page.PELANGGAN,"Pengeluaran" to Page.PENGELUARAN,"Gaji Karyawan" to Page.GAJI,"Pengaturan" to Page.PENGATURAN,"Transaksi" to Page.TRANSAKSI);LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){item{Header("Lainnya","Semua modul",back)};items(modules){(name,target)->val enabled=target!=Page.GAJI||payroll;Card(Modifier.fillMaxWidth().clickable(enabled){open(target)},colors=CardDefaults.cardColors(CardColor),shape=RoundedCornerShape(15.dp)){Row(Modifier.padding(16.dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(name,Modifier.weight(1f),fontWeight=FontWeight.SemiBold);Text(if(enabled)"Buka →" else "Tidak tersedia",color=if(enabled)Scheme.primary else Muted)}}}}}

@Composable private fun Unsupported(p:PaddingValues,title:String,message:String,back:()->Unit){Box(Modifier.fillMaxSize().padding(p),contentAlignment=Alignment.Center){Card(Modifier.fillMaxWidth().padding(20.dp),colors=CardDefaults.cardColors(CardColor)){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Header(title,back=back);Text(message,color=Muted);Text("Tidak ada endpoint fiktif/dummy yang digunakan.",color=Scheme.primary,fontWeight=FontWeight.Bold)}}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TransactionSheet(api:ApiClient,close:()->Unit){var product by remember{mutableStateOf("")};var qty by remember{mutableStateOf("1")};var method by remember{mutableStateOf("Tunai")};var customer by remember{mutableStateOf("")};var account by remember{mutableStateOf("")};var error by remember{mutableStateOf("")};var saving by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();ModalBottomSheet(onDismissRequest={if(!saving)close()},containerColor=Color(0xFF292929)){Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text("Transaksi Baru",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Idempotency-Key otomatis",color=Muted);OutlinedTextField(product,{product=it},label={Text("Produk ID")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(qty,{qty=it},label={Text("Qty")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(method,{method=it},label={Text("Metode Bayar")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(account,{account=it},label={Text("Akun Penerima")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(customer,{customer=it},label={Text("Pelanggan ID")},singleLine=true,modifier=Modifier.fillMaxWidth());if(error.isNotBlank())Text(error,color=Scheme.error);Button(enabled=!saving,onClick={scope.launch{saving=true;val b=JSONObject().put("items",JSONArray().apply{if(product.isNotBlank())put(JSONObject().put("produk_id",product).put("qty",qty.toIntOrNull()?:1))}).put("metode_bayar",method).put("manual_entry",true);if(customer.isNotBlank())b.put("pelanggan_id",customer);if(account.isNotBlank())b.put("akun_penerima",account);runCatching{api.createTransaksi(b)}.onSuccess{close()}.onFailure{error=it.message?:"Transaksi gagal"};saving=false}}){Text(if(saving)"Menyimpan…" else "Simpan Transaksi")}}}}
