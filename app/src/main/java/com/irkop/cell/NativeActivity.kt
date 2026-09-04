package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DarkColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

private val Bg = Color(0xFF131313)
private val Surface1 = Color(0xFF1B1B1C)
private val Surface2 = Color(0xFF202020)
private val Surface3 = Color(0xFF2A2A2A)
private val TextMain = Color(0xFFE5E2E1)
private val TextMuted = Color(0xFFBCC9C5)
private val Primary = Color(0xFF70D8C8)
private val Success = Color(0xFF4EDEA3)
private val Error = Color(0xFFFFB4AB)
private val OnPrimary = Color(0xFF003731)

private val AppScheme: DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Success,
    background = Bg,
    onBackground = TextMain,
    surface = Bg,
    onSurface = TextMain,
    surfaceVariant = Surface3,
    onSurfaceVariant = TextMuted,
    error = Error
)

private enum class Screen {
    HOME, KASIR, TRANSAKSI, LAPORAN, STOK, SERVICE, KASBON, PELANGGAN,
    PENGELUARAN, GAJI, PENGATURAN, MORE
}

private fun JSONObject.textOf(vararg keys: String): String {
    for (key in keys) {
        if (has(key) && !isNull(key)) return optString(key)
    }
    return ""
}

private fun JSONObject.longOf(vararg keys: String): Long {
    for (key in keys) {
        if (has(key) && !isNull(key)) return optLong(key)
    }
    return 0L
}

private fun JSONArray.asObjects(): List<JSONObject> = buildList {
    for (index in 0 until length()) optJSONObject(index)?.let(::add)
}

private fun rupiah(value: Long): String =
    NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .format(value)
        .replace(",00", "")

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppScheme) {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val api = remember(context) { ApiClient(context) }
    var loggedIn by remember { mutableStateOf(api.hasToken()) }

    if (loggedIn) {
        MainShell(api = api) {
            api.clearToken()
            loggedIn = false
        }
    } else {
        LoginScreen(api = api) { loggedIn = true }
    }
}

@Composable
private fun LoginScreen(api: ApiClient, onSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(Bg).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            shape = RoundedCornerShape(30.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Irkop Cell", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Text("Solusi Kasir, PPOB & Servis Konter Modern", color = TextMuted)
                AssistChip(onClick = {}, label = { Text("FAST SYNC • CLOUD POS") })
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ID Kasir / Username") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kata Sandi / PIN Sesi") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (error.isNotBlank()) Text(error, color = Error)
                Button(
                    onClick = {
                        scope.launch {
                            busy = true
                            error = ""
                            try {
                                api.login(username.trim(), password)
                                onSuccess()
                            } catch (e: Exception) {
                                error = e.message ?: "Login gagal"
                            } finally {
                                busy = false
                            }
                        }
                    },
                    enabled = !busy && username.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp))
                    else Text("Masuk Sesi Kasir", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(api: ApiClient, logout: () -> Unit) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var showTransaction by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Bg) {
                val items = listOf(
                    Triple(Screen.HOME, "Beranda", Icons.Default.Home),
                    Triple(Screen.TRANSAKSI, "Kasir", Icons.Default.PointOfSale),
                    Triple(Screen.LAPORAN, "Laporan", Icons.Default.Assessment),
                    Triple(Screen.MORE, "Lainnya", Icons.Default.MoreHoriz)
                )
                items.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item.first,
                        onClick = { screen = item.first },
                        icon = { Icon(item.third, contentDescription = item.second) },
                        label = { Text(item.second) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (screen == Screen.HOME || screen == Screen.TRANSAKSI) {
                ExtendedFloatingActionButton(
                    onClick = { showTransaction = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Transaksi Baru") },
                    containerColor = Primary,
                    contentColor = OnPrimary
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                Screen.HOME -> HomeScreen(api) { screen = it }
                Screen.KASIR -> CashierScreen(api)
                Screen.TRANSAKSI -> DataScreen(api, "Transaksi", "/api/transaksi") { o ->
                    "${o.textOf("id")} • ${o.textOf("pelanggan_nama").ifBlank { "Umum" }} • ${rupiah(o.longOf("total"))}"
                }
                Screen.LAPORAN -> ReportScreen(api)
                Screen.STOK -> DataScreen(api, "Daftar Barang & Stok", "/api/produk") { o ->
                    "${o.textOf("nama", "nama_produk")} • stok ${o.longOf("stok")} • ${rupiah(o.longOf("harga_jual", "harga"))}"
                }
                Screen.SERVICE -> DataScreen(api, "Service HP", "/api/service-hp") { o ->
                    "${o.textOf("nama_pelanggan", "pelanggan_nama")} • ${o.textOf("status")}"
                }
                Screen.KASBON -> DataScreen(api, "Kasbon", "/api/kasbon") { o ->
                    "${o.textOf("pelanggan_nama", "nama_pelanggan")} • ${rupiah(o.longOf("sisa", "nominal", "jumlah"))}"
                }
                Screen.PELANGGAN -> DataScreen(api, "Pelanggan", "/api/pelanggan") { o ->
                    "${o.textOf("nama")} • ${o.textOf("no_hp", "telepon", "nomor")}"
                }
                Screen.PENGELUARAN -> DataScreen(api, "Pengeluaran", "/api/pengeluaran") { o ->
                    "${o.textOf("deskripsi")} • ${rupiah(o.longOf("nominal"))}"
                }
                Screen.GAJI -> DataScreen(api, "Gaji Karyawan", "/api/gaji") { o ->
                    "${o.textOf("nama_karyawan", "nama")} • ${rupiah(o.longOf("nominal", "jumlah"))}"
                }
                Screen.PENGATURAN -> SettingsScreen()
                Screen.MORE -> MoreScreen(onNavigate = { screen = it }, onLogout = logout)
            }
        }
    }

    if (showTransaction) {
        TransactionDialog(api) { showTransaction = false }
    }
}

@Composable
private fun Header(title: String, subtitle: String? = null) {
    Column(Modifier.padding(16.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        subtitle?.let { Text(it, color = TextMuted, fontSize = 12.sp) }
    }
}

@Composable
private fun HomeScreen(api: ApiClient, navigate: (Screen) -> Unit) {
    var session by remember { mutableStateOf<JSONObject?>(null) }
    LaunchedEffect(Unit) {
        session = runCatching { api.get("/api/kasir/current") }.getOrNull()
    }

    val status = session?.textOf("status")?.uppercase().orEmpty().ifBlank { "MEMUAT" }
    val menu = listOf(
        "Kasir" to Screen.KASIR,
        "Transaksi" to Screen.TRANSAKSI,
        "Laporan" to Screen.LAPORAN,
        "Produk" to Screen.STOK,
        "Service" to Screen.SERVICE,
        "Kasbon" to Screen.KASBON,
        "Pelanggan" to Screen.PELANGGAN,
        "Pengeluaran" to Screen.PENGELUARAN
    )

    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        item { Header("Irkop Cell", "SESI KASIR: $status") }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Selamat datang 👋", color = TextMuted)
                Text("Kelola toko lebih cepat hari ini", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Saldo Sistem", color = TextMuted)
                        val total = session?.optJSONArray("saldo")?.asObjects()
                            ?.sumOf { it.longOf("saldo_sistem", "saldo") } ?: 0L
                        Text(rupiah(total), fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Data finansial berasal dari backend", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Perlu Perhatian Segera", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                AttentionCard("Stok Menipis", "Periksa stok produk")
                Spacer(Modifier.height(8.dp))
                AttentionCard("Kasbon", "Periksa tagihan pelanggan")
                Spacer(Modifier.height(20.dp))
                Text("Menu Kasir Utama", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                menu.chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (label, target) ->
                            Card(
                                modifier = Modifier.weight(1f).clickable { navigate(target) },
                                colors = CardDefaults.cardColors(containerColor = Surface2),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.PointOfSale, contentDescription = null, tint = Primary)
                                    Spacer(Modifier.height(6.dp))
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AttentionCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface2),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(value, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CashierScreen(api: ApiClient) {
    var data by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf("") }
    var dialogOpen by remember { mutableStateOf(false) }
    var opening by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            try {
                data = api.get("/api/kasir/current")
                error = ""
            } catch (e: Exception) {
                error = e.message ?: "Gagal memuat sesi kasir"
            }
        }
    }
    LaunchedEffect(Unit) { reload() }

    val status = data?.textOf("status").orEmpty().ifBlank { "memuat" }
    Column {
        Header("Kasir & Rekonsiliasi", "Sesi harian")
        if (error.isNotBlank()) Text(error, color = Error, modifier = Modifier.padding(16.dp))
        Card(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface1),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", color = TextMuted)
                Text(status.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
                data?.optJSONArray("saldo")?.asObjects()?.forEach {
                    Text("${it.textOf("nama_akun")}: ${rupiah(it.longOf("saldo_sistem", "saldo"))}")
                }
            }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { opening = true; dialogOpen = true },
                enabled = status == "belum_buka"
            ) { Text("Buka Kasir") }
            OutlinedButton(
                onClick = { opening = false; dialogOpen = true },
                enabled = status == "buka"
            ) { Text("Closing") }
        }
    }
    if (dialogOpen) CashDialog(api, opening) { dialogOpen = false; reload() }
}

@Composable
private fun CashDialog(api: ApiClient, isOpening: Boolean, done: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!busy) done() },
        title = { Text(if (isOpening) "Opening Kasir" else "Closing Kasir") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter(Char::isDigit) },
                    label = { Text("Nominal Tunai Laci") },
                    singleLine = true
                )
                if (error.isNotBlank()) Text(error, color = Error)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        busy = true
                        try {
                            val value = amount.toLongOrNull() ?: 0L
                            if (isOpening) {
                                val body = JSONObject().put(
                                    "saldo_awal",
                                    JSONArray().put(
                                        JSONObject().put("nama_akun", "Tunai Laci").put("saldo", value)
                                    )
                                )
                                api.post("/api/kasir/opening", body, financial = true)
                            } else {
                                val body = JSONObject().put(
                                    "saldo_real",
                                    JSONArray().put(
                                        JSONObject().put("nama_akun", "Tunai Laci").put("saldo_real", value)
                                    )
                                )
                                api.post("/api/kasir/closing", body, financial = true)
                            }
                            done()
                        } catch (e: Exception) {
                            error = e.message ?: "Operasi gagal"
                        } finally {
                            busy = false
                        }
                    }
                },
                enabled = !busy
            ) { Text(if (busy) "Menyimpan…" else "Simpan") }
        },
        dismissButton = {
            TextButton(onClick = done, enabled = !busy) { Text("Batal") }
        }
    )
}

@Composable
private fun DataScreen(
    api: ApiClient,
    title: String,
    path: String,
    formatter: (JSONObject) -> String
) {
    var query by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf(emptyList<JSONObject>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            try {
                val response = if (query.isBlank()) api.get(path) else api.get(path, mapOf("q" to query))
                rows = (response.optJSONArray("items")
                    ?: response.optJSONArray("data")
                    ?: response.optJSONArray("results"))?.asObjects().orEmpty()
                error = ""
            } catch (e: Exception) {
                error = e.message ?: "Gagal memuat data"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column {
        Header(title, "Data live dari backend")
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                label = { Text("Cari") },
                singleLine = true
            )
            IconButton(onClick = { load() }) {
                Icon(Icons.Default.Search, contentDescription = "Cari")
            }
        }
        if (error.isNotBlank()) Text(error, color = Error, modifier = Modifier.padding(16.dp))
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rows.isEmpty()) item { Text("Belum ada data", color = TextMuted) }
                items(rows) { row ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface2),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(formatter(row), fontWeight = FontWeight.SemiBold)
                            Text(row.textOf("id", "created_at", "tanggal"), color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportScreen(api: ApiClient) {
    var data by remember { mutableStateOf<JSONObject?>(null) }
    LaunchedEffect(Unit) {
        data = runCatching {
            api.get("/api/laporan/bulan", mapOf("bulan" to java.time.LocalDate.now().toString().substring(0, 7)))
        }.getOrNull()
    }
    Column {
        Header("Laporan Hari Ini", "Ringkasan laporan dari backend")
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { StatRow("Omzet", rupiah(data?.longOf("omzet", "total_omzet") ?: 0L)) }
            item { StatRow("Laba", rupiah(data?.longOf("laba", "total_laba") ?: 0L)) }
            item { StatRow("Pengeluaran", rupiah(data?.longOf("pengeluaran", "total_pengeluaran") ?: 0L)) }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface2),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold)
            Text(value, color = Primary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column {
        Header("Pengaturan", "Permission mengikuti backend")
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { StatRow("Sinkronisasi", "Cloud POS API") }
            item { StatRow("Keamanan", "JWT + permission granular") }
            item { StatRow("Mode UI", "OLED Material 3") }
        }
    }
}

@Composable
private fun MoreScreen(onNavigate: (Screen) -> Unit, onLogout: () -> Unit) {
    Column {
        Header("Lainnya", "Administrasi dan operasional")
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item { MenuRow("Pengaturan", Icons.Default.Assessment) { onNavigate(Screen.PENGATURAN) } }
            item { MenuRow("Gaji Karyawan", Icons.Default.Assessment) { onNavigate(Screen.GAJI) } }
            item { MenuRow("Pelanggan", Icons.Default.Assessment) { onNavigate(Screen.PELANGGAN) } }
            item { MenuRow("Kasbon", Icons.Default.Assessment) { onNavigate(Screen.KASBON) } }
            item { Spacer(Modifier.height(12.dp)) }
            item { Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Keluar Sesi") } }
        }
    }
}

@Composable
private fun MenuRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface2),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Primary)
            Spacer(Modifier.width(12.dp))
            Text(title, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionDialog(api: ApiClient, close: () -> Unit) {
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = close,
        title = { Text("Transaksi Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih produk atau layanan dari data kasir.", color = TextMuted)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan") },
                    singleLine = false
                )
                if (error.isNotBlank()) Text(error, color = Error)
            }
        },
        confirmButton = {
            Button(onClick = {
                error = "Tambahkan item transaksi sebelum menyimpan."
            }) { Text("Lanjut") }
        },
        dismissButton = { TextButton(onClick = close) { Text("Batal") } }
    )
}
