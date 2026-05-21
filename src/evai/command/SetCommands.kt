package evai.command

import mindustry.*
import mindustry.gen.*
import mindustry.world.*
import evai.command.CommandRegistry.CommandHandler

class SetCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "set" -> {
            if (args.size < 3) return CommandResult(false, "usage: set [items/liquid/power/health/team/config/enabled/warmup/progress] <x> <y> <value>, or set <x> <y> <field> <value>")
            var idx = 0
            var sub = ""
            if (args[0] in listOf("items", "liquid", "power", "health", "team", "config", "enabled", "warmup", "progress")) {
                sub = args[0]
                idx = 1
            }
            val x = args[idx].toIntOrNull() ?: return CommandResult(false, "invalid x")
            val y = args[idx + 1].toIntOrNull() ?: return CommandResult(false, "invalid y")
            val tile = Vars.world.tile(x, y) ?: return CommandResult(false, "tile not found")
            val build = tile.build() ?: return CommandResult(false, "no building at $x,$y")
            if (sub.isNotEmpty()) {
                val valStr = if (args.size > idx + 2) args[idx + 2] else ""
                setSubField(build, sub, valStr)
            } else {
                val field = args[idx + 2]
                val valStr = if (args.size > idx + 3) args[idx + 3] else ""
                setReflectField(build, field, valStr)
            }
        }
        "nuke" -> {
            val x = args.getOrNull(0)?.toIntOrNull() ?: return CommandResult(false, "usage: nuke <x> <y> [radius]")
            val y = args.getOrNull(1)?.toIntOrNull() ?: return CommandResult(false, "usage: nuke <x> <y> [radius]")
            val radius = args.getOrNull(2)?.toIntOrNull() ?: 10
            for (dx in -radius..radius) {
                for (dy in -radius..radius) {
                    if (dx * dx + dy * dy > radius * radius) continue
                    val tile = Vars.world.tile(x + dx, y + dy) ?: continue
                    val build = tile.build() ?: continue
                    build.kill()
                }
            }
            CommandResult(true, "Nuked radius $radius at ($x, $y)")
        }
        else -> CommandResult(false, "unknown set command: $action")
    }

    private fun setSubField(build: Building, sub: String, value: String): CommandResult = try {
        when (sub) {
            "items" -> {
                val parts = value.split(" ")
                if (parts.size < 2) return CommandResult(false, "usage: set items <x> <y> <item> <amount>")
                val item = Vars.content.items().find { it.name == parts[0] } ?: return CommandResult(false, "unknown item: ${parts[0]}")
                val amount = parts[1].toIntOrNull() ?: return CommandResult(false, "invalid amount")
                build.items?.set(item, amount)
                CommandResult(true, "Set ${parts[0]} to $amount at building")
            }
            "liquid" -> {
                val parts = value.split(" ")
                if (parts.size < 2) return CommandResult(false, "usage: set liquid <x> <y> <liquid> <amount>")
                val liquid = Vars.content.liquids().find { it.name == parts[0] } ?: return CommandResult(false, "unknown liquid: ${parts[0]}")
                val amount = parts[1].toFloatOrNull() ?: return CommandResult(false, "invalid amount")
                build.liquids?.set(liquid, amount)
                CommandResult(true, "Set ${parts[0]} to $amount at building")
            }
            "power" -> {
                val amount = value.toFloatOrNull() ?: return CommandResult(false, "invalid amount")
                build.power?.status = amount / build.block.consumes?.power?.capacity?.toFloat() ?: 1f
                CommandResult(true, "Set power to $amount")
            }
            "health" -> {
                val amount = value.toFloatOrNull() ?: return CommandResult(false, "invalid amount")
                build.health = amount
                CommandResult(true, "Set health to $amount")
            }
            "team" -> {
                val team = mindustry.game.Team.all.find { it.name.equals(value, true) } ?: return CommandResult(false, "unknown team: $value")
                build.team = team
                CommandResult(true, "Set team to $value")
            }
            "config" -> {
                build.configure(value)
                CommandResult(true, "Sent config: $value")
            }
            "enabled" -> {
                val en = value == "on" || value == "true"
                build.enabled = en
                CommandResult(true, "Enabled: $en")
            }
            "warmup" -> {
                val w = value.toFloatOrNull() ?: return CommandResult(false, "invalid warmup (0-1)")
                build.warmup = w.coerceIn(0f, 1f)
                CommandResult(true, "Warmup: $w")
            }
            "progress" -> {
                val p = value.toFloatOrNull() ?: return CommandResult(false, "invalid progress")
                build.progress = p
                CommandResult(true, "Progress: $p")
            }
            else -> CommandResult(false, "unknown sub: $sub")
        }
    } catch (e: Exception) {
        CommandResult(false, "error: ${e.message}")
    }

    private fun setReflectField(build: Building, field: String, value: String): CommandResult = try {
        var clazz: Class<*> = build.javaClass
        var found = false
        while (clazz != Any::class.java) {
            try {
                val f = clazz.getDeclaredField(field)
                f.isAccessible = true
                val type = f.type
                when {
                    type == Int::class.java || type == Int::class.javaPrimitiveType -> f.setInt(build, value.toInt())
                    type == Float::class.java || type == Float::class.javaPrimitiveType -> f.setFloat(build, value.toFloat())
                    type == Boolean::class.java || type == Boolean::class.javaPrimitiveType -> f.setBoolean(build, value.toBoolean())
                    type == String::class.java -> f.set(build, value)
                    type == Double::class.java || type == Double::class.javaPrimitiveType -> f.setDouble(build, value.toDouble())
                    type == Long::class.java || type == Long::class.javaPrimitiveType -> f.setLong(build, value.toLong())
                    else -> f.set(build, value)
                }
                found = true
                break
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        if (found) CommandResult(true, "Set $field = $value")
        else CommandResult(false, "field not found: $field")
    } catch (e: Exception) {
        CommandResult(false, "error setting $field: ${e.message}")
    }
}