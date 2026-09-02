package com.irkop.cell.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
import com.irkop.cell.core.ApiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class AppState(
    val loading: Boolean = true,
    val user: UserSession? = null,
    val error: String? = null
)

class AppViewModel(
    private val session: SessionManager,
    private val repo: Repository
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    init {
        viewModelScope.launch {
            session.load()

            val existingToken = session.token
            val existingUser = session.user

            if (!existingToken.isNullOrBlank() && existingUser != null) {
                // Render session tersimpan terlebih dahulu, lalu validasi
                // JWT ke backend melalui /api/auth/me.
                _state.value = AppState(false, existingUser)

                runCatching {
                    repo.me()
                }.onSuccess { freshUser ->
                    session.save(existingToken, freshUser)
                    _state.value = AppState(false, freshUser)
                }.onFailure { error ->
                    // Token expired/revoked atau /auth/me gagal:
                    // jangan biarkan session lokal tetap dianggap valid.
                    session.clear()
                    _state.value = AppState(
                        false,
                        null,
                        error.message ?: "Sesi login sudah tidak berlaku"
                    )
                }
            } else {
                // Hindari partial/stale session jika hanya token atau user
                // yang tersimpan.
                session.clear()
                _state.value = AppState(false, null)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val cleanUsername = username.trim()

            if (cleanUsername.isBlank() || password.isBlank()) {
                _state.value = AppState(false, null, "Username dan password wajib diisi")
                return@launch
            }

            _state.value = AppState(true)

            runCatching {
                repo.login(cleanUsername, password)
            }.onSuccess { (token, user) ->
                // JWT + user disimpan atomically melalui SessionManager
                // sebelum UI dianggap authenticated.
                session.save(token, user)
                _state.value = AppState(false, user)
            }.onFailure {
                session.clear()
                _state.value = AppState(
                    false,
                    null,
                    it.message ?: "Login gagal"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            // Local logout wajib berhasil walaupun request remote gagal.
            runCatching { repo.logout() }
            session.clear()
            _state.value = AppState(false, null)
        }
    }
}

class ScreenViewModel(private val repo: Repository) : ViewModel() {
    val loading = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    val data = MutableStateFlow<JsonObject?>(null)

    fun load(block: suspend () -> JsonObject) {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { block() }
                .onSuccess { data.value = it }
                .onFailure { error.value = it.message ?: "Gagal memuat data" }
            loading.value = false
        }
    }

    fun create(body: JsonObject, block: suspend (JsonObject) -> JsonObject) {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            runCatching { block(body) }
                .onSuccess { data.value = it }
                .onFailure { error.value = it.message ?: "Operasi gagal" }
            loading.value = false
        }
    }
}
