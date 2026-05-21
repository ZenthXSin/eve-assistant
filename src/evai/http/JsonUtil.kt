package evai.http

object JsonUtil {
    fun buildObject(vararg entries: Pair<String, Any?>): String {
        val pairs = entries.map { (k, v) -> "\"$k\":${toJson(v)}" }
        return "{${pairs.joinToString(",")}}"
    }

    fun buildArray(vararg items: Any?): String = "[${items.joinToString(",") { toJson(it) }}]"

    fun toJson(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"${escape(value)}\""
        is Number -> value.toString()
        is Boolean -> value.toString()
        is Map<*, *> -> buildObject(*value.entries.map { (k, v) -> k.toString() to v }.toTypedArray())
        is Collection<*> -> "[${value.joinToString(",") { toJson(it) }}]"
        is Array<*> -> "[${value.joinToString(",") { toJson(it) }}]"
        else -> "\"${escape(value.toString())}\""
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}