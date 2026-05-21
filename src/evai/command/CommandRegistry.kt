package evai.command

data class CommandResult(val ok: Boolean, val message: String = "", val data: Map<String, Any?>? = null) {
    fun toJson(): String = buildString {
        append("{\"ok\":$ok,")
        if (message.isNotBlank()) append("\"message\":${escape(message)},")
        if (data != null) {
            append("\"data\":{")
            append(data.entries.joinToString(",") { (k, v) -> "\"$k\":${toValue(v)}" })
            append("},")
        }
        append("\"timestamp\":${System.currentTimeMillis()}}")
    }

    private fun escape(s: String) = "\"${s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")}\""

    private fun toValue(v: Any?): String = when (v) {
        null -> "null"
        is String -> escape(v)
        is Number -> v.toString()
        is Boolean -> v.toString()
        is List<*> -> "[${v.joinToString(",") { toValue(it) }}]"
        is Map<*, *> -> "{${v.entries.joinToString(",") { (k, v) -> "\"$k\":${toValue(v)}" }}}"
        else -> escape(v.toString())
    }
}

object CommandRegistry {
    private val handlers = mutableMapOf<String, CommandHandler>()

    fun register() {
        handlers["help"] = BaseCommands()
        handlers["status"] = BaseCommands()
        handlers["whereami"] = BaseCommands()
        handlers["clear"] = BaseCommands()
        handlers["version"] = BaseCommands()
        handlers["list"] = InfoCommands()
        handlers["info"] = InfoCommands()
        handlers["scan"] = ScanCommands()
        handlers["check"] = ScanCommands()
        handlers["map"] = ScanCommands()
        handlers["debug"] = DebugCommands()
        handlers["set"] = SetCommands()
        handlers["nuke"] = SetCommands()
        handlers["build"] = BuildCommands()
        handlers["break"] = BuildCommands()
        handlers["schem"] = SchemCommands()
        handlers["spawn"] = SpawnCommands()
        handlers["control"] = SpawnCommands()
        handlers["kill"] = SpawnCommands()
        handlers["listen"] = EventCommands()
        handlers["fire"] = EventCommands()
        handlers["js"] = JsCommands()
        handlers["logic"] = JsCommands()
        handlers["eval"] = JsCommands()
        handlers["save"] = SaveCommands()
        handlers["load"] = SaveCommands()
        handlers["export"] = ExportCommands()
    }

    fun execute(cmd: String): CommandResult {
        val parts = cmd.trim().split("\\s+".toRegex())
        if (parts.isEmpty()) return CommandResult(false, "empty command")
        val action = parts[0]
        val args = parts.drop(1).toTypedArray()
        val handler = handlers[action] ?: return CommandResult(false, "unknown command: $action")
        return handler.handle(action, args)
    }

    interface CommandHandler {
        fun handle(action: String, args: Array<String>): CommandResult
    }
}