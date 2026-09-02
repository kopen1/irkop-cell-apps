package com.irkop.cell.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.sessionDataStore by preferencesDataStore("session")

class SessionManager(private val context: Context) {
    @Volatile var token: String? = null
        private set

    @Volatile var user: UserSession? = null
        private set

    suspend fun load() {
        val prefs = context.sessionDataStore.data.first()
        token = prefs[TOKEN]
        user = prefs[USER_JSON]?.let { UserSession.fromJson(it) }
    }

    suspend fun save(authToken: String, authUser: UserSession) {
        token = authToken
        user = authUser
        context.sessionDataStore.edit {
            it[TOKEN] = authToken
            it[USER_JSON] = authUser.toJson()
        }
    }

    suspend fun clear() {
        token = null
        user = null
        context.sessionDataStore.edit { it.clear() }
    }

    companion object {
        private val TOKEN = stringPreferencesKey("jwt")
        private val USER_JSON = stringPreferencesKey("user")
    }
}
