package com.irkop.cell.features.kasbon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.core.ApiError
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

private fun JsonObject.s(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)}?:"-"
private fun JsonObject.n(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.longOrNull}?:0L
private fun JsonObject.a(k:String)=this[k]?.jsonArray?.filterIsInstance<JsonObject>().orEmpty()

@Composable
fun KasbonDetailScreen(repo:Repository){
 var rows by remember{mutableStateOf(emptyList<JsonObject>())};var create by remember{mutableStateOf(false)};var selected by remember{mutableStateOf<JsonObject?>(null)};var deleting by remember{mutableStateOf<JsonObject?>(null)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
 fun load(){scope.launch{runCatching{repo.kasbon()}.onSuccess{rows=it.a("items");error=null}.onFailure{error=ApiError.message(it)}}};LaunchedEffect(Unit){load()}
 val total=rows.sumOf{it.n("nominal")};val remaining=rows.filter{it.s("status")!="lunas"}.sumOf{(it.n("nominal")-it.n("terbayar")).coerceAtLeast(0)}
 LazyColumn(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("Kasbon",style=MaterialTheme.typography.headlineMedium,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold);Text("Kelola hutang dan pelunasan pelanggan",color=MaterialTheme.colorScheme.onSurfaceVariant)};Button(onClick={create=true},shape=MaterialTheme.shapes.medium){Text("+ Kasbon")}}}
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Summary("Total",total,Modifier.weight(1f));Summary("Sisa",remaining,Modifier.weight(1f))}}
  error?.let{item{Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.errorContainer)){Text(it,Modifier.padding(14.dp),color=MaterialTheme.colorScheme.onErrorContainer)}}}
  if(rows.isEmpty())item{Text("Belum ada kasbon",color=MaterialTheme.colorScheme.onSurfaceVariant)}else items(rows){r->KasbonCard(r,{selected=r},{deleting=r})}
 }
 if(create)CreateKasbon(repo,{create=false;load()},{create=false})
 selected?.let{DetailKasbon(repo,it,{selected=null;load()},{selected=null})}
 deleting?.let{r->AlertDialog(onDismissRequest={deleting=null},title={Text("Hapus kasbon?")},text={Text("${r.s("pelanggan_nama")} akan dihapus. Backend akan menolak jika histori pembayaran tidak memungkinkan.")},confirmButton={Button(onClick={scope.launch{runCatching{repo.deleteKasbon(r.s("id"),"Hapus dari aplikasi")}.onSuccess{deleting=null;load()}.onFailure{error=ApiError.message(it);deleting=null}}}){Text("Hapus")}},dismissButton={TextButton(onClick={deleting=null}){Text("Batal")}})}
}

@Composable private fun Summary(label:String,value:Long,modifier:Modifier){Card(modifier){Column(Modifier.padding(14.dp)){Text(label,style=MaterialTheme.typography.labelMedium);Text(java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id","ID")).apply{maximumFractionDigits=0}.format(value),style=MaterialTheme.typography.titleLarge,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold)}}}
@Composable private fun KasbonCard(r:JsonObject,onDetail:()->Unit,onDelete:()->Unit){val nominal=r.n("nominal");val paid=r.n("terbayar");val sisa=(nominal-paid).coerceAtLeast(0);Card(onClick=onDetail,modifier=Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium){Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(r.s("pelanggan_nama"),style=MaterialTheme.typography.titleMedium,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold);Text(r.s("status"),color=MaterialTheme.colorScheme.primary)};Text("Bon ${money(nominal)} · Terbayar ${money(paid)} · Sisa ${money(sisa)}");Text("Tanggal ${r.s("tanggal")} · Jatuh tempo ${r.s("jatuh_tempo")}",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);TextButton(onClick=onDelete){Text("Hapus")}}}}
@Composable private fun CreateKasbon(repo:Repository,done:()->Unit,cancel:()->Unit){var customer by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var due by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=cancel,title={Text("Tambah Kasbon")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){NativeTextField(customer,{customer=it},"Pelanggan ID",numeric=true);NativeTextField(amount,{amount=it},"Nominal",numeric=true);NativeTextField(due,{due=it},"Jatuh tempo YYYY-MM-DD");NativeTextField(note,{note=it},"Catatan");error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={Button(enabled=!busy&&customer.toLongOrNull()!=null&&amount.toLongOrNull()?.let{it>0}==true,onClick={busy=true;scope.launch{runCatching{buildJsonObject{put("pelanggan_id",customer.toLong());put("nominal",amount.toLong());if(due.isNotBlank())put("jatuh_tempo",due);if(note.isNotBlank())put("catatan",note)}}.mapCatching{repo.createKasbon(it)}.onSuccess{done()}.onFailure{error=ApiError.message(it)};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton(onClick=cancel){Text("Batal")}})}
@Composable private fun DetailKasbon(repo:Repository,row:JsonObject,done:()->Unit,cancel:()->Unit){var pay by remember{mutableStateOf("")};var settle by remember{mutableStateOf(false)};var due by remember{mutableStateOf(row.s("jatuh_tempo"))};var note by remember{mutableStateOf(row.s("catatan"))};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();val sisa=(row.n("nominal")-row.n("terbayar")).coerceAtLeast(0);AlertDialog(onDismissRequest=cancel,title={Text("Detail Kasbon")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("Pelanggan: ${row.s("pelanggan_nama")}");Text("Nominal: ${money(row.n("nominal"))}");Text("Terbayar: ${money(row.n("terbayar"))}");Text("Sisa: ${money(sisa)}",fontWeight=androidx.compose.ui.text.font.FontWeight.Bold);if(sisa>0){NativeTextField(pay,{pay=it},"Bayar sebagian",numeric=true);Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(settle,{settle=it});Text("Lunasi sisa")}};NativeTextField(due,{due=it},"Jatuh tempo");NativeTextField(note,{note=it},"Catatan");error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={Button(enabled=!busy,onClick={busy=true;scope.launch{val amount=pay.toLongOrNull();runCatching{when{settle&&sisa>0->repo.payKasbon(row.s("id"),buildJsonObject{put("nominal",sisa);put("metode","tunai");put("akun_id","Tunai Laci")});amount!=null&&amount>0->repo.payKasbon(row.s("id"),buildJsonObject{put("nominal",amount);put("metode","tunai");put("akun_id","Tunai Laci")});else->repo.updateKasbon(row.s("id"),buildJsonObject{if(due!="-")put("jatuh_tempo",due);if(note!="-")put("catatan",note)})}}.onSuccess{done()}.onFailure{error=ApiError.message(it)};busy=false}}){Text(if(busy)"Memproses…" else "Simpan")}},dismissButton={TextButton(onClick=cancel){Text("Batal")}})}
private fun money(v:Long)=java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id","ID")).apply{maximumFractionDigits=0}.format(v)
