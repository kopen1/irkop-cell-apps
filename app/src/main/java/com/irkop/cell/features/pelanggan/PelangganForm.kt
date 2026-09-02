package com.irkop.cell.features.pelanggan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun PelangganForm(
    repo: Repository,
    initialId: String? = null,
    initialName: String = "",
    initialPhone: String = "",
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(initialId, initialName) { mutableStateOf(initialName) }
    var phone by remember(initialId, initialPhone) { mutableStateOf(initialPhone) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (initialId == null) "Tambah Pelanggan" else "Edit Pelanggan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NativeTextField(name, { name = it }, "Nama")
                NativeTextField(phone, { phone = it }, "Telepon", numeric = true)
                error?.let { Text(it) }
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank() && !saving, onClick = {
                saving = true
                scope.launch {
                    runCatching {
                        val body = buildJsonObject {
                            put("nama", name.trim())
                            put("telepon", phone.trim())
                        }
                        if (initialId == null) repo.createPelanggan(body)
                        else repo.updatePelanggan(initialId, body)
                    }.onSuccess { onSaved() }
                        .onFailure { error = it.message ?: "Gagal menyimpan" }
                    saving = false
                }
            }) { Text(if (saving) "Menyimpan…" else "Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}
