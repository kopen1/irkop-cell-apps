package com.irkop.cell.features.pelanggan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.data.Repository
import kotlinx.serialization.json.JsonObject

private fun JsonObject.s(vararg keys: String): String =
    keys.firstNotNullOfOrNull { this[it]?.toString()?.trim('"')?.takeIf(String::isNotBlank) } ?: "-"

private fun JsonObject.n(vararg keys: String): String =
    keys.firstNotNullOfOrNull { this[it]?.toString()?.trim('"') } ?: "0"

private fun JsonObject.objects(key: String): List<JsonObject> =
    (this[key] as? kotlinx.serialization.json.JsonArray)?.filterIsInstance<JsonObject>() ?: emptyList()

@Composable
fun PelangganDetailDialog(
    detail: JsonObject,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detail Pelanggan") },
        text = {
            LazyColumn {
                item {
                    Column(Modifier.padding(bottom = 8.dp)) {
                        Text("Nama: ${detail.s("nama")}")
                        Text("Telepon: ${detail.s("telepon")}")
                        Text("Total belanja: ${detail.n("total_belanja")}")
                        Text("Frekuensi transaksi: ${detail.n("frekuensi_transaksi")}")
                    }
                }
                item { Text("Riwayat transaksi") }
                items(detail.objects("riwayat_transaksi")) { tx ->
                    Text("${tx.s("kode_transaksi", "id")} · ${tx.n("total")}")
                }
                item { Text("Kasbon") }
                items(detail.objects("kasbon")) { bill ->
                    Text("${bill.n("nominal")} · ${bill.s("status")}")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
