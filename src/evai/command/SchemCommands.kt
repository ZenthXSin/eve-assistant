package evai.command

import mindustry.*
import mindustry.game.*
import evai.command.CommandRegistry.CommandHandler

class SchemCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult {
        if (args.isEmpty()) return CommandResult(false, "usage: schem <create/place/list/delete/rename/export/import> ...")
        val sub = args[0].lowercase()
        return when (sub) {
            "create" -> {
                if (args.size < 6) return CommandResult(false, "usage: schem create <name> <x1> <y1> <x2> <y2>")
                val name = args[1]
                val x1 = args[2].toIntOrNull() ?: return CommandResult(false, "invalid x1")
                val y1 = args[3].toIntOrNull() ?: return CommandResult(false, "invalid y1")
                val x2 = args[4].toIntOrNull() ?: return CommandResult(false, "invalid x2")
                val y2 = args[5].toIntOrNull() ?: return CommandResult(false, "invalid y2")
                val minX = minOf(x1, x2)
                val maxX = maxOf(x1, x2)
                val minY = minOf(y1, y2)
                val maxY = maxOf(y1, y2)
                val schem = Schematics.create(minX, minY, maxX, maxY)
                schem.tags["name"] = name
                Vars.schematics.add(schem)
                CommandResult(true, "Created schematic '$name' from ($minX,$minY) to ($maxX,$maxY)")
            }
            "place" -> {
                if (args.size < 4) return CommandResult(false, "usage: schem place <name> <x> <y> [rotate]")
                val name = args[1]
                val x = args[2].toIntOrNull() ?: return CommandResult(false, "invalid x")
                val y = args[3].toIntOrNull() ?: return CommandResult(false, "invalid y")
                val rotate = args.getOrNull(4)?.toIntOrNull() ?: 0
                val schem = Vars.schematics.all().find { it.tags["name"] == name }
                    ?: return CommandResult(false, "schematic not found: $name")
                val team = Vars.player?.team() ?: Team.sharded
                Schematics.place(schem, x, y, team, true)
                CommandResult(true, "Placed schematic '$name' at ($x, $y)")
            }
            "list" -> {
                val list = Vars.schematics.all().map { mapOf("name" to (it.tags["name"] ?: "unnamed"), "width" to it.width, "height" to it.height) }
                CommandResult(true, data = mapOf("schematics" to list))
            }
            "delete" -> {
                if (args.size < 2) return CommandResult(false, "usage: schem delete <name>")
                val name = args[1]
                val schem = Vars.schematics.all().find { it.tags["name"] == name }
                    ?: return CommandResult(false, "schematic not found: $name")
                Vars.schematics.remove(schem)
                CommandResult(true, "Deleted schematic '$name'")
            }
            "rename" -> {
                if (args.size < 3) return CommandResult(false, "usage: schem rename <old> <new>")
                val oldName = args[1]
                val newName = args[2]
                val schem = Vars.schematics.all().find { it.tags["name"] == oldName }
                    ?: return CommandResult(false, "schematic not found: $oldName")
                schem.tags["name"] = newName
                CommandResult(true, "Renamed '$oldName' to '$newName'")
            }
            "export" -> {
                if (args.size < 2) return CommandResult(false, "usage: schem export <name>")
                val name = args[1]
                val schem = Vars.schematics.all().find { it.tags["name"] == name }
                    ?: return CommandResult(false, "schematic not found: $name")
                val base64 = Schematics.writeBase64(schem)
                CommandResult(true, data = mapOf("name" to name, "base64" to base64))
            }
            "import" -> {
                if (args.size < 2) return CommandResult(false, "usage: schem import <base64> [name]")
                val base64 = args[1]
                val schem = Schematics.readBase64(base64)
                if (args.size >= 3) schem.tags["name"] = args[2]
                Vars.schematics.add(schem)
                CommandResult(true, "Imported schematic '${schem.tags["name"] ?: "unnamed"}'")
            }
            else -> CommandResult(false, "unknown schem subcommand: $sub")
        }
    }
}