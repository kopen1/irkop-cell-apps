package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

private val Bg=Color(0xFF131313)
private val Card=Color(0xFF202020)
private val CardHigh=Color(0xFF2A2A2A)
private val TextMain=Color(0xFFE5E2E1)
private val TextMuted=Color(0xFFBCC9C5)
private val Primary=Color(0xFF70D8C8)
private val Success=Color(0xFF4EDEA3)
private val Error=Color(0xFFFFB4AB)
private val OnPrimary=Color(0xFF003731)

private val Scheme=darkColorScheme(primary=Primary,onPrimary=OnPrimary,secondary=Success,background=Bg,onBackground=TextMain,surface=Bg,onSurface=TextMain,surfaceVariant=CardHigh,onSurfaceVariant=TextMuted,error=Error)

private enum class Page { HOME,KASIR,TRANSAKSI,LAPORAN,STOK,SERVICE,KASBON,PELANGGAN,PENGELUARAN,GAJI,PENGATURAN,MORE }

private fun JSONObject.str(vararg names:String):String { for(n in names) if(has(n)&&!isNull(n)) return optString(n); return "" }
private fun JSONObject.num(vararg names:String):Long { for(n in names) if(has(n)&&!isNull(n)) return optLong(n); return 0L }
private fun JSONArray.toObjects():List<JSONObject> { val r=mutableListOf<JSONObject>(); for(i in 0 until length()) optJSONObject(i)?.let{r.add(it)}; return r }
private fun rupiah(v:Long)=NumberFormat.getCurrencyInstance(Locale("id","ID")).format(v).replace(",00","")

class NativeActivity:ComponentActivity(){
    override fun onCreate(state:Bundle?){super.onCreate(state);setContent{MaterialTheme(colorScheme=Scheme){App()}}}
}

@Composable private fun App(){
    val api=remember{ApiClient(LocalContext.current)}
    var logged by remember{mutableStateOf(api.hasToken())}
    if(logged) MainShell(api){api.clearToken();logged=false} else Login(api){logged=true}
}

@Composable private fun Login(api:ApiClient,onDone:()->Unit){
    var user by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};var error by remember{mutableStateOf("")};val scope=rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(Bg).padding(20.dp),Alignment.Center){
        Card(Modifier.fillMaxWidth().widthIn(max=440.dp),colors=CardDefaults.cardColors(Card)){
            Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                Text("Irkop Cell",fontSize=32.sp,fontWeight=FontWeight.ExtraBold);Text("Solusi Kasir, PPOB & Servis Konter Modern",color=TextMuted);AssistChip(onClick={},label={Text("FAST SYNC • CLOUD POS")})
                OutlinedTextField(user,{user=it},Modifier.fillMaxWidth(),label={Text("ID Kasir / Username")},singleLine=true)
                OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("Kata Sandi / PIN Sesi")},singleLine=true,visualTransformation=PasswordVisualTransformation())
                if(error.isNotBlank())Text(error,color=Error)
                Button(onClick={scope.launch{busy=true;error="";try{api.login(user,pass);onDone()}catch(e:Exception){error=e.message?:"Login gagal"}finally{busy=false}}},enabled=!busy&&user.isNotBlank()&&pass.isNotBlank(),Modifier.fillMaxWidth().height(52.dp)){if(busy)CircularProgressIndicator(Modifier.size(20.dp))else Text("Masuk Sesi Kasir",fontWeight=FontWeight.Bold)}
            }
        }
    }
}

@Composable private fun MainShell(api:ApiClient,logout:()->Unit){
    var page by remember{mutableStateOf(Page.HOME)};var transaction by remember{mutableStateOf(false)}
    Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Bg){
        listOf(Triple(Page.HOME,"Beranda",Icons.Default.Home),Triple(Page.TRANSAKSI,"Kasir",Icons.Default.PointOfSale),Triple(Page.LAPORAN,"Laporan",Icons.Default.Assessment),Triple(Page.MORE,"Lainnya",Icons.Default.MoreHoriz)).forEach{item->NavigationBarItem(page==item.first,{page=item.first},{Icon(item.third,null)},{Text(item.second)})}
    }},floatingActionButton={if(page==Page.HOME||page==Page.TRANSAKSI)ExtendedFloatingActionButton(onClick={transaction=true},icon={Icon(Icons.Default.Add,null)},text={Text("Transaksi Baru")},containerColor=Primary,contentColor=OnPrimary)}){pad->
        Box(Modifier.fillMaxSize().padding(pad)){when(page){
            Page.HOME->Home(api){page=it};Page.KASIR->Cashier(api);Page.TRANSAKSI->DataPage(api,"Transaksi","/api/transaksi"){o->"${o.str("id")} • ${o.str("pelanggan_nama").ifBlank{"Umum"}} • ${rupiah(o.num("total"))}"};Page.LAPORAN->Report(api);Page.STOK->DataPage(api,"Daftar Barang & Stok","/api/produk"){o->"${o.str("nama","nama_produk")} • stok ${o.num("stok")} • ${rupiah(o.num("harga_jual","harga"))}"};Page.SERVICE->DataPage(api,"Service HP","/api/service-hp"){o->"${o.str("nama_pelanggan","pelanggan_nama")} • ${o.str("status")} • ${o.str("keluhan","deskripsi")}"};Page.KASBON->DataPage(api,"Kasbon","/api/kasbon"){o->"${o.str("pelanggan_nama","nama_pelanggan")} • ${rupiah(o.num("sisa","nominal","jumlah"))}"};Page.PELANGGAN->DataPage(api,"Pelanggan","/api/pelanggan"){o->"${o.str("nama")} • ${o.str("no_hp","telepon","nomor")}"};Page.PENGELUARAN->DataPage(api,"Pengeluaran","/api/pengeluaran"){o->"${o.str("deskripsi")} • ${rupiah(o.num("nominal"))}"};Page.GAJI->DataPage(api,"Gaji Karyawan","/api/gaji"){o->"${o.str("nama_karyawan","nama")} • ${rupiah(o.num("nominal","jumlah"))}"};Page.PENGATURAN->Settings(api);Page.MORE->More({page=it},logout)
        }}
        if(transaction)TransactionForm(api){transaction=false}
    }}
}

@Composable private fun Header(title:String,subtitle:String?=null){Column(Modifier.padding(16.dp)){Text(title,fontSize=22.sp,fontWeight=FontWeight.Bold);subtitle?.let{Text(it,color=TextMuted,fontSize=12.sp)}}}

@Composable private fun Home(api:ApiClient,go:(Page)->Unit){
    var d by remember{mutableStateOf<JSONObject?>(null)};LaunchedEffect(Unit){d=runCatching{api.get("/api/kasir/current")}.getOrNull()}
    LazyColumn(contentPadding=PaddingValues(bottom=96.dp)){item{Header("Irkop Cell","SESI KASIR: ${(d?.str("status")?:"MEMUAT")).uppercase()")};item{Column(Modifier.padding(16.dp)){Text("Selamat datang 👋",color=TextMuted);Text("Kelola toko lebih cepat hari ini",fontSize=26.sp,fontWeight=FontWeight.ExtraBold);Spacer(Modifier.height(16.dp));Card(colors=CardDefaults.cardColors(Card)){Column(Modifier.padding(20.dp)){Text("Saldo Sistem",color=TextMuted);val total=d?.optJSONArray("saldo")?.toObjects()?.sumOf{it.num("saldo_sistem")}?:0L;Text(rupiah(total),fontSize=30.sp,fontWeight=FontWeight.ExtraBold)}};Spacer(Modifier.height(22.dp));Text("Menu Kasir Utama",fontSize=18.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp))}};item{Quick(go)}}
}

@Composable private fun Quick(go:(Page)->Unit){val list=listOf("Kasir" to Page.KASIR,"Transaksi" to Page.TRANSAKSI,"Laporan" to Page.LAPORAN,"Produk" to Page.STOK,"Service" to Page.SERVICE,"Kasbon" to Page.KASBON,"Pelanggan" to Page.PELANGGAN,"Pengeluaran" to Page.PENGELUARAN);Column(Modifier.padding(horizontal=16.dp)){list.chunked(4).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{a->Card(Modifier.weight(1f),colors=CardDefaults.cardColors(CardHigh)){Column(Modifier.fillMaxWidth().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Apps,null,tint=Primary);Text(a.first,fontSize=10.sp,fontWeight=FontWeight.Bold)}}}};Spacer(Modifier.height(8.dp))}}}

@Composable private fun Cashier(api:ApiClient){var d by remember{mutableStateOf<JSONObject?>(null)};var open by remember{mutableStateOf(false)};var close by remember{mutableStateOf(false)};var err by remember{mutableStateOf("")};val scope=rememberCoroutineScope();fun load(){scope.launch{try{d=api.get("/api/kasir/current");err=""}catch(e:Exception){err=e.message?:"Gagal"}}};LaunchedEffect(Unit){load()};val status=d?.str("status")?:"memuat";Column{Header("Kasir & Rekonsiliasi","Sesi harian");if(err.isNotBlank())Text(err,color=Error,Modifier.padding(16.dp));Card(Modifier.padding(16.dp).fillMaxWidth(),colors=CardDefaults.cardColors(Card)){Column(Modifier.padding(18.dp)){Text("Status",color=TextMuted);Text(status.uppercase(),fontSize=24.sp,fontWeight=FontWeight.ExtraBold);d?.optJSONArray("saldo")?.toObjects()?.forEach{Text("${it.str("nama_akun")}: ${rupiah(it.num("saldo_sistem"))}")}}};Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({open=true},enabled=status=="belum_buka"){Text("Buka Kasir")};OutlinedButton({close=true},enabled=status=="buka"){Text("Closing")}}};if(open)CashDialog(api,true){open=false;load()};if(close)CashDialog(api,false){close=false;load()}}

@Composable private fun CashDialog(api:ApiClient,isOpen:Boolean,done:()->Unit){var amount by remember{mutableStateOf("")};var err by remember{mutableStateOf("")};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=done,title={Text(if(isOpen)"Opening Kasir" else "Closing Kasir")},text={Column{OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},label={Text("Nominal Tunai Laci")},singleLine=true);if(err.isNotBlank())Text(err,color=Error)}},confirmButton={Button({scope.launch{try{if(isOpen)api.post("/api/kasir/opening",JSONObject().put("saldo_awal",JSONArray().put(JSONObject().put("nama_akun","Tunai Laci").put("saldo",amount.toLongOrNull()?:0L))),true)else api.post("/api/kasir/closing",JSONObject().put("saldo_real",JSONArray().put(JSONObject().put("nama_akun","Tunai Laci").put("saldo_real",amount.toLongOrNull()?:0L))),true);done()}catch(e:Exception){err=e.message?:"Gagal"}}}){Text("Simpan")}},dismissButton={TextButton(done){Text("Batal")}})}

@Composable private fun DataPage(api:ApiClient,title:String,path:String,format:(JSONObject)->String){var rows by remember{mutableStateOf(emptyList<JSONObject>())};var q by remember{mutableStateOf("")};var loading by remember{mutableStateOf(true)};var err by remember{mutableStateOf("")};val scope=rememberCoroutineScope();fun load(){scope.launch{loading=true;try{val d=api.get(path,if(q.isBlank())emptyMap()else mapOf("q" to q));rows=(d.optJSONArray("items")?:d.optJSONArray("data")?:d.optJSONArray("results"))?.toObjects().orEmpty();err=""}catch(e:Exception){err=e.message?:"Gagal"}finally{loading=false}}};LaunchedEffect(Unit){load()};Column{Header(title,"Data live dari backend");Row(Modifier.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){OutlinedTextField(q,{q=it},Modifier.weight(1f),label={Text("Cari")},singleLine=true);IconButton({load()}){Icon(Icons.Default.Search,null)}};if(err.isNotBlank())Text(err,color=Error,Modifier.padding(16.dp));if(loading)Box(Modifier.fillMaxSize(),Alignment.Center){CircularProgressIndicator()}else LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){if(rows.isEmpty())item{Text("Belum ada data",color=TextMuted,Modifier.padding(30.dp))};items(rows){o->Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(CardHigh)){Column(Modifier.padding(16.dp)){Text(format(o),fontWeight=FontWeight.SemiBold);Text(o.str("id","created_at","tanggal"),color=TextMuted,fontSize=11.sp)}}}}}}

@Composable private fun Report(api:ApiClient){var d by remember{mutableStateOf<JSONObject?>(null)};LaunchedEffect(Unit){d=runCatching{api.get("/api/laporan/bulan",mapOf("bulan" to java.time.LocalDate.now().toString().substring(0,7)))}.getOrNull()};Column{Header("Laporan","Ringkasan dari backend");LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Stat("Omzet",rupiah(d?.num("omzet","total_omzet")?:0L))};item{Stat("Laba",rupiah(d?.num("laba","total_laba")?:0L))};item{Stat("Pengeluaran",rupiah(d?.num("pengeluaran","total_pengeluaran")?:0L))}}}}
@Composable private fun Stat(a:String,b:String){Card(colors=CardDefaults.cardColors(CardHigh)){Row(Modifier.fillMaxWidth().padding(18.dp),Arrangement.SpaceBetween){Text(a,fontWeight=FontWeight.Bold);Text(b,color=Primary,fontWeight=FontWeight.ExtraBold)}}}
@Composable private fun Settings(api:ApiClient){var d by remember{mutableStateOf<JSONObject?>(null)};LaunchedEffect(Unit){d=runCatching{api.get("/api/settings")}.getOrNull()};Column{Header("Pengaturan","Konfigurasi backend");LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Stat("Nama Website",d?.str("nama_website","nama").ifBlank{"Irkop Cell"})};item{Stat("Notifikasi",if(d?.optBoolean("notifhook_enabled")==true)"Aktif" else "Terkonfigurasi")}}}}
@Composable private fun More(go:(Page)->Unit,logout:()->Unit){Column{Header("Lainnya","Operasional & administrasi");LazyColumn(contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){listOf("Pelanggan" to Page.PELANGGAN,"Kasbon" to Page.KASBON,"Service HP" to Page.SERVICE,"Gaji Karyawan" to Page.GAJI,"Pengaturan" to Page.PENGATURAN).forEach{a->item{Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(CardHigh)){Text(a.first,Modifier.padding(18.dp),fontWeight=FontWeight.Bold)}}};item{Button(logout,Modifier.fillMaxWidth()){Text("Keluar")}}}}}

@Composable private fun TransactionForm(api:ApiClient,done:()->Unit){var product by remember{mutableStateOf("")};var qty by remember{mutableStateOf("1")};var method by remember{mutableStateOf("Tunai")};var err by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=done,title={Text("Transaksi Baru")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(product,{product=it},label={Text("ID Produk")},singleLine=true);OutlinedTextField(qty,{qty=it.filter(Char::isDigit)},label={Text("Qty")},singleLine=true);OutlinedTextField(method,{method=it},label={Text("Metode Bayar")},singleLine=true);if(err.isNotBlank())Text(err,color=Error)}},confirmButton={Button(enabled=!busy&&product.isNotBlank(),onClick={scope.launch{busy=true;try{val body=JSONObject().put("items",JSONArray().put(JSONObject().put("produk_id",product.toLongOrNull()?:product).put("qty",qty.toIntOrNull()?:1))).put("metode_bayar",method);api.post("/api/transaksi",body,true);done()}catch(e:Exception){err=e.message?:"Transaksi gagal"}finally{busy=false}}}){if(busy)CircularProgressIndicator(Modifier.size(18.dp))else Text("Simpan")}},dismissButton={TextButton(done){Text("Batal")}})}
