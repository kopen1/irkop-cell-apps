@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.irkop.cell.core.ApiClient
import com.irkop.cell.core.ApiError
import com.irkop.cell.core.AuthPolicy
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.*
import com.irkop.cell.ui.AppViewModel
import com.irkop.cell.ui.ScreenViewModel
import com.irkop.cell.util.shareReceipt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val Rupiah = NumberFormat
    .getCurrencyInstance(Locale("id", "ID"))
    .apply { maximumFractionDigits = 0 }

private fun money(value: Long?): String =
    Rupiah.format(value ?: 0L)

private fun JsonObject.text(vararg keys: String): String {
    for (key in keys) {
        string(key)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return "-"
}

private fun JsonObject.number(vararg keys: String): Long {
    for (key in keys) {
        long(key)?.let { return it }
    }
    return 0L
}

private fun JsonObject.items(): List<JsonObject> =
    array("items")?.filterIsInstance<JsonObject>() ?: emptyList()

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(applicationContext)
        val repo = Repository(ApiClient(session).api)

        setContent {
            val appVm: AppViewModel = viewModel(
                factory = SimpleFactory {
                    AppViewModel(session, repo)
                }
            )

            MaterialTheme {
                IRKOPCell(appVm, repo)
            }
        }
    }
}

@Composable
private fun IRKOPCell(
    appVm: AppViewModel,
    repo: Repository
) {
    val state by appVm.state.collectAsState()

    when {
        state.loading -> LoadingScreen()

        state.user == null ->
            LoginScreen(
                state.error,
                appVm::login,
                appVm::clearError
            )

        else ->
            MainScaffold(
                state.user!!,
                appVm::logout,
                repo
            )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoginScreen(
    error: String?,
    login: (String, String) -> Unit,
    clear: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "IRKOP CELL",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            "POS & Buku Kas Digital",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(20.dp))

        Button(
            enabled = username.isNotBlank() && password.isNotBlank(),
            onClick = {
                login(username.trim(), password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOGIN")
        }

        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                it,
                color = MaterialTheme.colorScheme.error
            )

            LaunchedEffect(it) {
                clear()
            }
        }
    }
}

@Composable
private fun MainScaffold(
    user: UserSession,
    logout: () -> Unit,
    repo: Repository
) {
    val nav = rememberNavController()

    val allDestinations = listOf(
        Triple(AuthPolicy.DASHBOARD, "Dashboard", Icons.Default.Home),
        Triple(AuthPolicy.TRANSAKSI, "Transaksi", Icons.Default.ReceiptLong),
        Triple(AuthPolicy.KASIR, "Kasir", Icons.Default.PointOfSale),
        Triple(AuthPolicy.LAPORAN, "Laporan", Icons.Default.Assessment)
    )

    val destinations = allDestinations.filter { (route, _, _) ->
        AuthPolicy.canAccess(user, route)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("IRKOP CELL")
                        Text(
                            user.nama.ifBlank { user.username },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = logout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { (route, label, icon) ->
                    NavigationBarItem(
                        selected = currentRoute(nav) == route,
                        onClick = {
                            if (AuthPolicy.canAccess(user, route)) {
                                nav.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            Icon(icon, label)
                        },
                        label = {
                            Text(label)
                        }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = nav,
            startDestination = destinations.firstOrNull()?.first ?: "access_denied",
            modifier = Modifier.padding(padding)
        ) {
            composable("access_denied") {
                AccessDeniedScreen()
            }
            composable("dashboard") {
                if (AuthPolicy.canAccess(user, AuthPolicy.DASHBOARD)) {
                    DashboardScreen(repo)
                } else {
                    AccessDeniedScreen()
                }
            }

            composable("transaksi") {
                if (AuthPolicy.canAccess(user, AuthPolicy.TRANSAKSI)) {
                    TransaksiScreen(repo)
                } else {
                    AccessDeniedScreen()
                }
            }

            composable("kasir") {
                if (AuthPolicy.canAccess(user, AuthPolicy.KASIR)) {
                    KasirScreen(repo)
                } else {
                    AccessDeniedScreen()
                }
            }

            composable("laporan") {
                if (AuthPolicy.canAccess(user, AuthPolicy.LAPORAN)) {
                    ReportScreen(repo)
                } else {
                    AccessDeniedScreen()
                }
            }
        }
    }
}

@Composable
private fun AccessDeniedScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null
            )
            Text(
                "Akses ditolak",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Akun Anda tidak memiliki permission untuk halaman ini.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun currentRoute(
    nav: NavHostController
): String? =
    nav.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

@Composable
private fun DashboardScreen(repo: Repository) {
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toString() }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var kasir by remember { mutableStateOf<JsonObject?>(null) }
    var transaksi by remember { mutableStateOf<JsonObject?>(null) }
    var kasbon by remember { mutableStateOf<JsonObject?>(null) }

    suspend fun loadDashboard() {
        loading = true
        error = null

        runCatching {
            coroutineScope {
                val kasirDeferred = async {
                    repo.kasirCurrent()
                }

                val transaksiDeferred = async {
                    repo.transaksi(
                        tanggal = today
                    )
                }

                val kasbonDeferred = async {
                    repo.kasbon()
                }

                awaitAll(
                    kasirDeferred,
                    transaksiDeferred,
                    kasbonDeferred
                )

                kasir = kasirDeferred.await()
                transaksi = transaksiDeferred.await()
                kasbon = kasbonDeferred.await()
            }
        }.onFailure {
            kasir = null
            transaksi = null
            kasbon = null
            error = it.message ?: "Gagal memuat dashboard"
        }

        loading = false
    }

    LaunchedEffect(today) {
        loadDashboard()
    }

    fun reload() {
        scope.launch {
            loadDashboard()
        }
    }

    fun activeKasbonCount(): Int {
        return kasbon
            ?.items()
            ?.count { item ->
                val status = item.text(
                    "status",
                    "status_pembayaran"
                )

                status.equals("belum_lunas", ignoreCase = true) ||
                    status.equals("belum lunas", ignoreCase = true) ||
                    status.equals("aktif", ignoreCase = true)
            }
            ?: 0
    }

    fun latestTransactions(): List<JsonObject> {
        return transaksi
            ?.items()
            ?.sortedByDescending { item ->
                item.text(
                    "created_at",
                    "tanggal",
                    "updated_at"
                )
            }
            ?.take(10)
            ?: emptyList()
    }

    fun saldoAccounts(): List<JsonObject> {
        val root = kasir ?: return emptyList()

        val saldo = root["saldo"] ?: return emptyList()

        return when (saldo) {
            is JsonArray ->
                saldo.filterIsInstance<JsonObject>()

            is JsonObject -> {
                val nested = saldo.array("items")

                if (nested != null) {
                    nested.filterIsInstance<JsonObject>()
                } else {
                    listOf(saldo)
                }
            }

            else -> emptyList()
        }
    }

    val omzet = transaksi?.number(
        "total_nilai",
        "total_omzet",
        "omzet"
    ) ?: transaksi
        ?.obj("ringkasan")
        ?.number(
            "omzet",
            "total_omzet"
        )
        ?: 0L

    val transactionCount = transaksi?.number(
        "total_items",
        "jumlah_transaksi",
        "transaksi"
    ) ?: transaksi?.items()?.size?.toLong()
        ?: transaksi
            ?.obj("ringkasan")
            ?.number(
                "transaksi",
                "jumlah_transaksi"
            )
        ?: 0L

    val latest = latestTransactions()
    val saldoAccounts = saldoAccounts()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        "Ringkasan operasional hari ini ($today)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(
                    onClick = {
                        reload()
                    }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Muat ulang"
                    )
                }
            }
        }

        if (loading) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        error?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Gagal memuat dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color =
                                MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            message,
                            color =
                                MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = {
                                reload()
                            }
                        ) {
                            Text("Coba lagi")
                        }
                    }
                }
            }
        }

        if (!loading && error == null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Omzet Hari Ini",
                        value = money(omzet)
                    )

                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Transaksi",
                        value = transactionCount.toString()
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Kasbon Aktif",
                        value = activeKasbonCount().toString()
                    )

                    DashboardStatCard(
                        modifier = Modifier.weight(1f),
                        title = "Status Kasir",
                        value = dashboardKasirStatus(
                            kasir?.text("status") ?: ""
                        )
                    )
                }
            }

            item {
                Text(
                    "Saldo per Akun",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                if (saldoAccounts.isEmpty()) {
                    InfoCard(
                        "Saldo",
                        "Belum ada saldo akun yang tersedia."
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            saldoAccounts.forEach { account ->
                                val accountName = account.text(
                                    "nama_akun",
                                    "nama",
                                    "akun"
                                )

                                val balance = account.number(
                                    "saldo_sistem",
                                    "saldo",
                                    "total"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Text(
                                        accountName,
                                        style =
                                            MaterialTheme.typography
                                                .titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        money(balance),
                                        style =
                                            MaterialTheme.typography
                                                .titleMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Transaksi Terbaru",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (latest.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null
                            )

                            Spacer(
                                Modifier.height(8.dp)
                            )

                            Text(
                                "Belum ada transaksi hari ini",
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Transaksi baru akan tampil di sini.",
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                items(latest) { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    transaction.text(
                                        "id",
                                        "kode",
                                        "nomor"
                                    ),
                                    style =
                                        MaterialTheme.typography
                                            .titleMedium
                                )

                                Text(
                                    transaction.text(
                                        "created_at",
                                        "tanggal"
                                    ),
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall
                                )

                                Text(
                                    transaction.text(
                                        "metode_bayar",
                                        "metode"
                                    ),
                                    style =
                                        MaterialTheme.typography
                                            .bodySmall
                                )

                                val confirmation =
                                    transaction.text(
                                        "konfirmasi_pembayaran",
                                        "status_konfirmasi",
                                        "konfirmasi"
                                    )

                                if (confirmation != "-") {
                                    Text(
                                        confirmation,
                                        style =
                                            MaterialTheme.typography
                                                .bodySmall
                                    )
                                }
                            }

                            Text(
                                money(
                                    transaction.number(
                                        "total",
                                        "total_nilai",
                                        "nominal"
                                    )
                                ),
                                style =
                                    MaterialTheme.typography
                                        .titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dashboardKasirStatus(status: String): String =
    when (status.lowercase()) {
        "belum_buka" -> "Belum Buka"
        "buka" -> "Buka"
        "tutup" -> "Tutup"
        else -> status.ifBlank { "-" }
    }

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun KasirScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(
        factory = SimpleFactory {
            ScreenViewModel(repo)
        }
    )

    val scope = rememberCoroutineScope()

    var opening by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.load {
            repo.kasirCurrent()
        }
    }

    val data by vm.data.collectAsState()
    val error by vm.error.collectAsState()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                "Kasir",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            data?.let {
                InfoCard("Status", it.text("status"))
                InfoCard("Tanggal", it.text("tanggal"))
            }
        }

        item {
            Button(
                onClick = {
                    amount = ""
                    opening = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OPENING KASIR")
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    amount = ""
                    note = ""
                    closing = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CLOSING KASIR")
            }
        }

        error?.let {
            item {
                ErrorBox(it)
            }
        }
    }

    if (opening) {
        AmountDialog(
            title = "Saldo Awal Tunai Laci",
            value = amount,
            onValue = {
                amount = it.filter(Char::isDigit)
            },
            onConfirm = {
                val saldo = amount.toLongOrNull()

                if (saldo != null && saldo >= 0) {
                    scope.launch {
                        runCatching {
                            repo.opening(
                                listOf(
                                    "Tunai Laci" to saldo
                                )
                            )
                        }.onSuccess {
                            vm.load {
                                repo.kasirCurrent()
                            }
                        }
                    }
                }

                opening = false
            },
            onCancel = {
                opening = false
            }
        )
    }

    if (closing) {
        AlertDialog(
            onDismissRequest = {
                closing = false
            },
            title = {
                Text("Closing Kasir")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it.filter(Char::isDigit)
                        },
                        label = {
                            Text("Saldo Real Tunai Laci")
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                        },
                        label = {
                            Text("Catatan")
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val saldo = amount.toLongOrNull()

                        if (saldo != null && saldo >= 0) {
                            scope.launch {
                                runCatching {
                                    repo.closing(
                                        listOf(
                                            "Tunai Laci" to saldo
                                        ),
                                        note
                                    )
                                }.onSuccess {
                                    vm.load {
                                        repo.kasirCurrent()
                                    }
                                }
                            }
                        }

                        closing = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        closing = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun TransaksiScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(
        factory = SimpleFactory {
            ScreenViewModel(repo)
        }
    )

    val scope = rememberCoroutineScope()

    var create by remember { mutableStateOf(false) }
    var edit by remember { mutableStateOf<JsonObject?>(null) }
    var detail by remember { mutableStateOf<JsonObject?>(null) }

    var q by remember { mutableStateOf("") }
    var tanggal by remember { mutableStateOf("") }
    var tanggalMulai by remember { mutableStateOf("") }
    var tanggalSelesai by remember { mutableStateOf("") }

    var method by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    fun load() {
        vm.load {
            repo.transaksi(
                q = q.trim().takeIf { it.isNotBlank() },
                tanggal = tanggal.trim().takeIf { it.isNotBlank() },
                tanggalMulai = tanggalMulai.trim().takeIf { it.isNotBlank() },
                tanggalSelesai = tanggalSelesai.trim().takeIf { it.isNotBlank() },
                metodeBayar = method.takeIf { it.isNotBlank() },
                statusKonfirmasi = confirmation.takeIf { it.isNotBlank() }
            )
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    val data by vm.data.collectAsState()
    val error by vm.error.collectAsState()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transaksi",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(
                    onClick = {
                        create = true
                    }
                ) {
                    Text("BARU")
                }
            }
        }

        item {
            OutlinedTextField(
                value = q,
                onValueChange = { q = it },
                label = {
                    Text("Cari transaksi")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = tanggal,
                onValueChange = { tanggal = it },
                label = {
                    Text("Tanggal YYYY-MM-DD")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = tanggalMulai,
                    onValueChange = { tanggalMulai = it },
                    label = {
                        Text("Mulai")
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tanggalSelesai,
                    onValueChange = { tanggalSelesai = it },
                    label = {
                        Text("Selesai")
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Text(
                "Metode pembayaran",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "" to "Semua",
                    "tunai" to "Tunai",
                    "transfer" to "Transfer",
                    "bon" to "Bon",
                    "cash_tunai" to "Cash"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = method == value,
                        onClick = {
                            method = value
                        },
                        label = {
                            Text(label)
                        }
                    )
                }
            }
        }

        item {
            Text(
                "Konfirmasi",
                style = MaterialTheme.typography.labelLarge
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "" to "Semua",
                    "menunggu" to "Menunggu",
                    "dikonfirmasi" to "Dikonfirmasi"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = confirmation == value,
                        onClick = {
                            confirmation = value
                        },
                        label = {
                            Text(label)
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (tanggal.isNotBlank()) {
                        tanggalMulai = ""
                        tanggalSelesai = ""
                    }

                    if (
                        tanggalMulai.isNotBlank() ||
                        tanggalSelesai.isNotBlank()
                    ) {
                        tanggal = ""
                    }

                    load()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TERAPKAN FILTER")
            }
        }

        data?.items()?.let { rows ->
            if (rows.isEmpty()) {
                item {
                    EmptyBox("Tidak ada transaksi.")
                }
            } else {
                items(rows) { row ->
                    TransactionCard(
                        row = row,
                        onDetail = {
                            val id =
                                row.string("id")

                            if (!id.isNullOrBlank()) {
                                scope.launch {
                                    runCatching {
                                        repo.transaksiDetail(id)
                                    }.onSuccess {
                                        detail = it
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        error?.let {
            item {
                ErrorBox(it)
            }
        }
    }

    if (create) {
        TransactionDialog(
            repo = repo,
            onDone = {
                create = false
                load()
            },
            onCancel = {
                create = false
            }
        )
    }

    detail?.let { d ->
        TransactionDetailDialog(
            repo = repo,
            data = d,
            onEdit = {
                edit = d
                detail = null
            },
            onDone = {
                detail = null
                load()
            },
            onCancel = {
                detail = null
            }
        )
    }

    edit?.let { d ->
        TransactionEditDialog(
            repo = repo,
            data = d,
            onDone = {
                edit = null
                load()
            },
            onCancel = {
                edit = null
            }
        )
    }
}

@Composable
private fun TransactionCard(
    row: JsonObject,
    onDetail: () -> Unit
) {
    Card(
        onClick = onDetail,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                row.text(
                    "nomor",
                    "kode",
                    "id"
                ),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                row.text(
                    "pelanggan_nama",
                    "pelanggan",
                    "nama"
                )
            )

            Text(
                money(
                    row.number(
                        "total",
                        "nominal",
                        "grand_total"
                    )
                ),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                row.text(
                    "metode_bayar",
                    "metode"
                )
            )

            Text(
                row.text(
                    "status_konfirmasi",
                    "status"
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TransactionDetailDialog(
    repo: Repository,
    data: JsonObject,
    onEdit: () -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var deleting by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Detail Transaksi")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                item {
                    Text(
                        "ID: ${data.text("id")}"
                    )
                }

                item {
                    Text(
                        "Tanggal: ${data.text("tanggal", "tanggal_transaksi")}"
                    )
                }

                item {
                    Text(
                        "Pelanggan: ${data.text("pelanggan_nama", "pelanggan")}"
                    )
                }

                item {
                    Text(
                        "Metode: ${data.text("metode_bayar", "metode")}"
                    )
                }

                item {
                    Text(
                        "Total: ${
                            money(
                                data.number(
                                    "total",
                                    "nominal",
                                    "grand_total"
                                )
                            )
                        }"
                    )
                }

                item {
                    data.array("items")?.let {
                        Text(
                            "Item: ${it.size}"
                        )
                    }
                }

                if (deleting) {
                    item {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = {
                                reason = it
                            },
                            label = {
                                Text("Alasan hapus")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!deleting) {
                Row {
                    TextButton(
                        onClick = onEdit
                    ) {
                        Text("Edit")
                    }

                    TextButton(
                        onClick = {
                            deleting = true
                        }
                    ) {
                        Text("Hapus")
                    }

                    TextButton(
                        onClick = {
                            val total = data.number(
                                "total",
                                "nominal",
                                "grand_total"
                            )

                            shareReceipt(
                                context,
                                data,
                                total,
                                data.text("metode_bayar", "metode")
                            )
                        }
                    ) {
                        Text("Bagikan")
                    }
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            runCatching {
                                repo.deleteTransaksi(
                                    data.text("id"),
                                    reason.takeIf {
                                        it.isNotBlank()
                                    }
                                )
                            }.onSuccess {
                                onDone()
                            }
                        }
                    }
                ) {
                    Text("KONFIRMASI HAPUS")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (deleting) {
                        deleting = false
                    } else {
                        onCancel()
                    }
                }
            ) {
                Text(
                    if (deleting) "Batal" else "Tutup"
                )
            }
        }
    )
}


@Composable
private fun TransactionEditDialog(
    repo: Repository,
    data: JsonObject,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var products by remember {
        mutableStateOf<List<JsonObject>>(emptyList())
    }

    var cart by remember {
        mutableStateOf<List<CartItem>>(emptyList())
    }

    var method by remember {
        mutableStateOf(
            data.text(
                "metode_bayar",
                "metode"
            )
        )
    }

    var customerId by remember {
        mutableStateOf(
            data.text(
                "pelanggan_id",
                "customer_id"
            ).takeIf { it != "-" } ?: ""
        )
    }

    var receiver by remember {
        mutableStateOf(
            data.text(
                "akun_penerima",
                "akun_penerima_nama"
            ).takeIf { it != "-" } ?: ""
        )
    }

    var busy by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var picker by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        runCatching {
            repo.produk()
        }.onSuccess { response ->
            products = response.items()

            val existing = data.array("items")
                ?.filterIsInstance<JsonObject>()
                ?: emptyList()

            cart = existing.mapNotNull { item ->
                val productId = item.string("produk_id")
                    ?: item.string("id")
                    ?: return@mapNotNull null

                val product = products.firstOrNull {
                    it.text("id") == productId
                } ?: buildJsonObject {
                    put("id", productId)
                    put(
                        "nama",
                        item.text(
                            "nama_produk_snapshot",
                            "nama_produk",
                            "nama"
                        )
                    )
                    put(
                        "harga",
                        item.number(
                            "harga_snapshot",
                            "harga",
                            "harga_jual"
                        )
                    )
                }

                CartItem(
                    product = product,
                    qty = item.number(
                        "qty"
                    ).toInt().coerceAtLeast(1)
                )
            }
        }.onFailure {
            error = ApiError.message(it)
        }
    }

    val total = cart.sumOf {
        it.product.number("harga") * it.qty
    }

    AlertDialog(
        onDismissRequest = {
            if (!busy) onCancel()
        },
        title = {
            Text("Edit Transaksi")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 560.dp)
            ) {
                item {
                    Text(
                        "ID: ${data.text("id")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item {
                    Text(
                        "Keranjang",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (cart.isEmpty()) {
                    item {
                        EmptyBox("Tidak ada item transaksi.")
                    }
                }

                items(cart) { item ->
                    Card {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    item.product.text(
                                        "nama"
                                    )
                                )

                                Text(
                                    "${money(item.product.number("harga"))} × ${item.qty}"
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        cart = cart.mapNotNull { current ->
                                            if (
                                                current.product.text("id") ==
                                                item.product.text("id")
                                            ) {
                                                val next = current.qty - 1

                                                if (next > 0) {
                                                    current.copy(
                                                        qty = next
                                                    )
                                                } else {
                                                    null
                                                }
                                            } else {
                                                current
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Kurangi"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        cart = cart.map { current ->
                                            if (
                                                current.product.text("id") ==
                                                item.product.text("id")
                                            ) {
                                                current.copy(
                                                    qty = current.qty + 1
                                                )
                                            } else {
                                                current
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Tambah"
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            picker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TAMBAH / GANTI PRODUK")
                    }
                }

                item {
                    InfoCard(
                        "Total",
                        money(total)
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerId,
                        onValueChange = {
                            customerId =
                                it.filter(Char::isDigit)
                        },
                        label = {
                            Text("Pelanggan ID")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        "Metode pembayaran",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "" to "Tidak diubah",
                            "tunai" to "Tunai",
                            "transfer" to "Transfer",
                            "bon" to "Bon",
                            "cash_tunai" to "Cash"
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = method == value,
                                onClick = {
                                    method = value
                                },
                                label = {
                                    Text(label)
                                }
                            )
                        }
                    }
                }

                if (method == "transfer") {
                    item {
                        OutlinedTextField(
                            value = receiver,
                            onValueChange = {
                                receiver = it
                            },
                            label = {
                                Text("Akun penerima")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                error?.let { message ->
                    item {
                        ErrorBox(message)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !busy && cart.isNotEmpty(),
                onClick = {
                    if (total <= 0) {
                        error =
                            "Total transaksi harus lebih dari 0."
                        return@Button
                    }

                    if (
                        method == "transfer" &&
                        receiver.isBlank()
                    ) {
                        error =
                            "Akun penerima wajib."
                        return@Button
                    }

                    if (
                        method == "bon" &&
                        customerId.toLongOrNull() == null
                    ) {
                        error =
                            "Pelanggan wajib untuk bon."
                        return@Button
                    }

                    val id = data.text("id")

                    if (id == "-") {
                        error =
                            "ID transaksi tidak ditemukan."
                        return@Button
                    }

                    busy = true

                    scope.launch {
                        runCatching {
                            repo.updateTransaksi(
                                id,
                                buildJsonObject {
                                    putJsonArray("items") {
                                        cart.forEach { item ->
                                            add(
                                                buildJsonObject {
                                                    put(
                                                        "produk_id",
                                                        item.product["id"]
                                                            ?: JsonNull
                                                    )
                                                    put(
                                                        "qty",
                                                        item.qty
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    if (method.isNotBlank()) {
                                        put(
                                            "metode_bayar",
                                            method
                                        )
                                    }

                                    customerId
                                        .toLongOrNull()
                                        ?.let {
                                            put(
                                                "pelanggan_id",
                                                it
                                            )
                                        }

                                    if (
                                        method == "transfer" &&
                                        receiver.isNotBlank()
                                    ) {
                                        put(
                                            "akun_penerima",
                                            receiver.trim()
                                        )
                                    }
                                }
                            )
                        }.onSuccess {
                            onDone()
                        }.onFailure {
                            error = ApiError.message(it)
                        }

                        busy = false
                    }
                }
            ) {
                Text(
                    if (busy) {
                        "Memproses..."
                    } else {
                        "SIMPAN PERUBAHAN"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !busy,
                onClick = onCancel
            ) {
                Text("Batal")
            }
        }
    )

    if (picker) {
        AlertDialog(
            onDismissRequest = {
                picker = false
            },
            title = {
                Text("Pilih Produk")
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(
                        max = 420.dp
                    )
                ) {
                    items(products) { product ->
                        TextButton(
                            onClick = {
                                val id =
                                    product.text("id")

                                val existing =
                                    cart.firstOrNull {
                                        it.product.text("id") == id
                                    }

                                cart =
                                    if (existing == null) {
                                        cart + CartItem(
                                            product,
                                            1
                                        )
                                    } else {
                                        cart.map {
                                            if (
                                                it.product.text("id") == id
                                            ) {
                                                it.copy(
                                                    qty = it.qty + 1
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    }

                                picker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
                                    product.text("nama")
                                )

                                Text(
                                    money(
                                        product.number("harga")
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker = false
                    }
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun TransactionDialog(
    repo: Repository,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    var products by remember {
        mutableStateOf<List<JsonObject>>(emptyList())
    }

    var cart by remember {
        mutableStateOf<List<CartItem>>(emptyList())
    }

    var method by remember {
        mutableStateOf("tunai")
    }

    var customerId by remember {
        mutableStateOf("")
    }

    var receiver by remember {
        mutableStateOf("")
    }

    var manual by remember {
        mutableStateOf(false)
    }

    var date by remember {
        mutableStateOf(LocalDate.now().toString())
    }

    var picker by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var busy by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching {
            repo.produk()
        }.onSuccess {
            products = it.items()
        }.onFailure {
            error = ApiError.message(it)
        }
    }

    val total = cart.sumOf {
        it.product.number("harga") * it.qty
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Transaksi Baru")
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 560.dp)
            ) {
                item {
                    Text(
                        "Keranjang",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (cart.isEmpty()) {
                    item {
                        EmptyBox("Belum ada produk.")
                    }
                }

                items(cart) { item ->
                    Card {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text(
                                    item.product.text("nama")
                                )

                                Text(
                                    "${money(item.product.number("harga"))} × ${item.qty}"
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        cart = cart.mapNotNull { x ->
                                            if (
                                                x.product.text("id") ==
                                                item.product.text("id")
                                            ) {
                                                val next =
                                                    x.qty - 1

                                                if (next > 0) {
                                                    x.copy(qty = next)
                                                } else {
                                                    null
                                                }
                                            } else {
                                                x
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        "Kurangi"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        cart = cart.map { x ->
                                            if (
                                                x.product.text("id") ==
                                                item.product.text("id")
                                            ) {
                                                x.copy(
                                                    qty = x.qty + 1
                                                )
                                            } else {
                                                x
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        "Tambah"
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = {
                            picker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("TAMBAH PRODUK")
                    }
                }

                item {
                    InfoCard(
                        "Total",
                        money(total)
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerId,
                        onValueChange = {
                            customerId = it.filter(Char::isDigit)
                        },
                        label = {
                            Text("Pelanggan ID")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        "Metode pembayaran",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "tunai",
                            "transfer",
                            "bon",
                            "cash_tunai"
                        ).forEach { value ->
                            FilterChip(
                                selected = method == value,
                                onClick = {
                                    method = value
                                },
                                label = {
                                    Text(value)
                                }
                            )
                        }
                    }
                }

                if (method == "transfer") {
                    item {
                        OutlinedTextField(
                            value = receiver,
                            onValueChange = {
                                receiver = it
                            },
                            label = {
                                Text("Akun penerima *")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = manual,
                            onCheckedChange = {
                                manual = it
                            }
                        )

                        Text("Manual entry / backdate")
                    }
                }

                if (manual) {
                    item {
                        OutlinedTextField(
                            value = date,
                            onValueChange = {
                                date = it
                            },
                            label = {
                                Text("Tanggal YYYY-MM-DD")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Maksimal 30 hari ke belakang.",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                error?.let {
                    item {
                        ErrorBox(it)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled =
                    cart.isNotEmpty() &&
                    !busy,
                onClick = {
                    if (total <= 0) {
                        error =
                            "Total transaksi harus lebih dari 0."
                        return@Button
                    }

                    if (
                        method == "transfer" &&
                        receiver.isBlank()
                    ) {
                        error =
                            "Akun penerima wajib."
                        return@Button
                    }

                    if (
                        method == "bon" &&
                        customerId.toLongOrNull() == null
                    ) {
                        error =
                            "Pelanggan wajib untuk bon."
                        return@Button
                    }

                    if (manual) {
                        val parsed =
                            runCatching {
                                LocalDate.parse(date)
                            }.getOrNull()

                        if (parsed == null) {
                            error =
                                "Tanggal tidak valid."
                            return@Button
                        }

                        if (
                            parsed.isAfter(
                                LocalDate.now()
                            )
                        ) {
                            error =
                                "Tanggal tidak boleh masa depan."
                            return@Button
                        }

                        if (
                            parsed.isBefore(
                                LocalDate.now()
                                    .minusDays(30)
                            )
                        ) {
                            error =
                                "Maksimal 30 hari ke belakang."
                            return@Button
                        }
                    }

                    busy = true

                    scope.launch {
                        runCatching {
                            repo.createTransaksi(
                                buildJsonObject {
                                    putJsonArray("items") {
                                        cart.forEach { item ->
                                            add(
                                                buildJsonObject {
                                                    put(
                                                        "produk_id",
                                                        item.product["id"]
                                                            ?: JsonNull
                                                    )
                                                    put(
                                                        "qty",
                                                        item.qty
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    put(
                                        "metode_bayar",
                                        method
                                    )

                                    customerId
                                        .toLongOrNull()
                                        ?.let {
                                            put(
                                                "pelanggan_id",
                                                it
                                            )
                                        }

                                    if (
                                        receiver.isNotBlank()
                                    ) {
                                        put(
                                            "akun_penerima",
                                            receiver.trim()
                                        )
                                    }

                                    put(
                                        "manual_entry",
                                        manual
                                    )

                                    if (manual) {
                                        put(
                                            "tanggal_transaksi",
                                            date
                                        )
                                    }
                                }
                            )
                        }.onSuccess {
                            onDone()
                        }.onFailure {
                            error =
                                ApiError.message(it)
                        }

                        busy = false
                    }
                }
            ) {
                Text(
                    if (busy)
                        "Memproses..."
                    else
                        "SIMPAN"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Batal")
            }
        }
    )

    if (picker) {
        AlertDialog(
            onDismissRequest = {
                picker = false
            },
            title = {
                Text("Pilih Produk")
            },
            text = {
                LazyColumn(
                    modifier =
                        Modifier.heightIn(
                            max = 420.dp
                        )
                ) {
                    items(products) { product ->
                        TextButton(
                            onClick = {
                                val id =
                                    product.text("id")

                                val existing =
                                    cart.firstOrNull {
                                        it.product.text("id") ==
                                            id
                                    }

                                cart =
                                    if (existing == null) {
                                        cart +
                                            CartItem(
                                                product,
                                                1
                                            )
                                    } else {
                                        cart.map {
                                            if (
                                                it.product.text("id") ==
                                                    id
                                            ) {
                                                it.copy(
                                                    qty =
                                                        it.qty + 1
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    }

                                picker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(product.text("nama"))
                                Text(
                                    money(
                                        product.number(
                                            "harga"
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker = false
                    }
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

private data class CartItem(
    val product: JsonObject,
    val qty: Int
)

@Composable
private fun ReportScreen(repo: Repository) {
    val vm: ScreenViewModel = viewModel(
        factory = SimpleFactory {
            ScreenViewModel(repo)
        }
    )

    var mode by remember {
        mutableStateOf("bulan")
    }

    var month by remember {
        mutableStateOf(
            LocalDate.now()
                .toString()
                .substring(0, 7)
        )
    }

    var year by remember {
        mutableStateOf(
            LocalDate.now()
                .year
                .toString()
        )
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        vm.load {
            repo.laporanBulan(month)
        }
    }

    val data by vm.data.collectAsState()
    val error by vm.error.collectAsState()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Laporan",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = mode == "bulan",
                    onClick = {
                        mode = "bulan"
                    },
                    label = {
                        Text("Bulanan")
                    }
                )

                FilterChip(
                    selected = mode == "tahun",
                    onClick = {
                        mode = "tahun"
                    },
                    label = {
                        Text("Tahunan")
                    }
                )
            }
        }

        if (mode == "bulan") {
            item {
                OutlinedTextField(
                    value = month,
                    onValueChange = {
                        month = it
                    },
                    label = {
                        Text("Bulan YYYY-MM")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            if (
                                Regex(
                                    """\d{4}-\d{2}"""
                                ).matches(month)
                            ) {
                                vm.load {
                                    repo.laporanBulan(
                                        month
                                    )
                                }
                            }
                        }
                    ) {
                        Text("MUAT")
                    }

                    OutlinedButton(
                        onClick = {
                            vm.load {
                                repo.laporanBulan(
                                    month
                                )
                            }
                        }
                    ) {
                        Text("REFRESH")
                    }
                }
            }
        } else {
            item {
                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        year =
                            it.filter(Char::isDigit)
                                .take(4)
                    },
                    label = {
                        Text("Tahun YYYY")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(
                    onClick = {
                        year.toIntOrNull()?.let {
                            vm.load {
                                repo.laporanTahun(it)
                            }
                        }
                    }
                ) {
                    Text("MUAT TAHUN")
                }
            }
        }

        item {
            data?.let {
                ReportSummary(it)
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    // Export tersedia di Repository.
                    // File download/Android share akan
                    // disambungkan pada tahap export file.
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("EXPORT CSV")
            }
        }

        error?.let {
            item {
                ErrorBox(it)
            }
        }
    }
}

@Composable
private fun ReportSummary(
    data: JsonObject
) {
    val summary =
        data.obj("summary")
            ?: data.obj("ringkasan")
            ?: data

    InfoCard(
        "Omzet",
        money(
            summary.number(
                "omzet",
                "total_omzet"
            )
        )
    )

    InfoCard(
        "Laba",
        money(
            summary.number(
                "laba",
                "total_laba"
            )
        )
    )

    InfoCard(
        "Pengeluaran",
        money(
            summary.number(
                "pengeluaran",
                "total_pengeluaran"
            )
        )
    )

    InfoCard(
        "Kasbon",
        money(
            summary.number(
                "kasbon",
                "kasbon_aktif"
            )
        )
    )

    InfoCard(
        "Net",
        money(
            summary.number(
                "net",
                "laba_bersih"
            )
        )
    )

    data.array("kategori")?.let {
        SectionCard(
            "Rekap Kategori",
            it.filterIsInstance<JsonObject>()
        )
    }

    data.array("best_selling")?.let {
        SectionCard(
            "Kategori Terlaris",
            it.filterIsInstance<JsonObject>()
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    rows: List<JsonObject>
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement =
                Arrangement.spacedBy(5.dp)
        ) {
            Text(
                title,
                style =
                    MaterialTheme.typography.titleMedium
            )

            if (rows.isEmpty()) {
                Text("Tidak ada data.")
            } else {
                rows.take(10).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            row.text(
                                "nama",
                                "kategori",
                                "nama_kategori"
                            ),
                            Modifier.weight(1f)
                        )

                        Text(
                            money(
                                row.number(
                                    "omzet",
                                    "total",
                                    "nominal"
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(14.dp)
        ) {
            Text(
                label,
                style =
                    MaterialTheme.typography.labelMedium
            )

            Text(
                value,
                style =
                    MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun ErrorBox(
    message: String
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            Modifier.padding(14.dp),
            color =
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyBox(
    message: String
) {
    Card(
        Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            Modifier.padding(14.dp)
        )
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
        title = {
            Text(title)
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                label = {
                    Text("Nominal")
                },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Batal")
            }
        }
    )
}

private class SimpleFactory<T : androidx.lifecycle.ViewModel>(
    private val creator: () -> T
) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <R : androidx.lifecycle.ViewModel> create(
        modelClass: Class<R>
    ): R {
        @Suppress("UNCHECKED_CAST")
        return creator() as R
    }
}
