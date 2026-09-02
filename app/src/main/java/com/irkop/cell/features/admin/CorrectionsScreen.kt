package com.irkop.cell.features.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

private fun JsonObject.s(vararg k: String) = k.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank) } ?: "-"
private fun JsonObject.n(vararg k: String) = k.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L
private fun JsonObject.a(k: String) = this[k]?.jsonArray?.filterIsInstance<JsonObject>() ?: emptyList()

@Composable
fun CorrectionsScreen(repo: Repository) {
    var tab by remember { mutableStateOf("produk") }
    val tabs = listOf("produk" to "Hapus Produk", "pengeluaran" to "Hapus Pengeluaran", "gaji" to "Edit Gaji", "rate" to "Rate Gaji")

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tabs.forEach { (key, label) ->
                FilterChip(
                    selected = tab == key,
                    onClick = { tab = key },
                    label = { Text(label, maxLines = 1) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().weight(1f)) {
            when (tab) {
                "produk" -> DeleteProduct(repo)
                "pengeluaran" -> DeleteExpense(repo)
                "gaji" -> EditSalary(repo)
                "rate" -> EditRate(repo)
            }
        }
    }
}

@Composable
private fun DeleteProduct(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun load() { scope.launch { runCatching { repo.produk() }.onSuccess { rows = it.a("items") }.onFailure { error = it.message } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        Text("Hapus / nonaktifkan produk", style = MaterialTheme.typography.titleLarge)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
            items(rows) { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(r.s("nama"), Modifier.weight(1f))
                    TextButton({ scope.launch { runCatching { repo.deleteProduk(r.s("id")) }.onSuccess { load() }.onFailure { error = it.message } } }) { Text("Hapus") }
                }
            }
        }
    }
}

@Composable
private fun DeleteExpense(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun load() { scope.launch { runCatching { repo.pengeluaran() }.onSuccess { rows = it.a("items") }.onFailure { error = it.message } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        Text("Hapus Pengeluaran", style = MaterialTheme.typography.titleLarge)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
            items(rows) { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${r.s("deskripsi")} · ${r.n("nominal")}", Modifier.weight(1f))
                    TextButton({ scope.launch { runCatching { repo.deletePengeluaran(r.s("id"), "Dihapus dari aplikasi") }.onSuccess { load() }.onFailure { error = it.message } } }) { Text("Hapus") }
                }
            }
        }
    }
}

@Composable
private fun EditSalary(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var edit by remember { mutableStateOf<JsonObject?>(null) }
    val scope = rememberCoroutineScope()
    fun load() { scope.launch { runCatching { repo.gaji() }.onSuccess { rows = it.a("items") } } }
    LaunchedEffect(Unit) { load() }
    Column(Modifier.fillMaxSize()) {
        Text("Edit Gaji", style = MaterialTheme.typography.titleLarge)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
            items(rows) { r ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${r.s("karyawan_nama", "nama")} · ${r.n("nominal", "gaji")}", Modifier.weight(1f))
                    TextButton({ edit = r }) { Text("Edit") }
                }
            }
        }
    }
    edit?.let { r ->
        var amount by remember(r) { mutableStateOf(r.s("nominal", "gaji")) }
        AlertDialog(
            onDismissRequest = { edit = null },
            title = { Text("Edit Gaji") },
            text = { NativeTextField(amount, { amount = it }, "Nominal", numeric = true) },
            confirmButton = {
                Button({
                    scope.launch {
                        runCatching { repo.updateGaji(r.s("id"), buildJsonObject { put("nominal", amount.toLongOrNull() ?: 0L) }) }
                            .onSuccess { edit = null; load() }
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton({ edit = null }) { Text("Batal") } },
        )
    }
}

@Composable
private fun EditRate(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var value by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { runCatching { repo.gajiRate() }.onSuccess { data = it; value = it.s("rate", "nominal") }.onFailure { error = it.message } }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Rate Gaji", style = MaterialTheme.typography.titleLarge)
        Text("Rate saat ini: ${data?.n("rate", "nominal") ?: 0}")
        NativeTextField(value, { value = it }, "Rate flat", numeric = true)
        Button({ scope.launch { runCatching { repo.updateGajiRate(buildJsonObject { put("rate", value.toLongOrNull() ?: 0L) }) }.onSuccess { data = it }.onFailure { error = it.message } } }) { Text("Simpan rate") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
