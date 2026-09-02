package com.irkop.cell.features.akun

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
fun AkunUangScreen(repo:Repository){
    var rows by remember{mutableStateOf(emptyList<JsonObject>())};var selected by remember{mutableStateOf<JsonObject?>(null)};var create by remember{mutableStateOf(false)};var deleting by remember{mutableStateOf<JsonObject?>(null)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
    fun load(){scope.launch{runCatching{repo.akun()}.onSuccess{rows=it["items"]?.jsonArray?.filterIsInstance<JsonObject>()?:emptyList();error=null}.onFailure{error=it.message}}}
    LaunchedEffect(Unit){load()}
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Akun Uang",style=MaterialTheme.typography.headlineMedium);Button({create=true}){Text("Tambah")}};Button(::load,Modifier.fillMaxWidth()){Text("Refresh")};error?.let{Text(it,color=MaterialTheme.colorScheme.error)};LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){items(rows){r->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(r.s("nama_akun"),style=MaterialTheme.typography.titleMedium);Text("Tipe: ${r.s("tipe")} · Aktif: ${r.s("aktif")}");Text("Saldo: ${r.n("saldo")}");Row{TextButton({selected=r}){Text("Edit")};TextButton({deleting=r}){Text("Hapus")}}}}}}}
    if(create)AccountForm(repo,null,{create=false;load()},{create=false})
    selected?.let{AccountForm(repo,it,{selected=null;load()},{selected=null})}
    deleting?.let{r->AlertDialog(onDismissRequest={deleting=null},title={Text("Hapus akun?")},text={Text("Akun ${r.s("nama_akun")} akan dinonaktifkan. Data mutasi lama tidak dihapus agar histori keuangan tetap utuh.")},confirmButton={Button({scope.launch{runCatching{repo.deleteAkun(r.s("id"))}.onSuccess{deleting=null;load()}.onFailure{error=it.message;deleting=null}}}){Text("Nonaktifkan")}},dismissButton={TextButton({deleting=null}){Text("Batal")}})}
}

@Composable private fun AccountForm(repo:Repository,row:JsonObject?,onSaved:()->Unit,onCancel:()->Unit){var name by remember(row){mutableStateOf(row?.s("nama_akun")?.takeUnless{it=="-"}?:"")};var type by remember(row){mutableStateOf(row?.s("tipe")?.takeUnless{it=="-"}?:"tunai")};var active by remember(row){mutableStateOf(row?.s("aktif")!="0"&&row?.s("aktif")!="false")};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();val types=listOf("tunai","bank","e_wallet","digital","lainnya");AlertDialog(onDismissRequest=onCancel,title={Text(if(row==null)"Tambah Akun" else "Edit Akun")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){NativeTextField(name,{name=it},"Nama akun");Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){types.forEach{FilterChip(type==it,{type=it},label={Text(it)})}};if(row!=null)Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(active,{active=it});Text("Aktif")};error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={Button(enabled=name.isNotBlank()&&!busy,onClick={busy=true;scope.launch{runCatching{if(row==null)repo.createAkun(buildJsonObject{put("nama_akun",name.trim());put("tipe",type)})else repo.updateAkun(row.s("id"),buildJsonObject{put("nama_akun",name.trim());put("tipe",type);put("aktif",active)})}.onSuccess{onSaved()}.onFailure{error=it.message};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton({onCancel()}){Text("Batal")}})}
