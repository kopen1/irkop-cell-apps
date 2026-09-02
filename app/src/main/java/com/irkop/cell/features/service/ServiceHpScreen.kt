package com.irkop.cell.features.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

private fun JsonObject.s(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)}?:"-"
private fun JsonObject.n(vararg k:String)=k.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.longOrNull}?:0L

@Composable
fun ServiceHpScreen(repo:Repository){
    var rows by remember{mutableStateOf(emptyList<JsonObject>())};var selected by remember{mutableStateOf<JsonObject?>(null)};var create by remember{mutableStateOf(false)};var deleting by remember{mutableStateOf<JsonObject?>(null)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
    fun load(){scope.launch{runCatching{repo.serviceHp()}.onSuccess{rows=it["items"]?.jsonArray?.filterIsInstance<JsonObject>()?:emptyList();error=null}.onFailure{error=it.message}}}
    LaunchedEffect(Unit){load()}
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Service HP",style=MaterialTheme.typography.headlineMedium);Button({create=true}){Text("Tambah")}};Button(::load,Modifier.fillMaxWidth()){Text("Refresh")};error?.let{Text(it,color=MaterialTheme.colorScheme.error)};LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){items(rows){r->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(r.s("nama_device"),style=MaterialTheme.typography.titleMedium);Text("Pelanggan: ${r.s("pelanggan_nama")}");Text("Status: ${r.s("status")}");Text("Estimasi: ${r.n("estimasi_biaya")} · Biaya: ${r.n("biaya")}");Text(r.s("deskripsi_kerusakan"));Row{TextButton({selected=r}){Text("Edit")};TextButton({deleting=r}){Text("Hapus")}}}}}}}
    if(create)ServiceForm(repo,null,{create=false;load()},{create=false})
    selected?.let{ServiceForm(repo,it,{selected=null;load()},{selected=null})}
    deleting?.let{r->AlertDialog(onDismissRequest={deleting=null},title={Text("Hapus service?")},text={Text("${r.s("nama_device")} akan dihapus dari daftar service. Histori tetap dicatat sebagai soft-delete.")},confirmButton={Button({scope.launch{runCatching{repo.deleteServiceHp(r.s("id"))}.onSuccess{deleting=null;load()}.onFailure{error=it.message;deleting=null}}}){Text("Hapus")}},dismissButton={TextButton({deleting=null}){Text("Batal")}})}
}

@Composable private fun ServiceForm(repo:Repository,row:JsonObject?,onSaved:()->Unit,onCancel:()->Unit){var customer by remember(row){mutableStateOf(row?.s("pelanggan_id")?:"")};var device by remember(row){mutableStateOf(row?.s("nama_device")?:"")};var complaint by remember(row){mutableStateOf(row?.s("deskripsi_kerusakan")?:"")};var estimate by remember(row){mutableStateOf(row?.s("estimasi_biaya")?.takeUnless{it=="-"}?:"")};var cost by remember(row){mutableStateOf(row?.s("biaya")?.takeUnless{it=="-"}?:"")};var modal by remember(row){mutableStateOf(row?.s("harga_modal")?.takeUnless{it=="-"}?:"")};var note by remember(row){mutableStateOf(row?.s("catatan")?.takeUnless{it=="-"}?:"")};var status by remember(row){mutableStateOf(row?.s("status")?.takeUnless{it=="-"}?:"masuk")};var date by remember(row){mutableStateOf(row?.s("tanggal_masuk")?.takeUnless{it=="-"}?:"")};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=onCancel,title={Text(if(row==null)"Tambah Service HP" else "Edit Service HP")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){NativeTextField(customer,{customer=it},"Pelanggan ID",numeric=true);NativeTextField(device,{device=it},"Nama device");NativeTextField(complaint,{complaint=it},"Deskripsi kerusakan");NativeTextField(estimate,{estimate=it},"Estimasi biaya",numeric=true);NativeTextField(cost,{cost=it},"Biaya final",numeric=true);NativeTextField(modal,{modal=it},"Harga modal",numeric=true);NativeTextField(date,{date=it},"Tanggal masuk");NativeTextField(note,{note=it},"Catatan");Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("masuk","proses","selesai","diambil").forEach{FilterChip(status==it,{status=it},label={Text(it)})}};error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={Button(enabled=!busy&&device.isNotBlank()&&complaint.isNotBlank()&&customer.toLongOrNull()!=null,onClick={busy=true;scope.launch{runCatching{buildJsonObject{put("nama_device",device.trim());put("deskripsi_kerusakan",complaint.trim());put("pelanggan_id",customer.toLong());if(estimate.isNotBlank())put("estimasi_biaya",estimate.toLong());if(modal.isNotBlank())put("harga_modal",modal.toLong());if(cost.isNotBlank())put("biaya",cost.toLong());if(note.isNotBlank())put("catatan",note.trim());if(date.isNotBlank())put("tanggal_masuk",date);if(row!=null)put("status",status)}}.let{body->if(row==null)repo.createServiceHp(body) else repo.updateServiceHp(row.s("id"),body)}}.onSuccess{onSaved()}.onFailure{error=it.message};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton({onCancel()}){Text("Batal")}})}
