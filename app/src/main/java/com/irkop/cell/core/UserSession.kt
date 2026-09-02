package com.irkop.cell.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

        // Backend permission keys normalnya exact-match.
        // Fallback case-insensitive mencegah UI gagal membaca key
        // hanya karena perbedaan kapitalisasi.
        permissions[page]?.let { return it }

        return permissions.entries.firstOrNull {
            it.key.equals(page, ignoreCase = true)
        }?.value == true
    }

    fun toJson(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(value: String): UserSession =
            runCatching { Json.decodeFromString(serializer(), value) }.getOrDefault(UserSession())
    }
}
