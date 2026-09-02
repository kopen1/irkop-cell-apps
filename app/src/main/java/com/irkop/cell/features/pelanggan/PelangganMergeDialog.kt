package com.irkop.cell.features.pelanggan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import com.irkop.cell.features.common.NativeTextField
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun PelangganMergeDialog(
    repo: Repository,
    primaryId: String,
    primaryName: String,
    onMerged: () -> Unit,
    onCancel: () -> Unit
) {
    var duplicateId by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Gabungkan Pelanggan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pelanggan utama: $primaryName (#$primaryId)")
                NativeTextField(duplicateId, { duplicateId = it.filter(Char::isDigit) }, "ID pelanggan yang digabung", numeric = true)
                error?.let { Text(it) }
            }
        },
        confirmButton = {
            Button(enabled = duplicateId.isNotBlank() && duplicateId != primaryId && !busy, onClick = {
                busy = true
                scope.launch {
                    runCatching {
                        repo.mergePelanggan(buildJsonObject {
                            put("id_utama", primaryId.toLong())
                            put("id_gabung", duplicateId.toLong())
                        })
                    }.onSuccess { onMerged() }
                        .onFailure { error = it.message ?: "Gagal menggabungkan pelanggan" }
                    busy = false
                }
            }) { Text(if (busy) "Memproses…" else "Gabungkan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}
