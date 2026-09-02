package com.irkop.cell.features.pengaturan

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
private fun JsonObject.permissions()=this["permissions"]?.jsonArray?.mapNotNull{it.jsonPrimitive.contentOrNull}?.toSet()?:emptySet()

private val pages=listOf("dashboard","transaksi","kasir","laporan","daftar_barang","laporan_service_hp","kasbon","pelanggan","pengeluaran","pengaturan")

@Composable
fun UserManagementScreen(repo:Repository){
    var users by remember{mutableStateOf(emptyList<JsonObject>())};var selected by remember{mutableStateOf<JsonObject?>(null)};var create by remember{mutableStateOf(false)};var error by remember{mutableStateOf<String?>(null)};val scope=rememberCoroutineScope()
    fun load(){scope.launch{runCatching{repo.users()}.onSuccess{root->users=if(root["items"] is JsonArray)root["items"]!!.jsonArray.filterIsInstance<JsonObject>() else emptyList();error=null}.onFailure{error=it.message}}}
    LaunchedEffect(Unit){load()}
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Manajemen User",style=MaterialTheme.typography.headlineMedium);Button({create=true}){Text("Tambah user")}}
        error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){items(users){u->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp)){Text("${u.s("nama")} · ${u.s("username")}");Text("Role: ${u.s("role")} · Aktif: ${u.s("aktif")}");Text("Permission: ${u.permissions().joinToString()}");TextButton({selected=u}){Text("Edit user / permission")}}}}}
    }
    if(create)CreateUserDialog(repo,{create=false;load()},{create=false})
    selected?.let{EditUserDialog(repo,it,{selected=null;load()},{selected=null})}
}

@Composable private fun CreateUserDialog(repo:Repository,onSaved:()->Unit,onCancel:()->Unit){var name by remember{mutableStateOf("")};var username by remember{mutableStateOf("")};var password by remember{mutableStateOf("")};var role by remember{mutableStateOf("karyawan")};var selected by remember{mutableStateOf(emptySet<String>())};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=onCancel,title={Text("Tambah User")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){item{NativeTextField(name,{name=it},"Nama")};item{NativeTextField(username,{username=it},"Username")};item{NativeTextField(password,{password=it},"Password")};item{Row{listOf("admin","karyawan").forEach{FilterChip(role==it,{role=it},label={Text(it)})}}};items(pages){p->FilterChip(selected.contains(p),{selected=if(selected.contains(p))selected-p else selected+p},label={Text(p)})};error?.let{item{Text(it)}}}},confirmButton={Button(enabled=name.isNotBlank()&&username.isNotBlank()&&password.length>=8&&!busy,onClick={busy=true;scope.launch{runCatching{repo.createUser(buildJsonObject{put("nama",name.trim());put("username",username.trim());put("password",password);put("role",role);putJsonArray("permissions"){selected.forEach{add(it)}}})}.onSuccess{onSaved()}.onFailure{error=it.message};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton(onClick=onCancel){Text("Batal")}})}

@Composable private fun EditUserDialog(repo:Repository,user:JsonObject,onSaved:()->Unit,onCancel:()->Unit){var name by remember(user){mutableStateOf(user.s("nama"))};var active by remember(user){mutableStateOf(user.s("aktif")!="0"&&user.s("aktif")!="false")};var perms by remember(user){mutableStateOf(user.permissions())};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)};val scope=rememberCoroutineScope();AlertDialog(onDismissRequest=onCancel,title={Text("Edit User & Permission")},text={LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){item{NativeTextField(name,{name=it},"Nama")};item{Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Checkbox(active,{active=it});Text("Aktif")}};items(pages){p->FilterChip(perms.contains(p),{perms=if(perms.contains(p))perms-p else perms+p},label={Text(p)})};error?.let{item{Text(it)}}}},confirmButton={Button(enabled=name.isNotBlank()&&!busy,onClick={busy=true;scope.launch{runCatching{repo.updateUser(user.s("id"),buildJsonObject{put("nama",name.trim());put("aktif",active)});repo.updateUserPermissions(user.s("id"),buildJsonObject{putJsonArray("halaman"){perms.forEach{add(it)}}})}.onSuccess{onSaved()}.onFailure{error=it.message};busy=false}}){Text(if(busy)"Menyimpan…" else "Simpan")}},dismissButton={TextButton(onClick=onCancel){Text("Batal")}})}
