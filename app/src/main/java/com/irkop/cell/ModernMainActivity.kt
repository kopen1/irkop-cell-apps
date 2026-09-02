@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.ApiError
import com.irkop.cell.core.AuthPolicy
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.*
import com.irkop.cell.ui.AppViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val Rp = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun rp(value: Long?): String = Rp.format(value ?: 0L)
private fun JsonObject.text(vararg keys: String): String = keys.firstNotNullOfOrNull { string(it)?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.num(vararg keys: String): Long = keys.firstNotNullOfOrNull { long(it) } ?: 0L
private fun JsonObject.rows(): List<JsonObject> = array("items")?.filterIsInstance<JsonObject>().orEmpty()
private fun initials(name: String): String = name.trim().split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "IC" }

class ModernMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(applicationContext)
        val repo = Repository(ApiClient(session).api)
        setContent {
            val appVm: AppViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppViewModel(session, repo) as T
            })
            IrkopTheme { Root(appVm, repo) }
        }
    }
}

@Composable private fun Root(vm: AppViewModel, repo: Repository) {
    val state by vm.state.collectAsState()
    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        state.user == null -> Login(state.error, vm::login, vm::clearError)
        else -> AppShell(state.user!!, vm::logout, repo)
    }
}

@Composable private fun Login(error: String?, login: (String, String) -> Unit, clear: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(72.dp).clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) { Icon(Icons.Default.PhoneAndroid, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp)) }
        Spacer(Modifier.height(18.dp)); Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge)
        Text("POS & Buku Kas Digital", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { login(username.trim(), password) }, enabled = username.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Text("Masuk") }
        if (!error.isNullOrBlank()) { Spacer(Modifier.height(10.dp)); Text(error, color = MaterialTheme.colorScheme.error); LaunchedEffect(error) { clear() } }
    }
}

private data class AppTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val enabled: Boolean)

@Composable private fun AppShell(user: UserSession, logout: () -> Unit, repo: Repository) {
    var tab by remember { mutableStateOf("dashboard") }
    val tabs = listOf(
        AppTab("dashboard", "Dashboard", Icons.Default.Home, AuthPolicy.canAccess(user, AuthPolicy.DASHBOARD)),
        AppTab("transaksi", "Transaksi", Icons.Default.ReceiptLong, AuthPolicy.canAccess(user, AuthPolicy.TRANSAKSI)),
        AppTab("kasir", "Kasir", Icons.Default.PointOfSale, AuthPolicy.canAccess(user, AuthPolicy.KASIR)),
        AppTab("laporan", "Laporan", Icons.Default.Assessment, AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)),
        AppTab("lainnya", "Lainnya", Icons.Default.MoreHoriz, true),
    )
    if (tabs.none { it.route == tab && it.enabled }) tab = tabs.firstOrNull { it.enabled }?.route ?: "lainnya"
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (tab == "dashboard") TopAppBar(
                title = { Column { Text("IRKOP CELL"); Text(user.nama.ifBlank { user.username }, style = MaterialTheme.typography.labelSmall) } },
                navigationIcon = { IconButton(onClick = { tab = "lainnya" }) { Icon(Icons.Default.Menu, "Menu") } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.NotificationsNone, "Notifikasi") }; IconButton(onClick = logout) { Icon(Icons.Default.Logout, "Keluar") } },
            )
        },
        bottomBar = { NavigationBar { tabs.forEach { item -> NavigationBarItem(selected = tab == item.route, enabled = item.enabled, onClick = { if (item.enabled) tab = item.route }, icon = { Icon(item.icon, item.label) }, label = { Text(item.label) }) } } },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                "dashboard" -> Dashboard(repo, { tab = "transaksi" }, { tab = "kasir" }, { tab = "lainnya" }, { tab = "laporan" })
                "transaksi" -> Transactions(repo)
                "kasir" -> Cashier(repo)
                "laporan" -> Report(repo)
                else -> ParityExtrasScreen(user, repo)
            }
        }
    }
}

@Composable private fun Dashboard(repo: Repository, goTx: () -> Unit, goCash: () -> Unit, goOther: () -> Unit, goReport: () -> Unit) {
    var tx by remember { mutableStateOf<JsonObject?>(null) }; var kb by remember { mutableStateOf<JsonObject?>(null) }; var pl by remember { mutableStateOf<JsonObject?>(null) }; var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { coroutineScope { val a = async { runCatching { repo.transaksi(tanggal = LocalDate.now().toString()) }.getOrNull() }; val b = async { runCatching { repo.kasbon() }.getOrNull() }; val c = async { runCatching { repo.pelanggan() }.getOrNull() }; val all = awaitAll(a, b, c); tx = all[0]; kb = all[1]; pl = all[2]; loading = false } }
    val rows = tx?.rows().orEmpty(); val omzet = tx?.num("total_nilai", "total_omzet", "omzet") ?: rows.sumOf { it.num("total", "nominal", "grand_total") }; val count = tx?.num("jumlah_transaksi", "transaksi", "total_items")?.takeIf { it > 0 } ?: rows.size.toLong(); val debt = kb?.rows()?.count { it.text("status", "status_pembayaran").contains("belum", true) } ?: 0; val customers = pl?.num("jumlah_pelanggan", "total_pelanggan")?.takeIf { it > 0 } ?: pl?.rows()?.size?.toLong() ?: 0
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)) {
        item { Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { listOf("Pelanggan", "Kasbon", "Service HP", "Akun Uang").forEachIndexed { i, label -> FilterChip(selected = i == 0, onClick = {}, label = { Text(label) }) } } }
        item { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) { Column(Modifier.padding(18.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ringkasan Hari Ini", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium); AssistChip(onClick = {}, label = { Text("Hari Ini") }) }; Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Stat("Pendapatan", rp(omzet), "↑ 0%", Modifier.weight(1f)); Stat("Transaksi", count.toString(), "↑ 0%", Modifier.weight(1f)) }; Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Stat("Kasbon", debt.toString(), "↓ 0%", Modifier.weight(1f)); Stat("Pelanggan", customers.toString(), "↑ 0%", Modifier.weight(1f)) } } } }
        item { Text("Menu Cepat", style = MaterialTheme.typography.titleLarge) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Quick("Transaksi", Icons.Default.ReceiptLong, goTx); Quick("Kasir", Icons.Default.PointOfSale, goCash); Quick("Kasbon", Icons.Default.AccountBalanceWallet, goOther); Quick("Laporan", Icons.Default.Assessment, goReport); Quick("Stok", Icons.Default.Inventory2, goOther) } }
        item { Text("Aktivitas Terakhir", style = MaterialTheme.typography.titleLarge) }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } } else if (rows.isEmpty()) item { Empty("Belum ada transaksi", "Transaksi baru akan tampil di sini.") } else items(rows.take(8)) { Activity(it) }
    }
}

@Composable private fun Stat(label: String, value: String, trend: String, modifier: Modifier) { Card(modifier, shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .82f))) { Column(Modifier.padding(11.dp)) { Text(label, style = MaterialTheme.typography.labelSmall); Text(value, style = MaterialTheme.typography.titleMedium); Text(trend, style = MaterialTheme.typography.labelSmall, color = if (trend.startsWith("↑")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) } } }
@Composable private fun Quick(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, click: () -> Unit) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(58.dp)) { FilledTonalIconButton(onClick = click, modifier = Modifier.size(48.dp)) { Icon(icon, label) }; Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) } }
@Composable private fun Activity(row: JsonObject) { val name=row.text("pelanggan_nama","pelanggan","nama"); Card(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Text(initials(name),color=MaterialTheme.colorScheme.primary)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(name,style=MaterialTheme.typography.titleMedium);Text(row.text("created_at","tanggal"),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(rp(row.num("total","nominal","grand_total")),style=MaterialTheme.typography.labelMedium)}}}
@Composable private fun Empty(title:String,subtitle:String){Card(Modifier.fillMaxWidth()){Column(Modifier.fillMaxWidth().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Inbox,null,modifier=Modifier.size(32.dp),tint=MaterialTheme.colorScheme.primary);Text(title,style=MaterialTheme.typography.titleMedium);Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}

@Composable private fun Transactions(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }; var query by remember { mutableStateOf("") }; var filter by remember { mutableStateOf("Semua") }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; var create by remember { mutableStateOf(false) }; val scope=rememberCoroutineScope()
    suspend fun load(active:String=filter){loading=true;runCatching{repo.transaksi(q=query.trim().takeIf(String::isNotBlank),metodeBayar=if(active=="Kasbon")"bon"else null)}.onSuccess{rows=it.rows()}.onFailure{error=ApiError.message(it)};loading=false}
    LaunchedEffect(Unit){load()}
    Box(Modifier.fillMaxSize()){LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(11.dp),contentPadding=PaddingValues(top=12.dp,bottom=92.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("Transaksi",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.weight(1f));IconButton(onClick={scope.launch{load()}}){Icon(Icons.Default.Refresh,"Refresh")};IconButton(onClick={}){Icon(Icons.Default.FilterList,"Filter")}}};item{OutlinedTextField(query,{query=it},placeholder={Text("Cari transaksi")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true,modifier=Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Semua","Penjualan","Pembelian","Kasbon").forEach{label->FilterChip(selected=filter==label,onClick={filter=label;scope.launch{load(label)}},label={Text(label)})}}};if(loading)item{Box(Modifier.fillMaxWidth().padding(20.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()}}else if(rows.isEmpty())item{Empty("Tidak ada transaksi","Coba ubah pencarian atau filter.")}else items(rows){TxCard(it)};error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}};FloatingActionButton(onClick={create=true},modifier=Modifier.align(Alignment.BottomEnd).padding(16.dp),containerColor=MaterialTheme.colorScheme.primary,contentColor=MaterialTheme.colorScheme.onPrimary){Icon(Icons.Default.Add,"Transaksi")}}
    if(create)Checkout(repo,{create=false;scope.launch{load()}},{create=false})
}

@Composable private fun TxCard(row: JsonObject) { val name=row.text("pelanggan_nama","pelanggan","nama"); val method=row.text("metode_bayar","metode"); val status=row.text("status_konfirmasi","status"); Card(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=.12f)),contentAlignment=Alignment.Center){Text(initials(name),color=MaterialTheme.colorScheme.primary)};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(row.text("nomor","kode","id"),style=MaterialTheme.typography.titleMedium);Text(name);Text(row.text("created_at","tanggal"),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Column(horizontalAlignment=Alignment.End){Text(rp(row.num("total","nominal","grand_total")),style=MaterialTheme.typography.titleMedium);Text(if(method=="bon")"Kasbon"else status,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}}}

private data class Cart(val product: JsonObject, val qty: Int)
private fun addCart(cart: List<Cart>, product: JsonObject): List<Cart> = cart.firstOrNull { it.product.text("id") == product.text("id") }?.let { cart.map { x -> if (x.product.text("id") == product.text("id")) x.copy(qty=x.qty+1) else x } } ?: cart + Cart(product,1)
private fun decCart(cart: List<Cart>, id: String): List<Cart> = cart.mapNotNull { if(it.product.text("id")!=id) it else it.copy(qty=it.qty-1).takeIf{z->z.qty>0} }

@Composable private fun Cashier(repo: Repository) {
    var products by remember { mutableStateOf(emptyList<JsonObject>()) }; var cart by remember { mutableStateOf(emptyList<Cart>()) }; var query by remember { mutableStateOf("") }; var method by remember { mutableStateOf("tunai") }; var customer by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var message by remember { mutableStateOf<String?>(null) }; val scope=rememberCoroutineScope()
    LaunchedEffect(Unit){runCatching{repo.produk()}.onSuccess{products=it.rows()}.onFailure{message=ApiError.message(it)}}
    val shown=products.filter{query.isBlank()||it.text("nama","sku").contains(query,true)};val total=cart.sumOf{it.product.num("harga","harga_jual")*it.qty}
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(11.dp),contentPadding=PaddingValues(top=12.dp,bottom=24.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("Kasir",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.weight(1f));Text("Checkout",color=MaterialTheme.colorScheme.primary)}};item{OutlinedTextField(query,{query=it},placeholder={Text("Cari produk / scan barcode")},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={IconButton(onClick={}){Icon(Icons.Default.QrCodeScanner,"Scan")}},singleLine=true,modifier=Modifier.fillMaxWidth())};if(cart.isNotEmpty()){item{Text("Keranjang",style=MaterialTheme.typography.titleLarge)};items(cart){x->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Inventory2,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Column(Modifier.weight(1f)){Text(x.product.text("nama"),style=MaterialTheme.typography.titleMedium);Text("${x.qty} × ${rp(x.product.num("harga","harga_jual"))}")};IconButton(onClick={cart=decCart(cart,x.product.text("id"))}){Icon(Icons.Default.Remove,"Kurangi")};Text(x.qty.toString());IconButton(onClick={cart=addCart(cart,x.product)}){Icon(Icons.Default.Add,"Tambah")}}}};item{Card{Column(Modifier.padding(14.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Subtotal");Text(rp(total))};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Diskon");Text(rp(0))};HorizontalDivider(Modifier.padding(vertical=7.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Total",style=MaterialTheme.typography.titleLarge);Text(rp(total),style=MaterialTheme.typography.titleLarge)}}}}};item{Text("Produk",style=MaterialTheme.typography.titleLarge)};items(shown.take(30)){p->Card(onClick={cart=addCart(cart,p)},modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(11.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Inventory2,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Column(Modifier.weight(1f)){Text(p.text("nama"),style=MaterialTheme.typography.titleMedium);Text("Stok ${p.num("stok")}",style=MaterialTheme.typography.bodySmall)};Text(rp(p.num("harga","harga_jual")),style=MaterialTheme.typography.titleMedium)}}};item{Text("Metode Pembayaran",style=MaterialTheme.typography.titleLarge)};item{Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("tunai","transfer","bon","lainnya").forEach{v->FilterChip(selected=method==v,onClick={method=v},label={Text(v.replaceFirstChar{it.uppercase()})})}}};item{OutlinedTextField(customer,{customer=it.filter(Char::isDigit)},label={Text("Pelanggan ID untuk Kasbon")},singleLine=true,modifier=Modifier.fillMaxWidth())};item{Button(onClick={if(method=="bon"&&customer.toLongOrNull()==null){message="Pelanggan wajib untuk kasbon."}else{busy=true;scope.launch{runCatching{repo.createTransaksi(buildJsonObject{putJsonArray("items"){cart.forEach{add(buildJsonObject{put("produk_id",it.product["id"]?:JsonNull);put("qty",it.qty)})}};put("metode_bayar",if(method=="lainnya")"cash_tunai"else method);customer.toLongOrNull()?.let{put("pelanggan_id",it)}})}.onSuccess{cart=emptyList();message="Pembayaran berhasil disimpan."}.onFailure{message=ApiError.message(it)};busy=false}}},enabled=cart.isNotEmpty()&&total>0&&!busy,modifier=Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium){Text(if(busy)"Memproses…"else"Bayar ${rp(total)}")};message?.let{item{Text(it,color=if(it.contains("berhasil"))MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}}}
}

@Composable private fun Report(repo: Repository) {
    var month by remember { mutableStateOf(LocalDate.now().toString().substring(0,7)) }; var data by remember { mutableStateOf<JsonObject?>(null) }; var loading by remember { mutableStateOf(true) }; var error by remember { mutableStateOf<String?>(null) }; val scope=rememberCoroutineScope()
    suspend fun load(){loading=true;runCatching{repo.laporanBulan(month)}.onSuccess{data=it}.onFailure{error=ApiError.message(it)};loading=false};LaunchedEffect(Unit){load()}
    val summary=data?.obj("summary")?:data?.obj("ringkasan")?:data;val series=data?.array("harian")?.filterIsInstance<JsonObject>()?:data?.array("daily")?.filterIsInstance<JsonObject>().orEmpty()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(11.dp),contentPadding=PaddingValues(top=12.dp,bottom=24.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("Laporan",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.weight(1f));Icon(Icons.Default.CalendarMonth,"Tanggal")}};item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Ringkasan","Penjualan","Kasbon","Produk").forEachIndexed{i,label->FilterChip(selected=i==0,onClick={},label={Text(label)})}}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){OutlinedTextField(month,{month=it},label={Text("Periode YYYY-MM")},modifier=Modifier.weight(1f),singleLine=true);Button(onClick={scope.launch{load()}}){Text("Muat")}}};if(loading)item{Box(Modifier.fillMaxWidth().padding(20.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()}}else{item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Pendapatan",rp(summary?.num("omzet","total_omzet")),Modifier.weight(1f));Metric("Transaksi",summary?.num("jumlah_transaksi","transaksi","total_items").toString(),Modifier.weight(1f))}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Kasbon",rp(summary?.num("kasbon","kasbon_aktif")),Modifier.weight(1f));Metric("Pelanggan",summary?.num("pelanggan","jumlah_pelanggan").toString(),Modifier.weight(1f))}};item{Chart(series)};item{Card{Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text("Ringkasan Keuangan",style=MaterialTheme.typography.titleMedium);listOf("Omzet" to summary?.num("omzet","total_omzet"),"Laba" to summary?.num("laba","total_laba"),"Pengeluaran" to summary?.num("pengeluaran","total_pengeluaran"),"Kasbon" to summary?.num("kasbon","kasbon_aktif"),"Net" to summary?.num("net","laba_bersih")).forEach{(k,v)->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(k);Text(rp(v))}}}}}};error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}}
}
@Composable private fun Metric(label:String,value:String,modifier:Modifier){Card(modifier){Column(Modifier.padding(13.dp)){Text(label,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,style=MaterialTheme.typography.titleLarge);Text("Periode terpilih",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}}
@Composable private fun Chart(rows:List<JsonObject>){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Grafik Pendapatan",style=MaterialTheme.typography.titleMedium);AssistChip(onClick={},label={Text("Per Hari")})};Spacer(Modifier.height(8.dp));if(rows.isEmpty())Text("Data harian belum tersedia dari API.",color=MaterialTheme.colorScheme.onSurfaceVariant)else{val values=rows.map{it.num("omzet","pendapatan","total","nominal")};Canvas(Modifier.fillMaxWidth().height(170.dp)){val max=(values.maxOrNull()?:1L).coerceAtLeast(1L).toFloat();val step=if(values.size<2)size.width else size.width/(values.size-1);val path=Path();values.forEachIndexed{i,v->{val x=step*i;val y=size.height-v.toFloat()/max*size.height;if(i==0)path.moveTo(x,y)else path.lineTo(x,y);drawCircle(MaterialTheme.colorScheme.primary,5f,androidx.compose.ui.geometry.Offset(x,y))}};drawPath(path,MaterialTheme.colorScheme.primary,style=Stroke(5f,cap=StrokeCap.Round))}}}}}

@Composable private fun Checkout(repo: Repository, done: () -> Unit, cancel: () -> Unit) {
    var products by remember { mutableStateOf(emptyList<JsonObject>()) }; var cart by remember { mutableStateOf(emptyList<Cart>()) }; var method by remember { mutableStateOf("tunai") }; var customer by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }; var error by remember { mutableStateOf<String?>(null) }; val scope=rememberCoroutineScope()
    LaunchedEffect(Unit){runCatching{repo.produk()}.onSuccess{products=it.rows()}.onFailure{error=ApiError.message(it)}};val total=cart.sumOf{it.product.num("harga","harga_jual")*it.qty}
    AlertDialog(onDismissRequest=cancel,title={Text("Transaksi Baru")},text={LazyColumn(Modifier.heightIn(max=520.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){if(cart.isEmpty())item{Empty("Keranjang kosong","Pilih produk di bawah.")};items(cart){x->Card{Row(Modifier.padding(9.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(x.product.text("nama"));Text("${x.qty} × ${rp(x.product.num("harga","harga_jual"))}")};IconButton(onClick={cart=decCart(cart,x.product.text("id"))}){Icon(Icons.Default.Remove,"Kurangi")};IconButton(onClick={cart=addCart(cart,x.product)}){Icon(Icons.Default.Add,"Tambah")}}}};items(products.take(20)){p->Card(onClick={cart=addCart(cart,p)}){Row(Modifier.padding(9.dp)){Text(p.text("nama"),Modifier.weight(1f));Text(rp(p.num("harga","harga_jual")))}}};item{OutlinedTextField(customer,{customer=it.filter(Char::isDigit)},label={Text("Pelanggan ID")},modifier=Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("tunai","transfer","bon","cash_tunai").forEach{v->FilterChip(selected=method==v,onClick={method=v},label={Text(v.replace('_',' ').replaceFirstChar{it.uppercase()})})}}};item{Text("Total ${rp(total)}",style=MaterialTheme.typography.titleLarge)};error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}}},confirmButton={Button(onClick={if(method=="bon"&&customer.toLongOrNull()==null){error="Pelanggan wajib untuk kasbon."}else{busy=true;scope.launch{runCatching{repo.createTransaksi(buildJsonObject{putJsonArray("items"){cart.forEach{add(buildJsonObject{put("produk_id",it.product["id"]?:JsonNull);put("qty",it.qty)})}};put("metode_bayar",method);customer.toLongOrNull()?.let{put("pelanggan_id",it)}})}.onSuccess{done()}.onFailure{error=ApiError.message(it)};busy=false}}},enabled=cart.isNotEmpty()&&total>0&&!busy){Text(if(busy)"Memproses…"else"Simpan")}},dismissButton={TextButton(onClick=cancel){Text("Batal")}})
}
