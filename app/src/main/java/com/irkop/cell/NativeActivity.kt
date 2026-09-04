package com.irkop.cell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val AppScheme = darkColorScheme(
    primary = Color(0xFF70D8C8),
    background = Color(0xFF131313),
    surface = Color(0xFF131313),
    onBackground = Color(0xFFE5E2E1),
    onSurface = Color(0xFFE5E2E1),
    error = Color(0xFFFFB4AB)
)

class NativeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = AppScheme) { AutoLoginScreen() } }
    }
}

@Composable
private fun AutoLoginScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember(context) { ApiClient(context) }
    var message by remember { mutableStateOf("Menyiapkan sesi demo…") }
    var loggedIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loginDemo() {
        scope.launch {
            message = "Login demo…"
            try {
                api.login("demo", "demodemo")
                loggedIn = true
                message = "Login demo berhasil"
            } catch (e: Exception) {
                message = e.message ?: "Login demo gagal"
            }
        }
    }

    LaunchedEffect(Unit) { loginDemo() }

    if (loggedIn) {
        DemoHome()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF131313)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge)
            Text("Login otomatis akun demo")
            Text(message)
            Button(onClick = { loginDemo() }, modifier = Modifier.fillMaxWidth()) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun DemoHome() {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF131313)).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Irkop Cell", style = MaterialTheme.typography.headlineLarge)
        Text("SESI KASIR: AKTIF", color = Color(0xFF4EDEA3))
        Text("Akun demo: demo")
        Text("Login otomatis berhasil")
    }
}
