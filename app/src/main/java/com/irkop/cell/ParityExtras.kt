package com.irkop.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.core.AuthPolicy
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.util.Locale

private val ParityRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
private fun pmoney(v: Long) = ParityRupiah.format(v)
private fun JsonObject.xs(vararg keys: String): String = keys.firstNotNullOfOrNull { string(it)?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.xn(vararg keys: String): Long = keys.firstNotNullOfOrNull { long(it) } ?: 0L
private fun JsonObject.xa(key: String): List<JsonObject> = array(key)?.filterIsInstance<JsonObject>() ?: emptyList()
private fun JsonElement.xsValue(): String = jsonPrimitive.contentOrNull ?: "-"

@Composable
fun ParityExtrasScreen(user: UserSession, repo: Repository) {
    var tab by remember { mutableStateOf("pelanggan") }
    val tabs = buildList {
        if (AuthPolicy.canAccess(user, "pelanggan")) add("pelanggan" to "Pelanggan")
        if (AuthPolicy.canAccess(user, "kasbon")) add("kasbon" to "Kasbon")
        if (AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)) add("laporan" to "Laporan")
        if (user.role.equals("admin", true)) add("admin" to "Admin")
    }
    LaunchedEffect(tabs) { if (tabs.none { it.first == tab }) tab = tabs.firstOrNull()?.first ?: "pelanggan" }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fitur Lanjutan", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { tabs.forEach { (k, label) -> FilterChip(tab == k, { tab = k }, label = { Text(label) }) } }
        when (tab) {
            "pelanggan" -> CustomerParity(repo)
            "kasbon" -> KasbonDetailParity(repo)
            "laporan" -> ReportParity(repo)
            "admin" -> AdminParity(repo)
        }
    }
}

@Composable
private fun CustomerParity(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var detail by remember { mutableStateOf<JsonObject?>(null) }
    var selected by remember { mutableStateOf<JsonObject?>(null) }
    var mergeId by remember { mutableStateOf("") }
    var q by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun load() { scope.launch { runCatching { repo.pelanggan(q.ifBlank { null }) }.onSuccess { rows = it.xa("items") }.onFailure { error = it.message } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(q, { q = it }, label = { Text("Cari pelanggan") }, modifier = Modifier.fillMaxWidth())
        Button(::load) { Text("Cari") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(rows) { r ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text("${r.xs("nama")} · ${r.xs("telepon")}")
                        Text("Transaksi ${r.xn("jumlah_transaksi")} · Belanja ${pmoney(r.xn("total_belanja"))}")
                        Row {
                            TextButton({ scope.launch { runCatching { repo.pelangganDetail(r.xs("id")) }.onSuccess { detail = it }.onFailure { error = it.message } } }) { Text("Detail / Riwayat") }
                            TextButton({ selected = r; mergeId = "" }) { Text("Merge") }
                        }
                    }
                }
            }
        }
    }
    detail?.let { d ->
        AlertDialog({ detail = null }, title = { Text("${d.xs("nama")} — detail") }, text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item { Text("Telepon: ${d.xs("telepon")}") }
                item { Text("Riwayat transaksi", style = MaterialTheme.typography.titleMedium) }
                items(d.xa("riwayat_transaksi")) { t -> Text("${t.xs("kode_transaksi", "id")} · ${pmoney(t.xn("total"))}") }
                item { Text("Kasbon", style = MaterialTheme.typography.titleMedium) }
                items(d.xa("kasbon")) { k -> Text("${pmoney(k.xn("nominal"))} · ${k.xs("status")}") }
            }
        }, confirmButton = { TextButton({ detail = null }) { Text("Tutup") } })
    }
    selected?.let { s ->
        AlertDialog({ selected = null }, title = { Text("Gabungkan pelanggan") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Utama: ${s.xs("nama")} (ID ${s.xs("id")})")
                OutlinedTextField(mergeId, { mergeId = it.filter(Char::isDigit) }, label = { Text("ID pelanggan yang digabung") }, modifier = Modifier.fillMaxWidth())
            }
        }, confirmButton = {
            Button({
                scope.launch {
                    runCatching {
                        repo.mergePelanggan(buildJsonObject {
                            put("id_utama", s.xs("id").toLong())
                            put("id_gabung", mergeId.toLong())
                        })
                    }.onSuccess { selected = null; load() }.onFailure { error = it.message }
                }
            }) { Text("Gabungkan") }
        }, dismissButton = { TextButton({ selected = null }) { Text("Batal") } })
    }
}

@Composable
private fun KasbonDetailParity(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var selected by remember { mutableStateOf<JsonObject?>(null) }
    val scope = rememberCoroutineScope()
    fun load() { scope.launch { runCatching { repo.kasbon() }.onSuccess { rows = it.xa("items") } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        Button(::load) { Text("Refresh") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(rows) { r ->
                Card({ selected = r }, Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text(r.xs("pelanggan_nama"))
                        Text("Nominal ${pmoney(r.xn("nominal"))} · Terbayar ${pmoney(r.xn("terbayar"))} · Sisa ${pmoney(r.xn("sisa"))}")
                        Text(r.xs("status"))
                    }
                }
            }
        }
    }
    selected?.let { r -> AlertDialog({ selected = null }, title = { Text("Detail Kasbon") }, text = { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text("Pelanggan: ${r.xs("pelanggan_nama")}"); Text("Tanggal: ${r.xs("tanggal")}"); Text("Nominal: ${pmoney(r.xn("nominal"))}"); Text("Terbayar: ${pmoney(r.xn("terbayar"))}"); Text("Sisa: ${pmoney(r.xn("sisa"))}"); Text("Status: ${r.xs("status")}"); Text("Catatan: ${r.xs("catatan")}") } }, confirmButton = { TextButton({ selected = null }) { Text("Tutup") } }) }
}

@Composable
private fun ReportParity(repo: Repository) {
    var month by remember { mutableStateOf(java.time.LocalDate.now().toString().take(7)) }
    var year by remember { mutableStateOf(java.time.LocalDate.now().year.toString()) }
    var monthly by remember { mutableStateOf<JsonObject?>(null) }
    var yearly by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            OutlinedTextField(month, { month = it }, label = { Text("Bulan YYYY-MM") }, modifier = Modifier.fillMaxWidth())
            Button({ scope.launch { runCatching { repo.laporanBulan(month) }.onSuccess { monthly = it }.onFailure { error = it.message } } }) { Text("Muat laporan bulan") }
            OutlinedTextField(year, { year = it }, label = { Text("Tahun YYYY") }, modifier = Modifier.fillMaxWidth())
            Button({ scope.launch { runCatching { repo.laporanTahun(year.toInt()) }.onSuccess { yearly = it }.onFailure { error = it.message } } }) { Text("Muat laporan tahun") }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        monthly?.let { m ->
            item { Text("Rekap kategori", style = MaterialTheme.typography.titleLarge) }
            items(m.xa("rekap_kategori")) { r -> Text("${r.xs("nama_kategori")} · qty ${r.xn("qty")} · omzet ${pmoney(r.xn("omzet"))}") }
            m.obj("perbandingan_bulan_sebelumnya")?.let { p -> item { Text("Bulan sebelumnya: ${p.xs("bulan")} · omzet ${pmoney(p.xn("omzet"))} · laba ${pmoney(p.xn("laba"))}") } }
        }
        yearly?.let { y ->
            item { Text("Breakdown 12 bulan", style = MaterialTheme.typography.titleLarge) }
            items(y.xa("breakdown_12_bulan")) { r -> Text("${r.xs("bulan")} · omzet ${pmoney(r.xn("omzet"))} · laba ${pmoney(r.xn("laba"))} · net ${pmoney(r.xn("net"))}") }
            item { Text("Kategori terlaris", style = MaterialTheme.typography.titleLarge) }
            items(y.xa("ranking_kategori_terlaris")) { r -> Text("${r.xs("nama_kategori")} · qty ${r.xn("qty")} · omzet ${pmoney(r.xn("omzet"))}") }
        }
    }
}

@Composable
private fun AdminParity(repo: Repository) {
    var users by remember { mutableStateOf(emptyList<JsonObject>()) }
    var selected by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun load() {
        scope.launch {
            runCatching { repo.users() }.onSuccess { root ->
                users = root.xa("items")
            }.onFailure { error = it.message }
        }
    }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        Text("User management & permission", style = MaterialTheme.typography.titleLarge)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(::load) { Text("Refresh users") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            items(users) { u ->
                Card({ selected = u }, Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text("${u.xs("nama")} · ${u.xs("username")}")
                        Text("Role ${u.xs("role")} · aktif ${u.xs("aktif")}")
                        val permissions = u["permissions"]?.jsonArray?.joinToString { it.xsValue() } ?: "-"
                        Text("Permission: $permissions")
                    }
                }
            }
        }
    }
    selected?.let { u ->
        var name by remember(u) { mutableStateOf(u.xs("nama")) }
        var role by remember(u) { mutableStateOf(u.xs("role")) }
        var perms by remember(u) { mutableStateOf(u["permissions"]?.jsonArray?.map { it.xsValue() }?.toSet() ?: emptySet()) }
        val pages = listOf("dashboard", "transaksi", "kasir", "laporan", "kasbon", "pelanggan", "pengeluaran", "pengaturan")
        AlertDialog({ selected = null }, title = { Text("Edit user & permission") }, text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                item { OutlinedTextField(name, { name = it }, label = { Text("Nama") }, modifier = Modifier.fillMaxWidth()) }
                item { Text("Role: $role") }
                items(pages) { p -> FilterChip(perms.contains(p), { perms = if (perms.contains(p)) perms - p else perms + p }, label = { Text(p) }) }
            }
        }, confirmButton = {
            Button({
                scope.launch {
                    runCatching {
                        repo.updateUser(u.xs("id"), buildJsonObject { put("nama", name); put("role", role) })
                        repo.updateUserPermissions(u.xs("id"), buildJsonObject { putJsonArray("halaman") { perms.forEach { add(it) } } })
                    }.onSuccess { selected = null; load() }.onFailure { error = it.message }
                }
            }) { Text("Simpan") }
        }, dismissButton = { TextButton({ selected = null }) { Text("Batal") } })
    }
}
