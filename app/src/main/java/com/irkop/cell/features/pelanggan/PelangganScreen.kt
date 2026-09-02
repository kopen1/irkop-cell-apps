package com.irkop.cell.features.pelanggan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

private fun JsonObject.s(vararg keys:String)=keys.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)}?:"-"
private fun JsonObject.n(vararg keys:String)=keys.firstNotNullOfOrNull{this[it]?.jsonPrimitive?.longOrNull}?:0L
private fun JsonObject.rows(key:String)=this[key]?.jsonArray?.filterIsInstance<JsonObject>()?:emptyList()

@Composable
fun PelangganScreen(repo:Repository) {
    var rows by remember{mutableStateOf(emptyList<JsonObject>())}
    var query by remember{mutableStateOf("")}
    var error by remember{mutableStateOf<String?>(null)}
    var form by remember{mutableStateOf<JsonObject?>(null)}
    var newForm by remember{mutableStateOf(false)}
    var detail by remember{mutableStateOf<JsonObject?>(null)}
    var merge by remember{mutableStateOf<JsonObject?>(null)}
    var deleting by remember{mutableStateOf<JsonObject?>(null)}
    val scope=rememberCoroutineScope()
    fun load(){scope.launch{runCatching{repo.pelanggan(query.ifBlank{null})}.onSuccess{rows=it.rows("items");error=null}.onFailure{error=it.message}}}
    LaunchedEffect(Unit){load()}
    Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Pelanggan",style=MaterialTheme.typography.headlineMedium);Button({newForm=true}){Text("Tambah")}}
        OutlinedTextField(query,{query=it},label={Text("Cari nama / telepon")},modifier=Modifier.fillMaxWidth(),singleLine=true)
        Button(::load,Modifier.fillMaxWidth()){Text("Cari")}
        error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        LazyColumn(verticalArrangement=Arrangement.spacedBy(6.dp)){
            items(rows){r->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                Text(r.s("nama"),style=MaterialTheme.typography.titleMedium);Text(r.s("telepon"));Text("Belanja ${r.n("total_belanja")} · ${r.n("jumlah_transaksi")} transaksi")
                Row{TextButton({scope.launch{runCatching{repo.pelangganDetail(r.s("id"))}.onSuccess{detail=it}.onFailure{error=it.message}}}){Text("Detail")};TextButton({form=r}){Text("Edit")};TextButton({merge=r}){Text("Merge")};TextButton({deleting=r}){Text("Hapus")}}
            }}}
        }
    }
    if(newForm)PelangganForm(repo,onSaved={newForm=false;load()},onCancel={newForm=false})
    form?.let{r->PelangganForm(repo,r.s("id"),r.s("nama"),r.s("telepon"),onSaved={form=null;load()},onCancel={form=null})}
    detail?.let{PelangganDetailDialog(it){detail=null}}
    merge?.let{PelangganMergeDialog(repo,it.s("id"),it.s("nama"),onMerged={merge=null;load()},onCancel={merge=null})}
    deleting?.let{r->AlertDialog({deleting=null},title={Text("Hapus pelanggan?")},text={Text("${r.s("nama")} akan dihapus permanen." )},confirmButton={Button({scope.launch{runCatching{repo.deletePelanggan(r.s("id"))}.onSuccess{deleting=null;load()}.onFailure{error=it.message;deleting=null}}}){Text("Hapus")}},dismissButton={TextButton({deleting=null}){Text("Batal")}})}
}
