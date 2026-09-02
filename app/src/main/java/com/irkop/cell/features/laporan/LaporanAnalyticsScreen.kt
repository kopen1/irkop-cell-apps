package com.irkop.cell.features.laporan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

private fun JsonObject.s(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)}?:"-"
private fun JsonObject.n(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.longOrNull}?:0L
private fun JsonObject.a(k:String)=this[k]?.jsonArray?.filterIsInstance<JsonObject>()?:emptyList()
private fun rupiah(v:Long)=java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id","ID")).apply{maximumFractionDigits=0}.format(v)

@Composable
fun LaporanAnalyticsScreen(repo:Repository){
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    var month by remember{mutableStateOf(java.time.LocalDate.now().toString().substring(0,7))}
    var year by remember{mutableStateOf(java.time.LocalDate.now().year.toString())}
    var monthly by remember{mutableStateOf<JsonObject?>(null)}
    var yearly by remember{mutableStateOf<JsonObject?>(null)}
    var error by remember{mutableStateOf<String?>(null)}
    fun loadMonth(){scope.launch{runCatching{repo.laporanBulan(month)}.onSuccess{monthly=it;error=null}.onFailure{error=it.message}}}
    fun loadYear(){scope.launch{runCatching{repo.laporanTahun(year.toInt())}.onSuccess{yearly=it;error=null}.onFailure{error=it.message}}}
    LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        item{Text("Laporan",style=MaterialTheme.typography.headlineMedium)}
        item{OutlinedTextField(month,{month=it},label={Text("Bulan YYYY-MM")},modifier=Modifier.fillMaxWidth());Button(::loadMonth){Text("Muat Bulanan")}}
        monthly?.let{m->
            item{Text("Rekap kategori",style=MaterialTheme.typography.titleLarge)}
            items(m.a("rekap_kategori")){r->Text("${r.s("nama_kategori")} · qty ${r.n("qty")} · omzet ${rupiah(r.n("omzet"))}")}
            item{val p=m.obj("perbandingan_bulan_sebelumnya");if(p!=null)Column{Text("Bulan sebelumnya: ${p.s("bulan")}");Text("Omzet ${rupiah(p.n("omzet"))} · Laba ${rupiah(p.n("laba"))} · Pengeluaran ${rupiah(p.n("pengeluaran"))}")}}
            item{Button({printLaporan(context,"Laporan $month",buildMonthlyHtml(m))}){Text("Print laporan")}}
        }
        item{OutlinedTextField(year,{year=it.filter(Char::isDigit)},label={Text("Tahun YYYY")},modifier=Modifier.fillMaxWidth());Button(::loadYear){Text("Muat Tahunan")}}
        yearly?.let{y->
            item{Text("Breakdown 12 bulan",style=MaterialTheme.typography.titleLarge)}
            items(y.a("breakdown_12_bulan")){r->Text("${r.s("bulan")} · omzet ${rupiah(r.n("omzet"))} · laba ${rupiah(r.n("laba"))} · net ${rupiah(r.n("net"))}")}
            item{Text("Kategori terlaris",style=MaterialTheme.typography.titleLarge)}
            items(y.a("ranking_kategori_terlaris")){r->Text("${r.s("nama_kategori")} · qty ${r.n("qty")} · omzet ${rupiah(r.n("omzet"))}")}
            item{Button({printLaporan(context,"Laporan $year",buildYearlyHtml(y))}){Text("Print laporan tahunan")}}
        }
        error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}
    }
}

private fun buildMonthlyHtml(m:JsonObject):String=buildString{append("<h2>Rekap kategori</h2><table><tr><th>Kategori</th><th>Qty</th><th>Omzet</th></tr>");m.a("rekap_kategori").forEach{append("<tr><td>${esc(it.s("nama_kategori"))}</td><td>${it.n("qty")}</td><td>${rupiah(it.n("omzet"))}</td></tr>")};append("</table>")}
private fun buildYearlyHtml(y:JsonObject):String=buildString{append("<h2>12 bulan</h2><table><tr><th>Bulan</th><th>Omzet</th><th>Laba</th><th>Net</th></tr>");y.a("breakdown_12_bulan").forEach{append("<tr><td>${esc(it.s("bulan"))}</td><td>${rupiah(it.n("omzet"))}</td><td>${rupiah(it.n("laba"))}</td><td>${rupiah(it.n("net"))}</td></tr>")};append("</table>")}
private fun esc(s:String)=s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
