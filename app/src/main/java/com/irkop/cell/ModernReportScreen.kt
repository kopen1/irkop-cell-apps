package com.irkop.cell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.irkop.cell.core.ApiError
import com.irkop.cell.data.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val ReportRp=NumberFormat.getCurrencyInstance(Locale("id","ID")).apply{maximumFractionDigits=0}
private fun reportMoney(v:Long?)=ReportRp.format(v?:0L)
private fun JsonObject.reportNum(vararg keys:String)=keys.firstNotNullOfOrNull{long(it)}?:0L

@Composable fun ModernReport(repo:Repository){val scope=rememberCoroutineScope();var month by remember{mutableStateOf(LocalDate.now().toString().substring(0,7))};var data by remember{mutableStateOf<JsonObject?>(null)};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)};suspend fun load(){loading=true;runCatching{repo.laporanBulan(month)}.onSuccess{data=it}.onFailure{error=ApiError.message(it)};loading=false};LaunchedEffect(Unit){load()};val s=data?.obj("summary")?:data?.obj("ringkasan")?:data;val series=data?.array("harian")?.filterIsInstance<JsonObject>()?:data?.array("daily")?.filterIsInstance<JsonObject>().orEmpty();LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(top=12.dp,bottom=24.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("Laporan",style=MaterialTheme.typography.headlineMedium,modifier=Modifier.weight(1f));Icon(Icons.Default.CalendarMonth,"Tanggal")}};item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){listOf("Ringkasan","Penjualan","Kasbon","Produk").forEachIndexed{i,x->FilterChip(selected=i==0,onClick={},label={Text(x)})}}};item{Row(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.CenterVertically){OutlinedTextField(month,{month=it},label={Text("Periode YYYY-MM")},modifier=Modifier.weight(1f),singleLine=true);Button(onClick={scope.launch{load()}}){Text("Muat")}}};if(loading)item{Box(Modifier.fillMaxWidth().padding(24.dp),contentAlignment=Alignment.Center){CircularProgressIndicator()}}else{item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){ReportMetric("Total Pendapatan",reportMoney(s?.reportNum("omzet","total_omzet")),Modifier.weight(1f));ReportMetric("Total Transaksi",s?.reportNum("jumlah_transaksi","transaksi","total_items").toString(),Modifier.weight(1f))}};item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){ReportMetric("Total Kasbon",reportMoney(s?.reportNum("kasbon","kasbon_aktif")),Modifier.weight(1f));ReportMetric("Total Pelanggan",s?.reportNum("pelanggan","jumlah_pelanggan").toString(),Modifier.weight(1f))}};item{RevenueChart(series)};item{Card{Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Ringkasan Keuangan",style=MaterialTheme.typography.titleMedium);listOf("Omzet" to s?.reportNum("omzet","total_omzet"),"Laba" to s?.reportNum("laba","total_laba"),"Pengeluaran" to s?.reportNum("pengeluaran","total_pengeluaran"),"Kasbon" to s?.reportNum("kasbon","kasbon_aktif"),"Net" to s?.reportNum("net","laba_bersih")).forEach{(k,v)->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(k);Text(reportMoney(v))}}}}}};error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}}}
@Composable private fun ReportMetric(a:String,b:String,m:Modifier){Card(m){Column(Modifier.padding(13.dp)){Text(a,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(b,style=MaterialTheme.typography.titleLarge);Text("Periode terpilih",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)}}}
@Composable private fun RevenueChart(rows:List<JsonObject>){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(15.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Grafik Pendapatan",style=MaterialTheme.typography.titleMedium);AssistChip(onClick={},label={Text("Per Hari")})};Spacer(Modifier.height(8.dp));if(rows.isEmpty())Text("Data harian belum tersedia dari API.",color=MaterialTheme.colorScheme.onSurfaceVariant)else{val values=rows.map{it.reportNum("omzet","pendapatan","total","nominal")};Canvas(Modifier.fillMaxWidth().height(170.dp)){val max=(values.maxOrNull()?:1L).coerceAtLeast(1L).toFloat();val step=if(values.size<2)size.width else size.width/(values.size-1);val path=Path();values.forEachIndexed{i,v->{val x=step*i;val y=size.height-v.toFloat()/max*size.height;if(i==0)path.moveTo(x,y)else path.lineTo(x,y);drawCircle(MaterialTheme.colorScheme.primary,5f,androidx.compose.ui.geometry.Offset(x,y))}};drawPath(path,MaterialTheme.colorScheme.primary,style=Stroke(5f,cap=StrokeCap.Round))}}}}}
