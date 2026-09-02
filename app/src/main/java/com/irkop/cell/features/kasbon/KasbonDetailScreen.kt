package com.irkop.cell.features.kasbon

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
private fun JsonObject.a(k:String)=this[k]?.jsonArray?.filterIsInstance<JsonObject>()?:emptyList()

@Composable
fun KasbonDetailScreen(repo:Repository){
    var rows by remember{mutableStateOf(emptyList<JsonObject>())};var selected by remember{mutableStateOf<JsonObject?>(null)};var deleting by remember{mutableStateOf<JsonObject?>(null)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
    fun load(){scope.launch{runCatching{repo.kasbon()}.onSuccess{rows=it.a("items");error=null}.onFailure{error=it.message}}}
    LaunchedEffect(Unit){load()}
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Kasbon",style=MaterialTheme.typography.headlineMedium);Button(::load){Text("Refresh")};error?.let{Text(it,color=MaterialTheme.colorScheme.error)};LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){items(rows){r->Card({selected=r},Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){Text(r.s("pelanggan_nama"),style=MaterialTheme.typography.titleMedium);Text("Nominal ${r.n("nominal")} · Terbayar ${r.n("terbayar")} · Sisa ${r.n("sisa")}");Text("Status ${r.s("status")}");Row{TextButton({selected=r}){Text("Detail")};TextButton({deleting=r}){Text("Hapus")}}}}}}}
    selected?.let{KasbonDetailDialog(repo,it,{selected=null;load()},{selected=null})}
    deleting?.let{row->AlertDialog(onDismissRequest={deleting=null},title={Text("Hapus kasbon?")},text={Text("Data ${row.s("pelanggan_nama")} akan dihapus. Kasbon yang sudah memiliki pembayaran atau terhubung transaksi akan ditolak backend demi menjaga histori keuangan.")},confirmButton={Button({scope.launch{runCatching{repo.deleteKasbon(row.s("id"),"Hapus dari aplikasi")}.onSuccess{deleting=null;load()}.onFailure{error=it.message;deleting=null}}}){Text("Hapus")}},dismissButton={TextButton({deleting=null}){Text("Batal")}})}
}

@Composable private fun KasbonDetailDialog(repo:Repository,row:JsonObject,onSaved:()->Unit,onCancel:()->Unit){
    var due by remember(row){mutableStateOf(row.s("jatuh_tempo"))};var note by remember(row){mutableStateOf(row.s("catatan"))};var settle by remember{mutableStateOf(false)};var account by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope()
    AlertDialog(onDismissRequest=onCancel,title={Text("Detail Kasbon")},text={Column(verticalArrangement=Arrangement.spacedBy(7.dp)){Text("Pelanggan: ${row.s("pelanggan_nama")}");Text("Nominal: ${row.n("nominal")}");Text("Terbayar: ${row.n("terbayar")}");Text("Sisa: ${row.n("sisa")}");NativeTextField(due,{due=it},"Jatuh tempo");NativeTextField(note,{note=it},"Catatan");if(row.s("status")!="lunas")NativeTextField(account,{account=it},"Akun pelunasan (opsional)");if(row.s("status")!="lunas")Row{Checkbox(settle,{settle=it});Text("Lunasi sisa")};error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={Button(enabled=!busy,onClick={busy=true;scope.launch{runCatching{buildJsonObject{put("jatuh_tempo",due.takeIf{it!="-"});put("catatan",note.takeIf{it!="-"});if(settle){put("status","lunas");if(account.isNotBlank())put("akun",account)}}.also{repo.updateKasbon(row.s("id"),it)}}.onSuccess{onSaved()}.onFailure{error=it.message};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton(onClick=onCancel){Text("Batal")}})
}
