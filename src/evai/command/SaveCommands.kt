package evai.command

import mindustry.*
import evai.command.CommandRegistry.CommandHandler

class SaveCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "save" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: save <name>")
            val name = args[0]
            try {
                Vars.saves.addSave(name)
                CommandResult(true, "Saved as '$name'")
            } catch (e: Exception) {
                CommandResult(false, "Save failed: ${e.message}")
            }
        }
        "load" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: load <name>")
            val name = args[0]
            val slot = Vars.saves.getSaveSlots().find { it.name == name }
                ?: return CommandResult(false, "save not found: $name")
            try {
                slot.load()
                CommandResult(true, "Loaded '$name'")
            } catch (e: Exception) {
                CommandResult(false, "Load failed: ${e.message}")
            }
        }
        "save" + " list" -> {
            val saves = Vars.saves.getSaveSlots().map { mapOf("name" to it.name, "date" to it.date.toString()) }
            CommandResult(true, data = mapOf("saves" to saves))
        }
        "save" + " delete" -> {
            if (args.size < 1) return CommandResult(false, "usage: save delete <name>")
            val name = args[0]
            val slot = Vars.saves.getSaveSlots().find { it.name == name }
                ?: return CommandResult(false, "save not found: $name")
            slot.delete()
            CommandResult(true, "Deleted save '$name'")
        }
        "save" + " current" -> {
            val current = Vars.saves.getCurrent()
            CommandResult(true, data = mapOf("current" to (current?.name ?: "none")))
        }
        else -> {
            // Handle "save list" "save delete" "save current" by checking action
            when {
                action == "save" && args.isNotEmpty() && args[0] == "list" -> {
                    val saves = Vars.saves.getSaveSlots().map { mapOf("name" to it.name, "date" to it.date.toString()) }
                    CommandResult(true, data = mapOf("saves" to saves))
                }
                action == "save" && args.size >= 2 && args[0] == "delete" -> {
                    val name = args[1]
                    val slot = Vars.saves.getSaveSlots().find { it.name == name }
                        ?: return CommandResult(false, "save not found: $name")
                    slot.delete()
                    CommandResult(true, "Deleted save '$name'")
                }
                action == "save" && args.size >= 1 && args[0] == "current" -> {
                    val current = Vars.saves.getCurrent()
                    CommandResult(true, data = mapOf("current" to (current?.name ?: "none")))
                }
                else -> CommandResult(false, "unknown save subcommand")
            }
        }
    }
}