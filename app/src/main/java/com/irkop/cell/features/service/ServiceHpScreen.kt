package com.irkop.cell.features.service

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private fun JsonObject.s(vararg k: String): String =
    k.firstNotNullOfOrNull {
        this[it]?.jsonPrimitive?.contentOrNull?.takeIf(String::isNotBlank)
    } ?: "-"

private fun JsonObject.n(vararg k: String): Long =
    k.firstNotNullOfOrNull { this[it]?.jsonPrimitive?.longOrNull } ?: 0L

@Composable
fun ServiceHpScreen(repo: Repository) {
    var rows by remember { mutableStateOf(emptyList<JsonObject>()) }
    var selected by remember { mutableStateOf<JsonObject?>(null) }
    var create by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            runCatching { repo.serviceHp() }
                .onSuccess {
                    rows = it["items"]?.let { value ->
                        runCatching { value.jsonArray.filterIsInstance<JsonObject>() }.getOrDefault(emptyList())
                    } ?: emptyList()
                    error = null
                }
                .onFailure { error = it.message ?: "Gagal memuat service HP" }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Text("Service HP", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { create = true }) { Text("Tambah") }
        }

        Button(onClick = ::load, modifier = Modifier.fillMaxWidth()) { Text("Refresh") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(rows) { r ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(r.s("nama_device"), style = MaterialTheme.typography.titleMedium)
                        Text("Pelanggan: ${r.s("pelanggan_nama")}")
                        Text("Status: ${r.s("status")}")
                        Text("Estimasi: ${r.n("estimasi_biaya")} · Biaya: ${r.n("biaya")}")
                        Text(r.s("deskripsi_kerusakan"))
                        Row {
                            TextButton(onClick = { selected = r }) { Text("Edit") }
                            TextButton(onClick = { deleting = r }) { Text("Hapus") }
                        }
                    }
                }
            }
        }
    }

    if (create) {
        ServiceForm(
            repo = repo,
            row = null,
            onSaved = { create = false; load() },
            onCancel = { create = false },
        )
    }

    selected?.let { row ->
        ServiceForm(
            repo = repo,
            row = row,
            onSaved = { selected = null; load() },
            onCancel = { selected = null },
        )
    }

    deleting?.let { row ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Hapus service?") },
            text = {
                Text("${row.s("nama_device")} akan dihapus dari daftar service. Histori tetap dicatat sebagai soft-delete.")
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        runCatching { repo.deleteServiceHp(row.s("id")) }
                            .onSuccess {
                                deleting = null
                                load()
                            }
                            .onFailure {
                                error = it.message ?: "Gagal menghapus service HP"
                                deleting = null
                            }
                    }
                }) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun ServiceForm(
    repo: Repository,
    row: JsonObject?,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    var customer by remember(row) { mutableStateOf(row?.s("pelanggan_id") ?: "") }
    var device by remember(row) { mutableStateOf(row?.s("nama_device") ?: "") }
    var complaint by remember(row) { mutableStateOf(row?.s("deskripsi_kerusakan") ?: "") }
    var estimate by remember(row) { mutableStateOf(row?.s("estimasi_biaya")?.takeUnless { it == "-" } ?: "") }
    var cost by remember(row) { mutableStateOf(row?.s("biaya")?.takeUnless { it == "-" } ?: "") }
    var modal by remember(row) { mutableStateOf(row?.s("harga_modal")?.takeUnless { it == "-" } ?: "") }
    var note by remember(row) { mutableStateOf(row?.s("catatan")?.takeUnless { it == "-" } ?: "") }
    var status by remember(row) { mutableStateOf(row?.s("status")?.takeUnless { it == "-" } ?: "masuk") }
    var date by remember(row) { mutableStateOf(row?.s("tanggal_masuk")?.takeUnless { it == "-" } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (row == null) "Tambah Service HP" else "Edit Service HP") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                NativeTextField(customer, { customer = it }, "Pelanggan ID", numeric = true)
                NativeTextField(device, { device = it }, "Nama device")
                NativeTextField(complaint, { complaint = it }, "Deskripsi kerusakan")
                NativeTextField(estimate, { estimate = it }, "Estimasi biaya", numeric = true)
                NativeTextField(cost, { cost = it }, "Biaya final", numeric = true)
                NativeTextField(modal, { modal = it }, "Harga modal", numeric = true)
                NativeTextField(date, { date = it }, "Tanggal masuk")
                NativeTextField(note, { note = it }, "Catatan")
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("masuk", "proses", "selesai", "diambil").forEach { value ->
                        FilterChip(
                            selected = status == value,
                            onClick = { status = value },
                            label = { Text(value) },
                        )
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                enabled = !busy && device.isNotBlank() && complaint.isNotBlank() && customer.toLongOrNull() != null,
                onClick = {
                    busy = true
                    scope.launch {
                        runCatching {
                            buildJsonObject {
                                put("nama_device", device.trim())
                                put("deskripsi_kerusakan", complaint.trim())
                                put("pelanggan_id", customer.toLong())
                                if (estimate.isNotBlank()) put("estimasi_biaya", estimate.toLong())
                                if (modal.isNotBlank()) put("harga_modal", modal.toLong())
                                if (cost.isNotBlank()) put("biaya", cost.toLong())
                                if (note.isNotBlank()) put("catatan", note.trim())
                                if (date.isNotBlank()) put("tanggal_masuk", date)
                                if (row != null) put("status", status)
                            }
                        }.mapCatching { body ->
                            if (row == null) repo.createServiceHp(body) else repo.updateServiceHp(row.s("id"), body)
                        }.onSuccess {
                            onSaved()
                        }.onFailure {
                            error = it.message ?: "Gagal menyimpan service HP"
                        }
                        busy = false
                    }
                },
            ) { Text(if (busy) "Menyimpan…" else "Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Batal") }
        },
    )
}
