package com.irkop.cell.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irkop.cell.core.SessionManager
import com.irkop.cell.core.UserSession
import com.irkop.cell.data.Repository
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
            val existing = session.user
            if (existing != null && session.token != null) {
                _state.value = AppState(false, existing)
                runCatching { repo.me() }.onSuccess {
                    session.save(session.token!!, it)
                    _state.value = AppState(false, it)
                }.onFailure {
                    // Keep the cached session; the user can retry from the app.
                }
            } else {
                _state.value = AppState(false, null)
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = AppState(true)
            runCatching { repo.login(username, password) }
                .onSuccess { (token, user) ->
                    session.save(token, user)
                    _state.value = AppState(false, user)
                }
                .onFailure { _state.value = AppState(false, null, it.message ?: "Login gagal") }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
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
