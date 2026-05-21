package evai.command

import mindustry.*
import evai.command.CommandRegistry.CommandHandler

class JsCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "js" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: js <code>")
            val code = args.joinToString(" ")
            try {
                val result = Vars.mods.getScripts().runConsole(code)
                CommandResult(true, data = mapOf("code" to code, "result" to (result?.toString() ?: "undefined")))
            } catch (e: Exception) {
                CommandResult(false, "JS error: ${e.message}")
            }
        }
        "logic" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: logic <code>")
            val code = args.joinToString(" ")
            try {
                val result = mindustry.logic.LExecutor.runLogicScript(code)
                CommandResult(true, data = mapOf("code" to code, "result" to result.toString()))
            } catch (e: Exception) {
                CommandResult(false, "Logic error: ${e.message}")
            }
        }
        "eval" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: eval <expression>")
            val expr = args.joinToString(" ")
            try {
                val result = Vars.mods.getScripts().runConsole("print($expr);")
                CommandResult(true, data = mapOf("expression" to expr, "result" to (result?.toString() ?: "undefined")))
            } catch (e: Exception) {
                CommandResult(false, "Eval error: ${e.message}")
            }
        }
        else -> CommandResult(false, "unknown js command: $action")
    }
}