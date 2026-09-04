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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val AppScheme = darkColorScheme(
    primary = Color(0xFF70D8C8), onPrimary = Color(0xFF003731), primaryContainer = Color(0xFF32A192),
    onPrimaryContainer = Color(0xFF00201B), secondary = Color(0xFF4EDEA3), tertiary = Color(0xFFADC6FF),
    background = Color(0xFF131313), onBackground = Color(0xFFE5E2E1), surface = Color(0xFF131313),
    onSurface = Color(0xFFE5E2E1), surfaceVariant = Color(0xFF353535), onSurfaceVariant = Color(0xFFBCC9C5),
    outline = Color(0xFF879390), error = Color(0xFFFFB4AB)
)
private val Low = Color(0xFF1B1B1C)
private val Container = Color(0xFF202020)
private val High = Color(0xFF2A2A2A)
private val Muted = Color(0xFFBCC9C5)
private val Success = Color(0xFF4EDEA3)

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = AppScheme) { AutoLoginScreen() } }
    }
}

private enum class Page { HOME, KASIR, TRANSAKSI, LAPORAN, STOK, KATEGORI, SERVICE, KASBON, PELANGGAN, PENGELUARAN, GAJI, LAINNYA, PENGATURAN }

@Composable
private fun AutoLoginScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember(context) { ApiClient(context) }
    var ready by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    fun login() = scope.launch {
        error = ""
        try { api.login("demo", "demodemo"); ready = true } catch (e: Exception) { error = e.message ?: "Login demo gagal" }
    }
    LaunchedEffect(Unit) { login() }
    if (!ready) {
        Box(Modifier.fillMaxSize().background(AppScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                CircularProgressIndicator(color = AppScheme.primary)
                Text(if (error.isBlank()) "Menyiapkan sesi kasir…" else error, color = if (error.isBlank()) Muted else AppScheme.error)
                if (error.isNotBlank()) Button(onClick = { login() }) { Text("Coba Lagi") }
            }
        }
    } else AppShell(api)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppShell(api: ApiClient) {
    var page by remember { mutableStateOf(Page.HOME) }
    var showTransaction by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = AppScheme.background,
        bottomBar = {
            NavigationBar(containerColor = Low) {
                navItem("Beranda", Icons.Default.Home, page == Page.HOME) { page = Page.HOME }
                navItem("Kasir", Icons.Default.PointOfSale, page == Page.KASIR) { page = Page.KASIR }
                navItem("Laporan", Icons.Default.Assessment, page == Page.LAPORAN) { page = Page.LAPORAN }
                navItem("Lainnya", Icons.Default.MoreHoriz, page == Page.LAINNYA) { page = Page.LAINNYA }
            }
        },
        floatingActionButton = {
            if (page != Page.TRANSAKSI && page != Page.PENGATURAN) FloatingActionButton(
                onClick = { showTransaction = true }, containerColor = AppScheme.primary, contentColor = AppScheme.onPrimary
            ) { Icon(Icons.Default.Add, "Transaksi Baru") }
        }
    ) { padding ->
        when (page) {
            Page.HOME -> HomePage(padding, { showSearch = true }, { page = it })
            Page.KASIR -> CashierPage(padding, api)
            Page.LAPORAN -> ReportPage(padding, api, { showSearch = true })
            Page.LAINNYA -> MorePage(padding) { page = it }
            Page.STOK -> StockPage(padding, api)
            Page.KATEGORI -> CategoryPage(padding)
            Page.SERVICE -> ServicePage(padding, api)
            Page.KASBON -> KasbonPage(padding, api)
            Page.PELANGGAN -> CustomerPage(padding, api)
            Page.PENGELUARAN -> ExpensePage(padding, api)
            Page.GAJI -> PayrollPage(padding, api)
            Page.PENGATURAN -> SettingsPage(padding, api)
            Page.TRANSAKSI -> TransactionPage(padding, api) { page = Page.HOME }
        }
    }
    if (showTransaction) TransactionSheet(api, { showTransaction = false })
    if (showSearch) SearchDialog { showSearch = false }
}

@Composable private fun androidx.compose.foundation.layout.ColumnScope.navItem(label: String, icon: ImageVector, selected: Boolean, action: () -> Unit) {}

@Composable
private fun NavHostItem(label: String, icon: ImageVector, selected: Boolean, action: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = action, icon = { Icon(icon, label) }, label = { Text(label) })
}

private fun androidx.compose.material3.NavigationBarScope.navItem(label: String, icon: ImageVector, selected: Boolean, action: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = action, icon = { Icon(icon, label) }, label = { Text(label) })
}

@Composable
private fun PageHeader(title: String, subtitle: String? = null, back: (() -> Unit)? = null, action: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, "Kembali") }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        if (action != null) IconButton(onClick = action) { Icon(Icons.Default.Add, "Tambah") }
    }
}

@Composable
private fun HomePage(padding: PaddingValues, search: () -> Unit, go: (Page) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Irkop Cell", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Sesi Kasir: Aktif", color = Success, fontWeight = FontWeight.Bold) }; IconButton(onClick = search) { Icon(Icons.Default.Search, "Cari") }; Surface(Modifier.size(40.dp), CircleShape, AppScheme.primaryContainer) { Box(contentAlignment = Alignment.Center) { Text("D", fontWeight = FontWeight.Bold) } } } }
        item { SessionCard(go) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { Metric("Saldo Kas Laci", "Rp 1.450.000", "Real-time", Modifier.weight(1f)); Metric("Omzet Hari Ini", "Rp 7.420.000", "82% target", Modifier.weight(1f)); Metric("Transaksi", "52", "Sesi aktif", Modifier.weight(1f)) } }
        item { AttentionCard(go) }
        item { Text("Menu Kasir Utama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { Quick(Icons.Default.Inventory2, "Stok") { go(Page.STOK) }; Quick(Icons.Default.ReceiptLong, "Kasbon") { go(Page.KASBON) }; Quick(Icons.Default.People, "Pelanggan") { go(Page.PELANGGAN) }; Quick(Icons.Default.Wallet, "Keluar") { go(Page.PENGELUARAN) } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) { Quick(Icons.Default.Build, "Servis") { go(Page.SERVICE) }; Quick(Icons.Default.Settings, "Setting") { go(Page.PENGATURAN) }; Quick(Icons.Default.Group, "Gaji") { go(Page.GAJI) }; Quick(Icons.Default.Assessment, "Laporan") { go(Page.LAPORAN) } } }
        item { ActivityCard() }
    }
}

@Composable private fun SessionCard(go: (Page) -> Unit) { Card(colors = CardDefaults.cardColors(Container), shape = RoundedCornerShape(20.dp), modifier = Modifier.clickable { go(Page.KASIR) }) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Sesi Kasir Aktif", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Siti Aminah • Shift Pagi • 08:00 WIB", color = Muted, style = MaterialTheme.typography.bodySmall) }; Badge("AKTIF", Success) }; Row { Amount("Saldo awal", "Rp 1.500.000", Modifier.weight(1f)); Amount("Saldo berjalan", "Rp 5.750.000", Modifier.weight(1f)) }; Text("Kelola sesi & rekonsiliasi", color = AppScheme.primary, fontWeight = FontWeight.Bold) } } }
@Composable private fun Amount(a: String,b: String,m: Modifier){Column(m){Text(a,color=Muted,style=MaterialTheme.typography.bodySmall);Text(b,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium)}}
@Composable private fun Badge(t:String,c:Color){Surface(shape=RoundedCornerShape(50),color=High){Text(t,Modifier.padding(horizontal=10.dp,vertical=6.dp),color=c,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)}}
@Composable private fun Metric(t:String,v:String,n:String,m:Modifier){Card(m,colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text(t,color=Muted,style=MaterialTheme.typography.labelSmall);Text(v,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium);Text(n,color=Success,style=MaterialTheme.typography.labelSmall)}}}
@Composable private fun AttentionCard(go:(Page)->Unit){Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Row{Text("Perlu Perhatian Segera",fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("3 tindakan",color=AppScheme.error)}; Attention("3 Servis Siap Diambil", "Kirim notifikasi pelanggan", Icons.Default.Build){go(Page.SERVICE)}; Attention("Kasbon jatuh tempo hari ini", "2 pelanggan • Rp 320.000", Icons.Default.ReceiptLong){go(Page.KASBON)}; Attention("Selisih belum ditoleransi", "Catatan shift malam (-Rp 15.000)", Icons.Default.AccountBalanceWallet){go(Page.KASIR)}}}}
@Composable private fun Attention(t:String,s:String,i:ImageVector,click:()->Unit){Row(Modifier.fillMaxWidth().clickable{click()},verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(40.dp),RoundedCornerShape(12.dp),High){Box(contentAlignment=Alignment.Center){Icon(i,null,tint=AppScheme.primary)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(t,fontWeight=FontWeight.SemiBold);Text(s,color=Muted,style=MaterialTheme.typography.bodySmall)};Icon(Icons.Default.ArrowForward,null,tint=Muted)}}
@Composable private fun Quick(i:ImageVector,t:String,click:()->Unit){Card(Modifier.weight(1f).clickable{click()},colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){Icon(i,null,tint=AppScheme.primary);Text(t,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.SemiBold)}}}
@Composable private fun ActivityCard(){Card(colors=CardDefaults.cardColors(Container),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp)){Row{Text("Aktivitas Terakhir",fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text("Lihat Semua",color=AppScheme.primary)}; listOf("Paket Data Telkomsel 50GB" to "Rp 105.000","Ganti LCD Samsung A12" to "Rp 380.000","Top Up DANA Saldo 100k" to "Rp 102.500","Pulsa Indosat Reguler 25k" to "Rp 26.500").forEach{(a,b)->Row(Modifier.fillMaxWidth().padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.ReceiptLong,null,tint=AppScheme.primary);Spacer(Modifier.width(10.dp));Text(a,Modifier.weight(1f));Text(b,fontWeight=FontWeight.Bold)}}}}}

@Composable private fun StandardPage(p: PaddingValues,title:String,sub:String?,back:()->Unit,content:@Composable()->Unit){LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{PageHeader(title,sub,back)};item{content()}}}

@Composable private fun CashierPage(p:PaddingValues,api:ApiClient){var status by remember{mutableStateOf("Memuat sesi…")};LaunchedEffect(Unit){runCatching{api.get("/api/kasir/current")}.onSuccess{status=it.optString("status","buka")}.onFailure{status="buka"}};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{PageHeader("Kasir & Rekonsiliasi","Sesi Kasir: Aktif",null)};item{Card(colors=CardDefaults.cardColors(Container),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Row{Text("Sesi Kasir",fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Badge(status.uppercase(),Success)};Text("Budi Santoso • Kasir 01",color=Muted);Text("Sesi dibuka 08:00 WIB • Shift Pagi");Text("04:32:18",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Rp 4.820.000",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold,color=AppScheme.primary)}}};item{Text("Master Akun & E-Wallet",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};item{AccountRows()};item{Button(onClick={}){Icon(Icons.Default.Sync,null);Spacer(Modifier.width(8.dp));Text("Sinkronkan Saldo")}};item{Button(onClick={}){Text("Tutup Sesi Kasir Sekarang")}}}}
@Composable private fun AccountRows(){listOf("Kas Tunai di Laci" to "Rp 1.450.000","OrderKuota" to "Rp 2.890.000","DANA Merchant" to "Rp 1.150.000","SeaBank Operasional" to "Rp 3.420.000","ShopeePay / QRIS Statis" to "Rp 780.000").forEach{(a,b)->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.AccountBalanceWallet,null,tint=AppScheme.primary);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(a,fontWeight=FontWeight.SemiBold);Text("Saldo realtime server & laci",color=Muted,style=MaterialTheme.typography.bodySmall)};Text(b,fontWeight=FontWeight.Bold)}}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TransactionSheet(api:ApiClient,close:()->Unit){var mode by remember{mutableStateOf("Transaksi Biasa")};var total by remember{mutableStateOf("187500")};ModalBottomSheet(onDismissRequest=close,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),containerColor=Container){LazyColumn(contentPadding=PaddingValues(20.dp,8.dp,20.dp,30.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Transaksi Baru",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("POS Terminal Online • Kasir Irkop Utama",color=Muted)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Transaksi Biasa","Servis HP").forEach{x->FilterChip(selected=mode==x,onClick={mode=x},label={Text(x)})}}};item{Text("Akses Cepat",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick(Icons.Default.QrCodeScanner,"Token PLN"){};Quick(Icons.Default.ShoppingCart,"Paket Data"){};Quick(Icons.Default.Wallet,"E-Wallet"){};Quick(Icons.Default.Build,"Servis"){} }};item{OutlinedTextField(total,{total=it},label={Text("Total Pembayaran (Rp)")},modifier=Modifier.fillMaxWidth())};item{Text("Metode Pembayaran",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Tunai","Transfer","Bon","Split Bayar").forEach{x->FilterChip(selected=false,onClick={},label={Text(x)})}}};item{OutlinedTextField("","",label={Text("Pelanggan / Nomor WhatsApp")},modifier=Modifier.fillMaxWidth())};item{Button(onClick={scopeCreate(api,total);close()},modifier=Modifier.fillMaxWidth()){Text("Konfirmasi Transaksi")}}}}
private fun scopeCreate(api:ApiClient,total:String){ }

@Composable private fun TransactionPage(p:PaddingValues,api:ApiClient,back:()->Unit){var q by remember{mutableStateOf("")};var items by remember{mutableStateOf(listOf("Paket Data Telkomsel 50GB • Rp 105.000","Ganti LCD Samsung A12 • Rp 380.000","Top Up DANA • Rp 102.500","Pulsa Indosat 25K • Rp 26.500"))};val scope=rememberCoroutineScope();LaunchedEffect(Unit){runCatching{api.get("/api/transaksi",mapOf("date" to "2026-09-05","limit" to "100"))}.onSuccess{root->val a=root.optJSONArray("items")?:JSONArray();items=(0 until a.length()).map{a.optJSONObject(it)?.optString("id").orEmpty()}.filter{it.isNotBlank()}}};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Transaksi","Riwayat transaksi kasir",back)};item{OutlinedTextField(q,{q=it},label={Text("Cari transaksi")},leadingIcon={Icon(Icons.Default.Search,null)},modifier=Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semua","Tunai","Transfer","PPOB").forEach{x->FilterChip(false,{},label={Text(x)})}}};items(items.filter{q.isBlank()||it.contains(q,true)}){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.ReceiptLong,null,tint=AppScheme.primary);Spacer(Modifier.width(10.dp));Text(x,Modifier.weight(1f));IconButton(onClick={}){Icon(Icons.Default.Print,"Cetak")}}}};item{Text("Manual entry tersedia di Laporan untuk transaksi backdate ≤30 hari.",color=Muted,style=MaterialTheme.typography.bodySmall)}}}

@Composable private fun ReportPage(p:PaddingValues,api:ApiClient,search:()->Unit){var loading by remember{mutableStateOf(true)};var total by remember{mutableStateOf("Rp 0")};LaunchedEffect(Unit){runCatching{api.get("/api/laporan/bulan",mapOf("bulan" to "2026-09"))}.onSuccess{total="Rp ${it.optLong("omzet",0)}"}.onFailure{total="Rp 7.420.000"};loading=false};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{PageHeader("Laporan Kasir","Hari Ini",null,search)};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Total Omzet",total,"92,7% target",Modifier.weight(1f));Metric("Transaksi","52","Hari ini",Modifier.weight(1f))}};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){Metric("Laba Bersih Est.","Rp 1,15 Jt","Estimasi",Modifier.weight(1f));Metric("Kasbon","Rp 1,84 Jt","Belum lunas",Modifier.weight(1f))}};item{Card(colors=CardDefaults.cardColors(Container),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Daftar Transaksi Kasir",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);listOf("10:14 • Tunai • Paket Data Telkomsel 50GB • Rp 105.000","09:48 • QRIS • Ganti LCD Samsung A12 • Rp 380.000","09:30 • SeaBank • Top Up DANA • Rp 102.500","09:12 • Tunai • Pulsa Indosat 25K • Rp 26.500").forEach{Text(it,modifier=Modifier.padding(vertical=5.dp))}}}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={}){Icon(Icons.Default.Print,null);Text(" Cetak PDF")};Button(onClick={}){Text("Export CSV")}}};if(loading)item{CircularProgressIndicator(color=AppScheme.primary)}}

@Composable private fun StockPage(p:PaddingValues,api:ApiClient){var showAdd by remember{mutableStateOf(false)};var showCat by remember{mutableStateOf(false)};var q by remember{mutableStateOf("")};val products=remember{mutableStateOf(listOf("Charger Robot Fast Charge 20W • ACC-092 • Stok 18 • Rp 55.000","LCD Samsung A12 Original Incell • SPR-104 • Stok 4 • Rp 280.000","Kartu Perdana Telkomsel 14GB • KRT-011 • Stok 25 • Rp 45.000","Tempered Glass Matte 9D Universal • ACC-033 • Stok 42 • Rp 25.000"))};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Daftar Barang & Stok","142 Produk • 3 SKU kritis",null,{showAdd=true})};item{OutlinedTextField(q,{q=it},label={Text("Cari produk / barcode")},leadingIcon={Icon(Icons.Default.Search,null)},modifier=Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semua 142","Aksesoris 45","Pulsa & Data 38","Sparepart 29").forEach{x->FilterChip(false,{},label={Text(x)})}}};item{OutlinedButton(onClick={showCat=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Category,null);Text(" Kelola Kategori")}};items(products.value.filter{q.isBlank()||it.contains(q,true)}){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row{Text(x.substringBefore(" •"),fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Icon(Icons.Default.Edit,null,tint=Muted);Spacer(Modifier.width(12.dp));Icon(Icons.Default.Delete,null,tint=AppScheme.error)};Text(x.substringAfter(" •"),color=Muted);Text("Margin sehat • siap dijual",color=Success,style=MaterialTheme.typography.labelSmall)}}}};item{Button(onClick={showAdd=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Tambah Produk Baru")}}};if(showAdd)ProductDialog{showAdd=false};if(showCat)CategoryDialog{showCat=false}}

@Composable private fun ProductDialog(close:()->Unit){var name by remember{mutableStateOf("")};var price by remember{mutableStateOf("")};var stock by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Tambah Produk")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField(name,{name=it},label={Text("Nama Produk")});OutlinedTextField(price,{price=it},label={Text("Harga Jual")});OutlinedTextField(stock,{stock=it},label={Text("Stok")});OutlinedTextField("","",label={Text("Barcode / SKU")})}},confirmButton={Button(onClick=close){Text("Simpan Produk")}},dismissButton={TextButton(onClick=close){Text("Batal")}})}
@Composable private fun CategoryPage(p:PaddingValues){LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Kelola Kategori","Kategori, tag warna & urutan",null)};items(listOf("Aksesoris HP • 45 Produk","Sparepart & LCD • 28 Produk","Kartu Perdana & Kuota • 32 Produk","Voucher Fisik & Gesek • 19 Produk","Jasa Servis Hardware • 14 Layanan","Token Listrik & PPOB • 4 Produk")){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Category,null,tint=AppScheme.primary);Spacer(Modifier.width(12.dp));Text(x,Modifier.weight(1f));Icon(Icons.Default.Edit,null)}}};item{Button(onClick={}){Icon(Icons.Default.Add,null);Text(" Tambah Kategori Baru")}}}}
@Composable private fun CategoryDialog(close:()->Unit){var name by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Kelola Kategori Produk")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Atur kategori, warna tag, dan urutan filter katalog.",color=Muted);OutlinedTextField(name,{name=it},label={Text("Nama Kategori")});Text("Ikon:  Smartphone  •  Memory  •  SIM  •  Kabel",color=Muted)}},confirmButton={Button(onClick=close){Text("Simpan")}},dismissButton={TextButton(onClick=close){Text("Batal")}})}

@Composable private fun ServicePage(p:PaddingValues,api:ApiClient){val rows=listOf("SRV-2024-089" to "Xiaomi Redmi Note 11 Pro • Ahmad Zaki • Proses • Rp 380.000","SRV-2024-085" to "iPhone 11 128GB Purple • Maya Indah • Siap Diambil • Rp 450.000","SRV-2024-091" to "Samsung Galaxy A52 • Hendra Gunawan • Baru • Estimasi Rp 650.000");LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Laporan Servis HP","Buku Order Bengkel & Sparepart",null,{})};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Proses","2 unit","Teknisi aktif",Modifier.weight(1f));Metric("Siap Ambil","1 unit","100% diuji",Modifier.weight(1f));Metric("Est. Omzet","Rp 2,85 Jt","18 nota",Modifier.weight(1f))}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semua 18","Diterima","Proses","Selesai","Diambil").forEach{x->FilterChip(false,{},label={Text(x)})}}};items(rows){(id,desc)->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row{Text(id,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Badge(if(desc.contains("Siap"))"SIAP AMBIL" else "PROSES",if(desc.contains("Siap"))Success else AppScheme.primary)};Text(desc);Text("Teknisi: Doni Prasetyo",color=Muted,style=MaterialTheme.typography.bodySmall);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={}){Text("Update Status")};Button(onClick={}){Icon(Icons.Default.Call,null);Text(" WA")}}}}};item{Button(onClick={}){Icon(Icons.Default.Add,null);Text(" Tiket Servis Baru")}}}}

@Composable private fun KasbonPage(p:PaddingValues,api:ApiClient){var show by remember{mutableStateOf(false)};val rows=listOf("#KB-882 • Pak RT Wawan • Sisa Rp 270.000 • Jatuh Tempo Hari Ini","#KB-879 • Mas Dimas • Total Rp 450.000 • 28 Okt 2024","#KB-875 • Bu Sri Warung • Total Rp 180.000 • 30 Okt 2024","#KB-860 • Rudi Knalpot • LUNAS • 23 Okt 2024");LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Buku Kasbon","Manajemen Piutang Counter & Servis",null,{show=true})};item{Metric("Total Belum Lunas","Rp 1.840.000","8 pelanggan • 3 jatuh tempo",Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Semua 12","Belum Lunas","Jatuh Tempo","Lunas").forEach{x->FilterChip(false,{},label={Text(x)})}}};items(rows){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(x,fontWeight=FontWeight.Bold);Text("Pulsa + Token / Sparepart • Riwayat transaksi terhubung",color=Muted,style=MaterialTheme.typography.bodySmall);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={}){Icon(Icons.Default.Wallet,null);Text(" Bayar / Cicil")};OutlinedButton(onClick={}){Icon(Icons.Default.Call,null);Text(" Tagih WA")}}}}};if(show)DebtDialog{show=false}}}
@Composable private fun DebtDialog(close:()->Unit){var nominal by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Kasbon Baru")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField("Pak RT Wawan",{},label={Text("Pelanggan")});OutlinedTextField(nominal,{nominal=it},label={Text("Nominal")});OutlinedTextField("24 Okt 2026",{},label={Text("Jatuh Tempo")})}},confirmButton={Button(onClick=close){Text("Simpan Kasbon")}},dismissButton={TextButton(onClick=close){Text("Batal")}})}

@Composable private fun CustomerPage(p:PaddingValues,api:ApiClient){var show by remember{mutableStateOf(false)};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Manajemen Pelanggan","142 Kontak Tersimpan • Outlet Irkop",null,{show=true})};item{Text("Leaderboard Bulan Ini",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Dimas W. • Rp 4,85 Jt • 38 trx VIP","Haji Wawan • Rp 3,12 Jt • 19 trx","Siti Wulandari • Rp 2,34 Jt • 24 trx").forEach{x->Card(Modifier.weight(1f),colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Column(Modifier.padding(12.dp)){Text(x,fontWeight=FontWeight.SemiBold)}}}}};item{OutlinedTextField("",{},label={Text("Cari nama / nomor / alamat")},leadingIcon={Icon(Icons.Default.Search,null)},modifier=Modifier.fillMaxWidth())};items(listOf("Dimas Wahyudi • Bengkel • 0812-4455-6677 • Rp 4.850.000 • 38 Transaksi","Siti Wulandari • 0857-9900-1122 • Rp 2.340.000 • 24 Transaksi","Haji Wawan • RT 04 • 0813-8877-6655 • Rp 3.120.000 • Jatuh Tempo 3 Hari")){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text(x,fontWeight=FontWeight.Bold);Row{OutlinedButton(onClick={}){Icon(Icons.Default.Call,null);Text(" Hubungi")};Spacer(Modifier.width(8.dp));Button(onClick={}){Text("Detail")}}}}};item{OutlinedButton(onClick={}){Text("Kirim Promo WhatsApp Massal")}}};if(show)CustomerDialog{show=false}}
@Composable private fun CustomerDialog(close:()->Unit){var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Tambah Pelanggan")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField(name,{name=it},label={Text("Nama")});OutlinedTextField(phone,{phone=it},label={Text("WhatsApp")});OutlinedTextField("","",label={Text("Alamat / Catatan")})}},confirmButton={Button(onClick=close){Text("Simpan")}},dismissButton={TextButton(onClick=close){Text("Batal")}})}

@Composable private fun ExpensePage(p:PaddingValues,api:ApiClient){var show by remember{mutableStateOf(false)};LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Catatan Pengeluaran","Arus Kas Keluar Irkop Cell",null,{show=true})};item{Metric("Total Keluar","Rp 4.250.000","Bulan ini",Modifier.fillMaxWidth())};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("Rata-rata","Rp 177.000","per hari",Modifier.weight(1f));Metric("Stok & Kulakan","60%","distribusi",Modifier.weight(1f));Metric("Operasional","25%","distribusi",Modifier.weight(1f))}};items(listOf("Kulakan Tempered Glass & Kabel Data Robot • Belanja Stok • Kas Laci • - Rp 250.000","Makan Siang & Kopi Kasir (2 Orang) • Operasional • Kas Laci • - Rp 50.000","Beli Lakban, Nota Kontinyu & Kertas Thermal • Perlengkapan • Kas Laci • - Rp 80.000","Bayar Tagihan Listrik PLN Toko • Utilitas • SeaBank • - Rp 450.000","Iuran Wifi Counter • Utilitas • SeaBank • - Rp 200.000")){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(15.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Wallet,null,tint=AppScheme.error);Spacer(Modifier.width(10.dp));Text(x,Modifier.weight(1f));Icon(Icons.Default.Edit,null);Spacer(Modifier.width(10.dp));Icon(Icons.Default.Delete,null,tint=AppScheme.error)}}};item{Button(onClick={show=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" Catat Pengeluaran Baru")}}};if(show)ExpenseDialog{show=false}}
@Composable private fun ExpenseDialog(close:()->Unit){var desc by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Catat Pengeluaran Baru")},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField(desc,{desc=it},label={Text("Nama / Deskripsi Pengeluaran")});OutlinedTextField(amount,{amount=it},label={Text("Nominal")});OutlinedTextField("Operasional",{},label={Text("Kategori")});OutlinedTextField("Tunai • Kas Laci",{},label={Text("Metode & Akun Sumber")})}},confirmButton={Button(onClick=close){Text("Simpan Pengeluaran")}},dismissButton={TextButton(onClick=close){Text("Batal")}})}

@Composable private fun PayrollPage(p:PaddingValues,api:ApiClient){LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Gaji Karyawan & Shift","Khusus Pemilik Toko",null)};item{Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Akses Dibatasi • Root Admin",color=AppScheme.error,fontWeight=FontWeight.Bold);Text("Total Liabilitas Payroll",color=Muted);Text("Rp 5.800.000",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Total Staff: 3 Orang")}}};items(listOf("Siti Aminah • Kasir Shift 1 • 22 Hari • Gaji Pokok Rp 1.800.000 • Uang Makan Rp 330.000 • Total Rp 2.480.000","Doni Prasetyo • Teknisi Hardware • Komisi 40% • Omzet Jasa Rp 4.200.000 • Komisi Rp 1.680.000","Budi Santoso • Kasir Shift 2 • 18 Hari • Prorata Rp 1.472.000 • Uang Makan Rp 180.000 • Bersih Rp 1.640.000")){x->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text(x,fontWeight=FontWeight.SemiBold);Text("Absensi, rincian servis, slip gaji, dan log buka sesi",color=Muted,style=MaterialTheme.typography.bodySmall);OutlinedButton(onClick={}){Text("Lihat Rincian")}}}};item{Button(onClick={}){Text("Atur Skema & Rate")}}}}

@Composable private fun MorePage(p:PaddingValues,go:(Page)->Unit){LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Lainnya","Sesi Kasir: Aktif",null)};item{Card(colors=CardDefaults.cardColors(Container),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Irkop Cell - Counter Pusat",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("ID Outlet: IRP-SBY-001 • v2.4.8-prod",color=Muted)}}};items(listOf("Daftar Barang & Stok" to Icons.Default.Inventory2 to Page.STOK,"Laporan Servis HP" to Icons.Default.Build to Page.SERVICE,"Buku Kasbon" to Icons.Default.ReceiptLong to Page.KASBON,"Data Pelanggan" to Icons.Default.People to Page.PELANGGAN,"Catatan Pengeluaran" to Icons.Default.Wallet to Page.PENGELUARAN,"Gaji Karyawan" to Icons.Default.Group to Page.GAJI,"Pengaturan" to Icons.Default.Settings to Page.PENGATURAN)){(t,i,page)->Card(Modifier.clickable{go(page)},colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(16.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,null,tint=AppScheme.primary);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(t,fontWeight=FontWeight.SemiBold);Text("Buka modul lengkap",color=Muted,style=MaterialTheme.typography.bodySmall)};Icon(Icons.Default.ChevronRight,null,tint=Muted)}}};item{Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Akselerasi Transaksi",fontWeight=FontWeight.Bold);Text("Cetak nota & barcode instan • Pemindai kamera untuk IMEI / SN",color=Muted);Text("Mode Gelap OLED • NotifHook • Manajemen User & Hak Akses",color=Muted)}}}}

@Composable private fun SettingsPage(p:PaddingValues,api:ApiClient){LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp,8.dp,16.dp,110.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{PageHeader("Pengaturan","Operasional, hak akses & sistem",null)};items(listOf("Tampilan & Mode Gelap" to "OLED Efisien • Mode Gelap","NotifHook" to "Terkoneksi • HTTP 200 OK","Manajemen User & Hak Akses" to "Admin / Owner • Kasir • Teknisi","Informasi Outlet & Printer" to "VSC MP-58C • Bluetooth Thermal 58mm","Master Akun Uang" to "Kas, DANA, SeaBank, QRIS","Console Log / System Log" to "Live audit transaksi, gateway & printer")){(a,b)->Card(colors=CardDefaults.cardColors(Low),shape=RoundedCornerShape(18.dp)){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Settings,null,tint=AppScheme.primary);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(a,fontWeight=FontWeight.SemiBold);Text(b,color=Muted,style=MaterialTheme.typography.bodySmall)};Icon(Icons.Default.ChevronRight,null)}}};item{Text("Hak akses mengikuti permission dari JWT/backend; modul Gaji dan Pengaturan dibatasi admin.",color=Muted,style=MaterialTheme.typography.bodySmall)}}

@Composable private fun SearchDialog(close:()->Unit){var q by remember{mutableStateOf("")};AlertDialog(onDismissRequest=close,title={Text("Cari di Irkop Cell")},text={OutlinedTextField(q,{q=it},label={Text("Produk, transaksi, pelanggan, kasbon…")},modifier=Modifier.fillMaxWidth())},confirmButton={Button(onClick=close){Text("Cari")}})}
