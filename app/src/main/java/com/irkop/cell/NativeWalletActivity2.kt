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

private val Rp2 = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun JsonObject.t2(vararg k: String) = k.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.v2(vararg k: String) = k.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L
private fun JsonObject.i2() = this["items"]?.jsonArray?.filterIsInstance<JsonObject>().orEmpty()
private fun ini2(s: String) = s.trim().split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }.ifBlank { "IC" }

class NativeWalletActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(applicationContext); val repo = Repository(ApiClient(session).api)
        setContent {
            val vm: AppViewModel = viewModel(factory = object : ViewModelProvider.Factory { override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = AppViewModel(session, repo) as T })
            IrkopTheme { WalletRoot2(vm, repo) }
        }
    }
}

@Composable private fun WalletRoot2(vm: AppViewModel, repo: Repository) { val state by vm.state.collectAsState(); when { state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }; state.user == null -> Login2(state.error, vm::login, vm::clearError); else -> Shell2(state.user!!, vm::logout, repo) } }

@Composable private fun Login2(error: String?, login: (String,String)->Unit, clear: ()->Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize()) { Column(Modifier.fillMaxWidth().padding(24.dp).align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(76.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(40.dp)) }
        Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("POS & Buku Kas Digital", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(value=u,onValueChange={u=it},modifier=Modifier.fillMaxWidth(),label={Text("Username")},singleLine=true)
        OutlinedTextField(value=p,onValueChange={p=it},modifier=Modifier.fillMaxWidth(),label={Text("Password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
        Button(onClick={login(u.trim(),p)},enabled=u.isNotBlank()&&p.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)){Text("Masuk")}
        error?.let { Text(it,color=MaterialTheme.colorScheme.error); LaunchedEffect(it){clear()} }
    } }
}

private enum class Tab2(val label:String,val icon:ImageVector){HOME("Home",Icons.Default.Home),TX("Transaksi",Icons.Default.ReceiptLong),KASIR("Kasir",Icons.Default.PointOfSale),REPORT("Laporan",Icons.Default.Assessment),OTHER("Lainnya",Icons.Default.MoreHoriz)}

@Composable private fun Shell2(user:UserSession,logout:()->Unit,repo:Repository){
    var tab by remember{mutableStateOf(Tab2.HOME)};var other by remember{mutableStateOf<String?>(null)}
    Scaffold(topBar={TopAppBar(title={Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),Alignment.Center){Text(ini2(user.nama.ifBlank{user.username}),fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onPrimaryContainer)};Spacer(Modifier.width(10.dp));Column{Text("IRKOP CELL",fontWeight=FontWeight.Bold);Text(user.nama.ifBlank{user.username},style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}},actions={IconButton(onClick={}){Icon(Icons.Default.NotificationsNone,"Notifikasi")};IconButton(onClick=logout){Icon(Icons.Default.Logout,"Keluar")}})},bottomBar={NavigationBar{Tab2.values().forEach{t->NavigationBarItem(selected=tab==t,onClick={tab=t;if(t!=Tab2.OTHER)other=null},icon={Icon(t.icon,null)},label={Text(t.label)})}}}){pad->Box(Modifier.fillMaxSize().padding(pad)){when(tab){Tab2.HOME->Home2(repo,{tab=Tab2.TX},{tab=Tab2.KASIR},{tab=Tab2.REPORT}){key->tab=Tab2.OTHER;other=key};Tab2.TX->Tx2(repo);Tab2.KASIR->Cashier2(repo);Tab2.REPORT->LaporanAnalyticsScreen(repo);Tab2.OTHER->Other2(user,repo,other){other=it}}}}
}

@Composable private fun Home2(repo:Repository,tx:()->Unit,cashier:()->Unit,report:()->Unit,other:(String)->Unit){
    var cash by remember{mutableStateOf<JsonObject?>(null)};var txd by remember{mutableStateOf<JsonObject?>(null)};var error by remember{mutableStateOf<String?>(null)};var loading by remember{mutableStateOf(true)};val scope=rememberCoroutineScope()
    fun load(){scope.launch{loading=true;runCatching{cash=repo.kasirCurrent();txd=repo.transaksi(tanggal=LocalDate.now().toString())}.onFailure{error=ApiError.message(it)};loading=false}}
    LaunchedEffect(Unit){load()};val rows=txd?.i2().orEmpty();val balance=cash?.i2()?.sumOf{it.v2("saldo_sistem","saldo","total")}?:cash?.v2("saldo_sistem","saldo")?:0L;val omzet=txd?.v2("total_nilai","total_omzet","omzet")?:0L
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("Selamat datang 👋",color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Kelola toko lebih cepat hari ini",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)}
        item{Card(Modifier.fillMaxWidth().clickable{tx()},shape=RoundedCornerShape(26.dp)){Box(Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary,MaterialTheme.colorScheme.primaryContainer))).padding(22.dp)){Column{Text("Saldo kasir",color=MaterialTheme.colorScheme.onPrimary);Text(Rp2.format(balance),color=MaterialTheme.colorScheme.onPrimary,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(8.dp));Text("${rows.size} transaksi hari ini",color=MaterialTheme.colorScheme.onPrimary.copy(alpha=.85f));Text("Omzet ${Rp2.format(omzet)}",color=MaterialTheme.colorScheme.onPrimary.copy(alpha=.85f),style=MaterialTheme.typography.labelMedium)}}}}
        item{Text("Akses cepat",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)}
        item{Quick2(tx,cashier,report,other)}
        item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("Aktivitas terbaru",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));TextButton(onClick=tx){Text("Lihat semua")}}}
        when{loading->item{Box(Modifier.fillMaxWidth().padding(24.dp),Alignment.Center){CircularProgressIndicator()}};error!=null->item{Error2(error!!,::load)};rows.isEmpty()->item{Empty2()};else->items(rows.take(8)){r->Activity2(r)}}
    }
}

@Composable private fun Quick2(tx:()->Unit,cashier:()->Unit,report:()->Unit,other:(String)->Unit){val a=listOf(Triple("Transaksi",Icons.Default.ReceiptLong){tx()},Triple("Kasir",Icons.Default.PointOfSale){cashier()},Triple("Kasbon",Icons.Default.AccountBalanceWallet){other("kasbon")},Triple("Laporan",Icons.Default.Assessment){report()},Triple("Pelanggan",Icons.Default.People){other("pelanggan")},Triple("Produk",Icons.Default.Inventory2){other("ops")},Triple("Service",Icons.Default.Build){other("service")},Triple("Akun Uang",Icons.Default.AccountBalance){other("akun")});Column(verticalArrangement=Arrangement.spacedBy(10.dp)){a.chunked(4).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{(label,icon,click)->Card(Modifier.weight(1f).clickable{click()},shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally){FilledTonalIconButton(onClick=click,modifier=Modifier.size(46.dp)){Icon(icon,label)};Spacer(Modifier.height(5.dp));Text(label,style=MaterialTheme.typography.labelSmall,maxLines=1)}}}}}}}
@Composable private fun Activity2(x:JsonObject){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),Alignment.Center){Text(ini2(x.t2("pelanggan_nama","pelanggan","nama")),fontWeight=FontWeight.Bold)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(x.t2("pelanggan_nama","pelanggan","nama"),fontWeight=FontWeight.SemiBold);Text(x.t2("created_at","tanggal","waktu"),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(Rp2.format(x.v2("total","nominal","grand_total")),fontWeight=FontWeight.Bold)}}}
@Composable private fun Empty2(){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Column(Modifier.fillMaxWidth().padding(26.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Inbox,null,tint=MaterialTheme.colorScheme.primary);Text("Belum ada transaksi",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);Text("Transaksi baru akan muncul di sini",color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
@Composable private fun Error2(s:String,retry:()->Unit){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.errorContainer)){Column(Modifier.padding(14.dp)){Text(s,color=MaterialTheme.colorScheme.onErrorContainer);TextButton(onClick=retry){Text("Coba lagi")}}}}

@Composable private fun Tx2(repo:Repository){var q by remember{mutableStateOf("")};var method by remember{mutableStateOf("")};var status by remember{mutableStateOf("")};var rows by remember{mutableStateOf(emptyList<JsonObject>())};var error by remember{mutableStateOf<String?>(null)};var add by remember{mutableStateOf(false)};var detail by remember{mutableStateOf<JsonObject?>(null)};val scope=rememberCoroutineScope();fun load(){scope.launch{runCatching{repo.transaksi(q=q.trim().takeIf{it.isNotBlank()},metodeBayar=method.takeIf{it.isNotBlank()},statusKonfirmasi=status.takeIf{it.isNotBlank()})}.onSuccess{rows=it.i2();error=null}.onFailure{error=ApiError.message(it)}}};LaunchedEffect(Unit){load()};LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=90.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("Transaksi",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));IconButton(onClick=::load){Icon(Icons.Default.Refresh,"Refresh")}}};item{OutlinedTextField(value=q,onValueChange={q=it},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(16.dp),placeholder={Text("Cari transaksi…")},leadingIcon={Icon(Icons.Default.Search,null)})};item{Filter2(listOf("" to "Semua","tunai" to "Tunai","transfer" to "Transfer","bon" to "Kasbon","cash_tunai" to "Cash"),method){method=it}};item{Filter2(listOf("" to "Semua status","menunggu" to "Menunggu","otomatis" to "Otomatis","manual" to "Manual","tidak_perlu" to "Tidak perlu"),status){status=it}};item{Button(onClick=::load,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("Terapkan filter")}};if(rows.isEmpty())item{Empty2()}else items(rows){r->Card(onClick={detail=r},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(r.t2("nomor","kode","id"),fontWeight=FontWeight.Bold);Text(Rp2.format(r.v2("total","nominal","grand_total")),fontWeight=FontWeight.Bold)};Text(r.t2("pelanggan_nama","pelanggan","nama"));Text(r.t2("metode_bayar","metode"),style=MaterialTheme.typography.labelMedium)}}};error?.let{item{Error2(it,::load)}}};FloatingActionButton(onClick={add=true},modifier=Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(18.dp)){Icon(Icons.Default.Add,"Transaksi baru")};if(add)ModernCheckoutDialog(repo,{add=false;load()},{add=false});detail?.let{TransactionDetail2(repo,it,{detail=null;load()},{detail=null})}}

@Composable private fun Filter2(opts:List<Pair<String,String>>,selected:String,onPick:(String)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){opts.forEach{(v,l)->FilterChip(selected=selected==v,onClick={onPick(v)},label={Text(l)})}}}
@Composable private fun TransactionDetail2(repo:Repository,row:JsonObject,done:()->Unit,cancel:()->Unit){val scope=rememberCoroutineScope();var del by remember{mutableStateOf(false)};var reason by remember{mutableStateOf("")};var status by remember{mutableStateOf(row.t2("konfirmasi_pembayaran","status_konfirmasi").takeUnless{it=="-"}?:"menunggu")};var msg by remember{mutableStateOf<String?>(null)};AlertDialog(onDismissRequest=cancel,title={Text("Detail transaksi")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){Text("ID: ${row.t2("id","nomor","kode")}");Text("Pelanggan: ${row.t2("pelanggan_nama","pelanggan")}");Text("Total: ${Rp2.format(row.v2("total","nominal","grand_total"))}",fontWeight=FontWeight.Bold);Filter2(listOf("menunggu" to "Menunggu","otomatis" to "Otomatis","manual" to "Manual","tidak_perlu" to "Tidak perlu"),status){status=it};Button(onClick={scope.launch{runCatching{repo.updateTransaksiKonfirmasi(row.t2("id"),status)}.onSuccess{msg="Konfirmasi tersimpan"}.onFailure{msg=ApiError.message(it)}}}){Text("Simpan konfirmasi")};msg?.let{Text(it)};if(del)OutlinedTextField(value=reason,onValueChange={reason=it},modifier=Modifier.fillMaxWidth(),label={Text("Alasan hapus")})}},confirmButton={if(!del)Row{TextButton(onClick={del=true}){Text("Hapus")};TextButton(onClick=cancel){Text("Tutup")}}else Button(onClick={scope.launch{runCatching{repo.deleteTransaksi(row.t2("id"),reason.takeIf{it.isNotBlank()})}.onSuccess{done()}.onFailure{msg=ApiError.message(it)}}}){Text("Konfirmasi hapus")}},dismissButton={if(del)TextButton(onClick={cancel}){Text("Batal")}})}

@Composable private fun Cashier2(repo:Repository){var data by remember{mutableStateOf<JsonObject?>(null)};var open by remember{mutableStateOf(false)};var close by remember{mutableStateOf(false)};var checkout by remember{mutableStateOf(false)};var amount by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope();fun load(){scope.launch{runCatching{data=repo.kasirCurrent()}.onFailure{error=ApiError.message(it)}}};LaunchedEffect(Unit){load()};val state=data?.t2("status")?:"-";LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Kasir",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Opening, transaksi, dan closing",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(20.dp)){Text("Status sesi",color=MaterialTheme.colorScheme.onSurfaceVariant);Text(if(state=="buka")"Kasir sedang buka" else "Belum buka",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("Tanggal ${data?.t2("tanggal")?:"-"}")}}};error?.let{item{Error2(it,::load)}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Button(onClick={amount="";open=true},enabled=state!="buka",modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)){Text("Opening")};OutlinedButton(onClick={amount="";note="";close=true},enabled=state=="buka",modifier=Modifier.weight(1f),shape=RoundedCornerShape(14.dp)){Text("Closing")}}};item{Button(onClick={checkout=true},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.PointOfSale,null);Spacer(Modifier.width(8.dp));Text("Transaksi penjualan")}};item{Text("Saldo per akun",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(data?.i2().orEmpty()){a->Card(Modifier.fillMaxWidth()){Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a.t2("nama_akun","nama"));Text(Rp2.format(a.v2("saldo_sistem","saldo")),fontWeight=FontWeight.Bold)}}}};if(open)Amount2("Opening Kasir",amount,{amount=it},{val v=amount.toLongOrNull();if(v!=null&&v>=0)scope.launch{runCatching{repo.opening(listOf("Tunai Laci" to v))}.onSuccess{open=false;load()}.onFailure{error=ApiError.message(it)}}},{open=false});if(close)AlertDialog(onDismissRequest={close=false},title={Text("Closing Kasir")},text={Column{OutlinedTextField(value=amount,onValueChange={amount=it.filter(Char::isDigit)},label={Text("Saldo real tunai")});OutlinedTextField(value=note,onValueChange={note=it},label={Text("Catatan")})}},confirmButton={Button(onClick={val v=amount.toLongOrNull();if(v!=null&&v>=0)scope.launch{runCatching{repo.closing(listOf("Tunai Laci" to v),note)}.onSuccess{close=false;load()}.onFailure{error=ApiError.message(it)}}}){Text("Simpan")}},dismissButton={TextButton(onClick={close=false}){Text("Batal")}});if(checkout)ModernCheckoutDialog(repo,{checkout=false;load()},{checkout=false})}
@Composable private fun Amount2(title:String,value:String,onValue:(String)->Unit,ok:()->Unit,cancel:()->Unit){AlertDialog(onDismissRequest=cancel,title={Text(title)},text={OutlinedTextField(value=value,onValueChange=onValue,label={Text("Nominal")},singleLine=true)},confirmButton={Button(onClick=ok){Text("Simpan")}},dismissButton={TextButton(onClick=cancel){Text("Batal")}})}

@Composable private fun Other2(user:UserSession,repo:Repository,selected:String?,onSelect:(String?)->Unit){if(!selected.isNullOrBlank()){Column(Modifier.fillMaxSize()){Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={onSelect(null)}){Icon(Icons.Default.ArrowBack,"Kembali")};Column{Text("Menu Lainnya",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(otherName2(selected),style=MaterialTheme.typography.labelMedium)}};HorizontalDivider();Box(Modifier.fillMaxSize().padding(8.dp)){when(selected){"pelanggan"->PelangganScreen(repo);"kasbon"->KasbonDetailScreen(repo);"service"->ServiceHpScreen(repo);"akun"->AkunUangScreen(repo);"ops"->OperationsScreen(user,repo);"koreksi"->CorrectionsScreen(repo)}}}}else LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Lainnya",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Fitur operasional dan administrasi",color=MaterialTheme.colorScheme.onSurfaceVariant)};item{OtherGrid2(onSelect,listOf(Triple("pelanggan","Pelanggan",Icons.Default.People),Triple("kasbon","Kasbon",Icons.Default.AccountBalanceWallet),Triple("service","Service HP",Icons.Default.Build),Triple("akun","Akun Uang",Icons.Default.AccountBalance)))};if(user.role.equals("admin",true)){item{OtherGrid2(onSelect,listOf(Triple("ops","Produk & Operasional",Icons.Default.Inventory2),Triple("koreksi","Koreksi",Icons.Default.EditNote)))}}}}
@Composable private fun OtherGrid2(onSelect:(String?)->Unit,entries:List<Triple<String,String,ImageVector>>){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){entries.forEach{(key,label,icon)->Card(Modifier.weight(1f).clickable{onSelect(key)},shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.height(8.dp));Text(label,fontWeight=FontWeight.SemiBold)}}}}}
private fun otherName2(k:String)=when(k){"pelanggan"->"Pelanggan";"kasbon"->"Kasbon";"service"->"Service HP";"akun"->"Akun Uang";"ops"->"Produk & Operasional";"koreksi"->"Koreksi";else->"Fitur"}
