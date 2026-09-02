package com.irkop.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irkop.cell.core.ApiError
import com.irkop.cell.data.Repository
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*

@Composable
fun ModernCheckoutDialog(repo: Repository, onDone: () -> Unit, onCancel: () -> Unit) {
    var products by remember { mutableStateOf(emptyList<JsonObject>()) }
    var cart by remember { mutableStateOf(emptyList<CheckoutLine>()) }
    var method by remember { mutableStateOf("tunai") }
    var customerId by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { runCatching { repo.produk() }.onSuccess { products = it.mItems() }.onFailure { error = ApiError.message(it) } }
    val total = cart.sumOf { it.product.mNumber("harga", "harga_jual") * it.qty }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Transaksi Baru") },
        text = {
            LazyColumn(Modifier.heightIn(max = 520.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (cart.isEmpty()) item { Text("Keranjang masih kosong.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(cart) { line ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(line.product.mText("nama"), Modifier.weight(1f))
                        Text("${line.qty} ×")
                        IconButton(onClick = { cart = checkoutDec(cart, line.product.mText("id")) }) { Icon(Icons.Default.Remove, "Kurangi") }
                        IconButton(onClick = { cart = checkoutAdd(cart, line.product) }) { Icon(Icons.Default.Add, "Tambah") }
                    }
                }
                item { Text("Produk", style = MaterialTheme.typography.titleMedium) }
                items(products.take(20)) { product ->
                    Card(onClick = { cart = checkoutAdd(cart, product) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(10.dp)) { Text(product.mText("nama"), Modifier.weight(1f)); Text(mMoney(product.mNumber("harga", "harga_jual"))) }
                    }
                }
                item { OutlinedTextField(customerId, { customerId = it.filter(Char::isDigit) }, label = { Text("Pelanggan ID") }, modifier = Modifier.fillMaxWidth()) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("tunai", "transfer", "bon", "cash_tunai").forEach { value -> FilterChip(selected = method == value, onClick = { method = value }, label = { Text(value.replace('_', ' ').replaceFirstChar { it.uppercase() }) }) } } }
                item { Text("Total ${mMoney(total)}", style = MaterialTheme.typography.titleLarge) }
                error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (method == "bon" && customerId.toLongOrNull() == null) { error = "Pelanggan wajib untuk kasbon."; return@Button }
                busy = true
                scope.launch {
                    runCatching {
                        repo.createTransaksi(buildJsonObject {
                            putJsonArray("items") { cart.forEach { line -> add(buildJsonObject { put("produk_id", line.product["id"] ?: JsonNull); put("qty", line.qty) }) } }
                            put("metode_bayar", method)
                            customerId.toLongOrNull()?.let { put("pelanggan_id", it) }
                        })
                    }.onSuccess { onDone() }.onFailure { error = ApiError.message(it) }
                    busy = false
                }
            }, enabled = cart.isNotEmpty() && total > 0 && !busy) { Text(if (busy) "Memproses…" else "Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}

private data class CheckoutLine(val product: JsonObject, val qty: Int)
private fun checkoutAdd(cart: List<CheckoutLine>, product: JsonObject): List<CheckoutLine> = cart.firstOrNull { it.product.mText("id") == product.mText("id") }?.let { cart.map { line -> if (line.product.mText("id") == product.mText("id")) line.copy(qty = line.qty + 1) else line } } ?: cart + CheckoutLine(product, 1)
private fun checkoutDec(cart: List<CheckoutLine>, id: String): List<CheckoutLine> = cart.mapNotNull { if (it.product.mText("id") != id) it else it.copy(qty = it.qty - 1).takeIf { line -> line.qty > 0 } }
