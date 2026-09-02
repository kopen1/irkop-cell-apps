package com.irkop.cell.data

import kotlinx.serialization.json.*

fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
fun JsonObject.long(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull
fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject
fun JsonObject.array(key: String): JsonArray? = this[key] as? JsonArray

fun JsonElement.displayValue(): String = when (this) {
    is JsonObject -> this.toString()
    is JsonArray -> this.toString()
    JsonNull -> "-"
    else -> jsonPrimitive.contentOrNull ?: "-"
}
