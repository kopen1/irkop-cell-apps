package com.irkop.cell.util

import android.content.Context
import android.content.Intent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

fun shareReceipt(
    context: Context,
    result: JsonObject,
    total: Long,
    method: String
) {
    val id = result["id"]?.jsonPrimitive?.contentOrNull
        ?: result["transaksi_id"]?.jsonPrimitive?.contentOrNull
        ?: "-"

    val number = result["nomor"]?.jsonPrimitive?.contentOrNull
        ?: result["no_transaksi"]?.jsonPrimitive?.contentOrNull
        ?: id

    val text = buildString {
        appendLine("IRKOP CELL")
        appendLine("------------------------------")
        appendLine("No. Transaksi : $number")
        appendLine("Metode Bayar  : $method")
        appendLine("Total         : Rp$total")
        appendLine("------------------------------")
        appendLine("Terima kasih.")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Struk IRKOP CELL $number")
        putExtra(Intent.EXTRA_TEXT, text)
    }

    context.startActivity(
        Intent.createChooser(intent, "Bagikan struk")
    )
}
