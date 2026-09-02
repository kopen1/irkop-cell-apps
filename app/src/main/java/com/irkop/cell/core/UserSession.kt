package com.irkop.cell.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserSession(
    val id: String? = null,
    val nama: String = "",
    val username: String = "",
    val role: String = "",
    val permissions: Map<String, Boolean> = emptyMap()
) {
    fun can(page: String): Boolean {
        if (role.equals("admin", ignoreCase = true)) return true
        return permissions[page] == true
    }

    fun toJson(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(value: String): UserSession =
            runCatching { Json.decodeFromString(serializer(), value) }.getOrDefault(UserSession())
    }
}
