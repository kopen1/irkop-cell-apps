package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.*
import com.irkop.cell.ui.AppViewModel
import com.irkop.cell.ui.ScreenViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

private val Rupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
    maximumFractionDigits = 0
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(applicationContext)
        val repo = Repository(ApiClient(session).api)
        setContent {
            val appVm: AppViewModel = viewModel(factory = SimpleFactory { AppViewModel(session, repo) })
            MaterialTheme {
                IRKOPCell(appVm, repo)
            }
        }
    }
}

@Composable
private fun IRKOPCell(appVm: AppViewModel, repo: Repository) {
    val state by appVm.state.collectAsState()
    when {
        state.loading -> LoadingScreen()
        state.user == null -> LoginScreen(state.error, appVm::login, appVm::clearError)
        else -> MainScaffold(state.user!!, appVm::logout, repo)
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoginScreen(error: String?, login: (String, String) -> Unit, clear: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("IRKOP CELL", style = MaterialTheme.typography.headlineLarge)
        Text("Native Android • POS & Buku Kas", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
        Button(
            enabled = username.isNotBlank() && password.isNotBlank(),
            onClick = { login(username.trim(), password) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("LOGIN") }
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
            LaunchedEffect(error) { clear() }
        }
    }
}

@Composable
private fun MainScaffold(user: UserSession, logout: () -> Unit, repo: Repository) {
    val nav = rememberNavController()
    val destinations = listOf(
        Triple("dashboard", "Dashboard", Icons.Default.Home),
        Triple("kasir", "Kasir", Icons.Default.PointOfSale),
        Triple("transaksi", "Transaksi", Icons.Default.ReceiptLong),
        Triple("lainnya", "Lainnya", Icons.Default.MoreHoriz)
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IRKOP CELL") },
                actions = {
                    Text(user.nama, style = MaterialTheme.typography.labelMedium)
                    IconButton(onClick = logout) { Icon(Icons.Default.Logout, "Logout") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute(nav) == route,
                        onClick = { nav.navigate(route) { launchSingleTop = true } },
                        icon = { Icon(icon, label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "dashboard", Modifier.padding(padding)) {
            composable("dashboard") { DashboardScreen(repo) }
            composable("kasir") { KasirScreen(repo) }
            composable("transaksi") { TransaksiScreen(repo) }
            composable("lainnya") { MoreScreen(user, nav) }
            composable("produk") { ProductScreen(repo) }
            composable("pelanggan") { CustomerScreen(repo) }
            composable("kasbon") { KasbonScreen(repo) }
            composable("pengeluaran") { ExpenseScreen(repo) }
            composable("service") { GenericListScreen("Service HP", repo) { repo.serviceHp() } }
            composable("akun") { GenericListScreen("Akun Uang", repo) { repo.akun() } }
            composable("laporan") { ReportScreen(repo) }
            composable("admin") { AdminScreen(user) }
        }
    }
}

@Composable
private fun currentRoute(nav: NavHostController): String? =
    nav.currentBackStackEntryAsState().value?.destination?.route

@Composable
private fun DashboardScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(factory = SimpleFactory { ScreenViewModel(repo) })
    LaunchedEffect(Unit) { vm.load { repo.kasirCurrent() } }
    val data by vm.data.collectAsState()
    val loading by vm.loading.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        if (loading) CircularProgressIndicator()
        data?.let {
            InfoCard("Status Kasir", it.string("status") ?: "-")
            InfoCard("Tanggal", it.string("tanggal") ?: "-")
            Text("Saldo dan ringkasan finansial tetap berasal dari backend.", style = MaterialTheme.typography.bodySmall)
        }
        vm.error.collectAsState().value?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun KasirScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(factory = SimpleFactory { ScreenViewModel(repo) })
    var opening by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.load { repo.kasirCurrent() } }
    val data by vm.data.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kasir", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        data?.let {
            InfoCard("Status", it.string("status") ?: "-")
            InfoCard("Tanggal", it.string("tanggal") ?: "-")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { amount = ""; opening = true }, modifier = Modifier.fillMaxWidth()) { Text("OPENING") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { amount = ""; closing = true }, modifier = Modifier.fillMaxWidth()) { Text("CLOSING") }
    }
    if (opening) {
        AmountDialog("Saldo Awal Tunai Laci", amount, { amount = it }, {
            vm.create { repo.opening(listOf("Tunai Laci" to (amount.toLongOrNull() ?: 0))) }
            opening = false
        }) { opening = false }
    }
    if (closing) {
        Column {}
        AlertDialog(
            onDismissRequest = { closing = false },
            title = { Text("Closing Kasir") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(amount, { amount = it }, label = { Text("Saldo Real Tunai Laci") })
                    OutlinedTextField(note, { note = it }, label = { Text("Catatan (opsional)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.create { repo.closing(listOf("Tunai Laci" to (amount.toLongOrNull() ?: 0)), note) }
                    closing = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { closing = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun TransaksiScreen(repo: Repository) {
    var create by remember { mutableStateOf(false) }
    val vm: ScreenViewModel = viewModel(factory = SimpleFactory { ScreenViewModel(repo) })
    var q by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.load { repo.transaksi() } }
    val data by vm.data.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Transaksi", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { create = true }) { Text("BARU") }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(q, { q = it }, label = { Text("Cari ID / produk / pelanggan") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { vm.load { repo.transaksi(q.takeIf { it.isNotBlank() }) } }) { Text("CARI") }
        Spacer(Modifier.height(12.dp))
        JsonList(data, "items")
        vm.error.collectAsState().value?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if (create) {
        TransactionDialog(repo, onDone = {
            create = false
            vm.load { repo.transaksi(q.takeIf { it.isNotBlank() }) }
        }, onCancel = { create = false })
    }
}

@Composable
private fun TransactionDialog(repo: Repository, onDone: () -> Unit, onCancel: () -> Unit) {
    var products by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var method by remember { mutableStateOf("tunai") }
    var customerId by remember { mutableStateOf("") }
    var receiverAccount by remember { mutableStateOf("") }
    var manual by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var picker by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching {
            val result = repo.produk()
            products = result.array("items")?.mapNotNull { it as? JsonObject } ?: emptyList()
        }.onFailure { error = it.message }
    }

    val total = cart.sumOf { (it.product.long("harga") ?: 0L) * it.qty }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Transaksi Baru") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                item {
                    Text("Keranjang", style = MaterialTheme.typography.titleMedium)
                    if (cart.isEmpty()) Text("Belum ada item.", style = MaterialTheme.typography.bodySmall)
                }
                items(cart) { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.product.string("nama") ?: "-")
                            Text("${Rupiah.format((item.product.long("harga") ?: 0L) * item.qty)} • qty ${item.qty}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = {
                                cart = cart.mapNotNull { x ->
                                    if (x.product.string("id") == item.product.string("id")) {
                                        val n = x.qty - 1
                                        if (n > 0) x.copy(qty = n) else null
                                    } else x
                                }
                            }) { Icon(Icons.Default.Remove, "Kurangi") }
                            IconButton(onClick = {
                                cart = cart.map { x -> if (x.product.string("id") == item.product.string("id")) x.copy(qty = x.qty + 1) else x }
                            }) { Icon(Icons.Default.Add, "Tambah") }
                        }
                    }
                }
                item {
                    OutlinedButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) { Text("Tambah Produk") }
                    Spacer(Modifier.height(6.dp))
                    Text("Estimasi total: ${Rupiah.format(total)}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(customerId, { customerId = it }, label = { Text("Pelanggan ID (opsional)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text("Metode bayar", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("tunai", "transfer", "bon", "cash_tunai").forEach { m ->
                            FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                        }
                    }
                    if (method == "transfer") {
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(receiverAccount, { receiverAccount = it }, label = { Text("Akun penerima *") }, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(manual, { manual = it })
                        Text("Manual entry / backdate")
                    }
                    if (manual) {
                        OutlinedTextField(date, { date = it }, label = { Text("Tanggal transaksi YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
                        Text("Maksimal 30 hari ke belakang, tidak boleh tanggal masa depan.", style = MaterialTheme.typography.bodySmall)
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        },
        confirmButton = {
            Button(enabled = cart.isNotEmpty() && !busy, onClick = {
                if (method == "transfer" && receiverAccount.isBlank()) {
                    error = "Akun penerima wajib untuk transfer."
                    return@Button
                }
                if (method == "bon" && customerId.isBlank()) {
                    error = "Pelanggan wajib untuk transaksi bon."
                    return@Button
                }
                busy = true
                error = null
                scope.launch {
                    runCatching {
                        repo.createTransaksi(buildJsonObject {
                            putJsonArray("items") {
                                cart.forEach { item ->
                                    add(buildJsonObject {
                                        put("produk_id", item.product.long("id") ?: item.product.string("id") ?: "")
                                        put("qty", item.qty)
                                    })
                                }
                            }
                            put("metode_bayar", method)
                            if (customerId.isNotBlank()) put("pelanggan_id", customerId.toLongOrNull() ?: customerId)
                            if (receiverAccount.isNotBlank()) put("akun_penerima", receiverAccount.trim())
                            put("manual_entry", manual)
                            if (manual) put("tanggal_transaksi", date)
                        })
                    }.onSuccess { onDone() }
                        .onFailure { error = it.message ?: "Transaksi gagal" }
                    busy = false
                }
            }) { Text(if (busy) "Memproses..." else "SIMPAN") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
    if (picker) {
        AlertDialog(
            onDismissRequest = { picker = false },
            title = { Text("Pilih Produk") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(products) { p ->
                        TextButton(onClick = {
                            val id = p.string("id") ?: ""
                            val existing = cart.firstOrNull { it.product.string("id") == id }
                            cart = if (existing == null) cart + CartItem(p, 1)
                            else cart.map { if (it.product.string("id") == id) it.copy(qty = it.qty + 1) else it }
                            picker = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.string("nama") ?: "-")
                                Text(Rupiah.format(p.long("harga") ?: 0L))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { picker = false }) { Text("Tutup") } }
        )
    }
}

private data class CartItem(val product: JsonObject, val qty: Int)

@Composable
private fun ProductScreen(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var categories by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var formOpen by remember { mutableStateOf(false) }
    var categoryOpen by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<JsonObject?>(null) }
    var categoryEdit by remember { mutableStateOf<JsonObject?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() = scope.launch {
        runCatching {
            data = repo.produk()
            categories = repo.kategori().array("items")?.mapNotNull { it as? JsonObject } ?: emptyList()
        }.onFailure { message = it.message }
    }
    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Produk", style = MaterialTheme.typography.headlineMedium)
            Row {
                OutlinedButton(onClick = { categoryOpen = true }) { Text("Kategori") }
                Spacer(Modifier.width(6.dp))
                Button(onClick = { edit = null; formOpen = true }) { Text("Tambah") }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("CRUD produk: kode, nama, kategori, harga jual, harga modal, satuan, stok, stok minimum.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        data?.array("items")?.let { rows ->
            LazyColumn {
                items(rows.filterIsInstance<JsonObject>()) { p ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("${p.string("kode") ?: "-"} • ${p.string("nama") ?: "-"}")
                                Text("Jual ${Rupiah.format(p.long("harga") ?: 0)} • Stok ${p.long("stok") ?: 0}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                TextButton(onClick = { edit = p; formOpen = true }) { Text("Edit") }
                                TextButton(onClick = {
                                    scope.launch { runCatching { repo.deleteProduk(p.string("id") ?: "") }.onSuccess { reload() }.onFailure { message = it.message } }
                                }) { Text("Hapus") }
                            }
                        }
                    }
                }
            }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if (formOpen) ProductFormDialog(repo, edit, categories, { formOpen = false; reload() }, { formOpen = false })
    if (categoryOpen) CategoryDialog(repo, categories, categoryEdit, { categoryOpen = false; categoryEdit = null; reload() }, { categoryOpen = false })
}

@Composable
private fun ProductFormDialog(
    repo: Repository,
    initial: JsonObject?,
    categories: List<JsonObject>,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var code by remember { mutableStateOf(initial?.string("kode") ?: "") }
    var name by remember { mutableStateOf(initial?.string("nama") ?: "") }
    var category by remember { mutableStateOf(initial?.long("kategori_id")?.toString() ?: "") }
    var sale by remember { mutableStateOf(initial?.long("harga")?.toString() ?: "") }
    var cost by remember { mutableStateOf(initial?.long("harga_modal")?.toString() ?: "") }
    var unit by remember { mutableStateOf(initial?.string("satuan") ?: "pcs") }
    var stock by remember { mutableStateOf(initial?.long("stok")?.toString() ?: "0") }
    var minStock by remember { mutableStateOf(initial?.long("stok_minimum")?.toString() ?: "0") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val nonStock = categories.firstOrNull { it.string("id") == category }?.bool("lacak_stok") == false

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (initial == null) "Tambah Produk" else "Edit Produk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                OutlinedTextField(code, { code = it }, label = { Text("Kode *") })
                OutlinedTextField(name, { name = it }, label = { Text("Nama *") })
                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                categories.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = category == (c.string("id") ?: ""), onClick = { category = c.string("id") ?: "" })
                        Text(c.string("nama") ?: "-")
                    }
                }
                OutlinedTextField(sale, { sale = it.filter(Char::isDigit) }, label = { Text("Harga jual *") })
                OutlinedTextField(cost, { cost = it.filter(Char::isDigit) }, label = { Text("Harga modal") })
                OutlinedTextField(unit, { unit = it }, label = { Text("Satuan") })
                if (!nonStock) {
                    OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("Stok") })
                    OutlinedTextField(minStock, { minStock = it.filter(Char::isDigit) }, label = { Text("Stok minimum") })
                } else {
                    Text("Kategori non-stok: field stok tidak dikirim.", style = MaterialTheme.typography.bodySmall)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = !busy, onClick = {
                if (code.isBlank() || name.isBlank() || sale.toLongOrNull() == null || sale.toLong() <= 0) {
                    error = "Kode, nama, dan harga jual wajib valid."
                    return@Button
                }
                busy = true
                scope.launch {
                    runCatching {
                        val body = buildJsonObject {
                            put("kode", code.trim())
                            put("nama", name.trim())
                            put("kategori_id", category.toLongOrNull())
                            put("harga", sale.toLong())
                            put("harga_modal", cost.toLongOrNull())
                            put("satuan", unit.ifBlank { "pcs" })
                            if (nonStock) put("lacak_stok", 0)
                            else {
                                put("stok", stock.toLongOrNull() ?: 0)
                                put("stok_minimum", minStock.toLongOrNull() ?: 0)
                            }
                        }
                        if (initial == null) repo.createProduk(body) else repo.updateProduk(initial.string("id") ?: "", body)
                    }.onSuccess { onDone() }.onFailure { error = it.message }
                    busy = false
                }
            }) { Text(if (busy) "Menyimpan..." else "Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}

@Composable
private fun CategoryDialog(
    repo: Repository,
    categories: List<JsonObject>,
    initial: JsonObject?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.string("nama") ?: "") }
    var track by remember { mutableStateOf(initial?.bool("lacak_stok") != false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var selectedEdit by remember { mutableStateOf(initial) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Kategori") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { c ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${c.string("nama") ?: "-"} • ${if (c.bool("lacak_stok") == true) "stok" else "non-stok"}")
                        TextButton(onClick = {
                            selectedEdit = c
                            name = c.string("nama") ?: ""
                            track = c.bool("lacak_stok") != false
                        }) { Text("Edit") }
                    }
                }
                OutlinedTextField(name, { name = it }, label = { Text("Nama kategori") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(track, { track = it })
                    Text("Lacak stok")
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = !busy, onClick = {
                if (name.isBlank()) { error = "Nama kategori wajib."; return@Button }
                busy = true
                scope.launch {
                    runCatching {
                        val body = buildJsonObject { put("nama", name.trim()); put("lacak_stok", if (track) 1 else 0) }
                        if (selectedEdit == null) repo.createKategori(body)
                        else repo.updateKategori(selectedEdit?.string("id") ?: "", body)
                    }.onSuccess { onDone() }.onFailure { error = it.message }
                    busy = false
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Tutup") } }
    )
}

@Composable
private fun CustomerScreen(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var create by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<JsonObject?>(null) }
    var q by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch { runCatching { data = repo.pelanggan(q.takeIf { it.isNotBlank() }) }.onFailure { error = it.message } }
    LaunchedEffect(Unit) { reload() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pelanggan", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { create = true }) { Text("Tambah") }
        }
        OutlinedTextField(q, { q = it }, label = { Text("Nama / nomor HP") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { reload() }) { Text("Cari") }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items((data?.array("items") ?: JsonArray(emptyList())).filterIsInstance<JsonObject>()) { p ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = {
                    scope.launch { runCatching { detail = repo.pelangganDetail(p.string("id") ?: "") }.onFailure { error = it.message } }
                }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.string("nama") ?: "-")
                        Text(p.string("telepon") ?: "Tanpa nomor", style = MaterialTheme.typography.bodySmall)
                        Text("Belanja ${Rupiah.format(p.long("total_belanja") ?: 0)} • ${p.long("frekuensi_transaksi") ?: 0} transaksi", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if (create) CustomerFormDialog(repo, { create = false; reload() }, { create = false })
    detail?.let { d ->
        AlertDialog(
            onDismissRequest = { detail = null },
            title = { Text("Detail Pelanggan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nama: ${d.string("nama") ?: "-"}")
                    Text("Telepon: ${d.string("telepon") ?: "-"}")
                    Text("Total belanja: ${Rupiah.format(d.long("total_belanja") ?: 0)}")
                    Text("Frekuensi: ${d.long("frekuensi_transaksi") ?: 0}")
                    Text("Riwayat: ${d.array("riwayat")?.size ?: 0} transaksi")
                    Text("Kasbon: ${d.array("kasbon")?.size ?: 0} data")
                }
            },
            confirmButton = { TextButton(onClick = { detail = null }) { Text("Tutup") } }
        )
    }
}

@Composable
private fun CustomerFormDialog(repo: Repository, onDone: () -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Tambah Pelanggan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nama *") })
                OutlinedTextField(phone, { phone = it }, label = { Text("Telepon / nomor") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = !busy, onClick = {
                if (name.isBlank()) { error = "Nama wajib diisi."; return@Button }
                busy = true
                scope.launch {
                    runCatching {
                        repo.createPelanggan(buildJsonObject {
                            put("nama", name.trim())
                            if (phone.isNotBlank()) put("telepon", phone.trim())
                        })
                    }.onSuccess { onDone() }.onFailure { error = it.message }
                    busy = false
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}

@Composable
private fun KasbonScreen(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch { runCatching { data = repo.kasbon() }.onFailure { error = it.message } }
    LaunchedEffect(Unit) { reload() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Kasbon", style = MaterialTheme.typography.headlineMedium)
        Text("Pelunasan menggunakan PUT /kasbon/:id dan default akun Tunai Laci.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items((data?.array("items") ?: JsonArray(emptyList())).filterIsInstance<JsonObject>()) { k ->
                val status = k.string("status") ?: "-"
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(k.string("pelanggan_nama") ?: "Pelanggan #${k.string("pelanggan_id") ?: "-"}")
                            Text(Rupiah.format(k.long("nominal") ?: 0), style = MaterialTheme.typography.titleMedium)
                            Text(status, style = MaterialTheme.typography.bodySmall)
                        }
                        if (status == "belum_lunas") {
                            Button(onClick = {
                                scope.launch {
                                    runCatching {
                                        repo.updateKasbon(k.string("id") ?: "", buildJsonObject {
                                            put("status", "lunas")
                                            put("akun", "Tunai Laci")
                                        })
                                    }.onSuccess { reload() }.onFailure { error = it.message }
                                }
                            }) { Text("LUNAS") }
                        }
                    }
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun ExpenseScreen(repo: Repository) {
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var create by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch { runCatching { data = repo.pengeluaran() }.onFailure { error = it.message } }
    LaunchedEffect(Unit) { reload() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Pengeluaran", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { create = true }) { Text("Tambah") }
        }
        Text("Mutasi finansial diproses backend dengan Idempotency-Key.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items((data?.array("items") ?: JsonArray(emptyList())).filterIsInstance<JsonObject>()) { e ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(e.string("deskripsi") ?: "-")
                        Text(Rupiah.format(e.long("nominal") ?: 0), style = MaterialTheme.typography.titleMedium)
                        Text("${e.string("metode_bayar") ?: "-"} • ${e.string("akun_sumber") ?: "-"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if (create) ExpenseFormDialog(repo, { create = false; reload() }, { create = false })
}

@Composable
private fun ExpenseFormDialog(repo: Repository, onDone: () -> Unit, onCancel: () -> Unit) {
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("tunai") }
    var account by remember { mutableStateOf("Tunai Laci") }
    var category by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Tambah Pengeluaran") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                OutlinedTextField(desc, { desc = it }, label = { Text("Deskripsi *") })
                OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, label = { Text("Nominal *") })
                OutlinedTextField(category, { category = it }, label = { Text("Kategori") })
                Text("Metode bayar", style = MaterialTheme.typography.labelLarge)
                Row {
                    FilterChip(method == "tunai", { method = "tunai" }, label = { Text("Tunai") })
                    Spacer(Modifier.width(6.dp))
                    FilterChip(method == "transfer", { method = "transfer" }, label = { Text("Transfer") })
                }
                OutlinedTextField(account, { account = it }, label = { Text("Akun sumber *") })
                OutlinedTextField(date, { date = it }, label = { Text("Tanggal YYYY-MM-DD (opsional)") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = !busy, onClick = {
                val n = amount.toLongOrNull()
                if (desc.isBlank() || n == null || n <= 0) { error = "Deskripsi dan nominal wajib valid."; return@Button }
                if (account.isBlank()) { error = "Akun sumber wajib."; return@Button }
                busy = true
                scope.launch {
                    runCatching {
                        repo.createPengeluaran(buildJsonObject {
                            put("deskripsi", desc.trim())
                            put("kategori", category.trim())
                            put("nominal", n)
                            put("metode_bayar", method)
                            put("akun_sumber", account.trim())
                            if (date.isNotBlank()) put("tanggal", date.trim())
                        })
                    }.onSuccess { onDone() }.onFailure { error = it.message }
                    busy = false
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}

@Composable
private fun MoreScreen(user: UserSession, nav: NavHostController) {
    val pages = listOf(
        "produk" to "Produk",
        "pelanggan" to "Pelanggan",
        "kasbon" to "Kasbon",
        "pengeluaran" to "Pengeluaran",
        "service" to "Service HP",
        "akun" to "Akun Uang",
        "laporan" to "Laporan",
        "admin" to "Admin"
    )
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Menu", style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)) }
        items(pages.filter { it.first != "admin" || user.role.equals("admin", true) }) { (route, label) ->
            ElevatedButton(onClick = { nav.navigate(route) }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(label)
            }
        }
    }
}

@Composable
private fun GenericListScreen(title: String, repo: Repository, loader: suspend () -> JsonObject) {
    val vm: ScreenViewModel = viewModel(factory = SimpleFactory { ScreenViewModel(repo) })
    LaunchedEffect(Unit) { vm.load(loader) }
    val data by vm.data.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        JsonList(data, "items")
        vm.error.collectAsState().value?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun ReportScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(factory = SimpleFactory { ScreenViewModel(repo) })
    val month = remember { mutableStateOf(LocalDate.now().toString().substring(0, 7)) }
    LaunchedEffect(Unit) { vm.load { repo.laporanBulan(month.value) } }
    val data by vm.data.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Laporan Bulanan", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(month.value, { month.value = it }, label = { Text("Bulan YYYY-MM") })
        Button(onClick = { vm.load { repo.laporanBulan(month.value) } }) { Text("MUAT") }
        Spacer(Modifier.height(12.dp))
        JsonList(data, null)
    }
}

@Composable
private fun AdminScreen(user: UserSession) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Admin", style = MaterialTheme.typography.headlineMedium)
        Text("Role: ${user.role}")
        Text("Permission aktif: ${user.permissions.count { it.value }}")
        Spacer(Modifier.height(12.dp))
        Text("Menu gaji, user, permission, settings, akun dan audit dapat dihubungkan ke endpoint admin yang sudah tersedia di ApiService.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun JsonList(data: JsonObject?, key: String?) {
    val arr = if (key == null) data?.let { JsonArray(listOf(it)) } else data?.array(key)
    if (arr == null || arr.isEmpty()) {
        Text("Tidak ada data.", style = MaterialTheme.typography.bodySmall)
        return
    }
    LazyColumn {
        items(arr) { item ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(item.displayValue(), Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun AmountDialog(
    title: String,
    value: String,
    onValue: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = { OutlinedTextField(value, onValue, label = { Text("Nominal") }, singleLine = true) },
        confirmButton = { Button(onClick = onConfirm) { Text("Simpan") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } }
    )
}

private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <R : androidx.lifecycle.ViewModel> create(modelClass: Class<R>): R =
        creator() as R
}
