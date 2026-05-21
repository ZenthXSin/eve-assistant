package evai.command

import mindustry.*
import mindustry.entities.*
import mindustry.game.*
import evai.command.CommandRegistry.CommandHandler

class ScanCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "scan" -> {
            if (args.isEmpty() || args[0] == "resources" || args[0] == "liquids") {
                scanResources(args)
            } else {
                val radius = args[0].toIntOrNull() ?: 20
                val player = Vars.player ?: return CommandResult(false, "no player")
                val tx = player.tileX()
                val ty = player.tileY()
                val blocks = mutableListOf<Map<String, Any?>>()
                for (x in (tx - radius).coerceAtLeast(0)..(tx + radius).coerceAtMost(Vars.world.width() - 1)) {
                    for (y in (ty - radius).coerceAtLeast(0)..(ty + radius).coerceAtMost(Vars.world.height() - 1)) {
                        val tile = Vars.world.tile(x, y) ?: continue
                        if (tile.block() !== mindustry.world.blocks.environment.Air) {
                            val build = tile.build()
                            blocks.add(mapOf(
                                "x" to x, "y" to y,
                                "block" to (tile.block()?.name ?: "?"),
                                "team" to (tile.team()?.name ?: "none"),
                                "health" to (build?.health?.toInt() ?: 0)
                            ))
                        }
                    }
                }
                CommandResult(true, data = mapOf("center" to mapOf("x" to tx, "y" to ty), "radius" to radius, "count" to blocks.size, "blocks" to blocks))
            }
        }
        "check" -> {
            var idx = 0
            var field = ""
            if (args.size >= 3 && args[0] in listOf("items", "power", "liquid", "health", "tile", "enemies", "allies")) {
                field = args[0]
                idx = 1
            }
            if (field == "enemies" || field == "allies") {
                val radius = if (args.size > idx) args[idx].toIntOrNull() ?: 50 else 50
                val player = Vars.player ?: return CommandResult(false, "no player")
                val team = if (field == "enemies") player.team().enemies else player.team()
                @Suppress("UNCHECKED_CAST")
                val units = (arc.struct.Seq::class.java.getMethod("copy", Unit::class.java) as? java.lang.reflect.Method)?.let {
                    // Try to get nearby units
                    val nearby = mutableListOf<Map<String, Any?>>()
                    Units.nearby(player.x, player.y, radius.toFloat()) { u ->
                        if (u.team == team) {
                            nearby.add(mapOf("name" to (u.type?.name ?: "?"), "health" to "${u.health.toInt()}/${u.maxHealth.toInt()}", "x" to u.x.toInt(), "y" to u.y.toInt()))
                        }
                    }
                    nearby
                } ?: run {
                    val nearby = mutableListOf<Map<String, Any?>>()
                    Units.nearby(player.x, player.y, radius.toFloat()) { u ->
                        if (u.team == team) {
                            nearby.add(mapOf("name" to (u.type?.name ?: "?"), "health" to "${u.health.toInt()}/${u.maxHealth.toInt()}", "x" to u.x.toInt(), "y" to u.y.toInt()))
                        }
                    }
                    nearby
                }
                CommandResult(true, data = mapOf("type" to field, "count" to units.size, "units" to units))
            } else {
                val x = if (args.size > idx) args[idx].toIntOrNull() else null
                val y = if (args.size > idx + 1) args[idx + 1].toIntOrNull() else null
                if (x == null || y == null) return CommandResult(false, "usage: check [field] <x> <y>")
                val tile = Vars.world.tile(x, y) ?: return CommandResult(false, "tile not found")
                when (field) {
                    "tile" -> CommandResult(true, data = mapOf("x" to tile.x, "y" to tile.y, "block" to (tile.block()?.name ?: "air"), "floor" to (tile.floor()?.name ?: "?"), "overlay" to (tile.overlay()?.name ?: "none"), "team" to (tile.team()?.name ?: "none"), "solid" to tile.solid()))
                    "items" -> {
                        val build = tile.build() ?: return CommandResult(false, "no building")
                        CommandResult(true, data = mapOf("items" to (if (build.items != null) build.items.total() else 0), "capacity" to build.block.itemCapacity))
                    }
                    "power" -> {
                        val build = tile.build() ?: return CommandResult(false, "no building")
                        val power = build.power
                        CommandResult(true, data = mapOf("status" to (power?.status ?: "none"), "produced" to (power?.graph?.getPowerProduced() ?: 0.0), "needed" to (power?.graph?.getPowerNeeded() ?: 0.0)))
                    }
                    "liquid" -> {
                        val build = tile.build() ?: return CommandResult(false, "no building")
                        CommandResult(true, data = mapOf("liquids" to (build.liquids?.total() ?: 0.0), "capacity" to (build.block.liquidCapacity)))
                    }
                    "health" -> {
                        val build = tile.build() ?: return CommandResult(false, "no building")
                        CommandResult(true, data = mapOf("health" to "${build.health}/${build.maxHealth}"))
                    }
                    "" -> {
                        val build = tile.build()
                        val data = mutableMapOf<String, Any?>("x" to tile.x, "y" to tile.y, "block" to (tile.block()?.name ?: "air"), "floor" to (tile.floor()?.name ?: "?"), "team" to (tile.team()?.name ?: "none"))
                        if (build != null) {
                            data["building"] = mapOf("health" to "${build.health}/${build.maxHealth}", "enabled" to build.enabled, "warmup" to build.warmup, "efficiency" to build.efficiency, "rotation" to build.rotation, "items" to (build.items?.total() ?: 0), "itemCapacity" to build.block.itemCapacity)
                        }
                        CommandResult(true, data = data)
                    }
                    else -> CommandResult(false, "unknown field: $field")
                }
            }
        }
        "map" -> {
            val world = Vars.world
            val state = Vars.state
            CommandResult(true, data = mapOf("width" to world.width(), "height" to world.height(), "map" to (state.map?.name() ?: "none"), "wave" to state.wave))
        }
        else -> CommandResult(false, "unknown scan command: $action")
    }

    private fun scanResources(args: Array<String>): CommandResult {
        val player = Vars.player ?: return CommandResult(false, "no player")
        val radius = if (args.size > 1) args[1].toIntOrNull() ?: 20 else 20
        val tx = player.tileX()
        val ty = player.tileY()
        val resources = mutableListOf<Map<String, Any?>>()
        for (x in (tx - radius).coerceAtLeast(0)..(tx + radius).coerceAtMost(Vars.world.width() - 1)) {
            for (y in (ty - radius).coerceAtLeast(0)..(ty + radius).coerceAtMost(Vars.world.height() - 1)) {
                val tile = Vars.world.tile(x, y) ?: continue
                if (args[0] == "resources" && tile.overlay()?.itemDrop != null) {
                    resources.add(mapOf("x" to x, "y" to y, "ore" to tile.overlay()?.name, "item" to tile.overlay()?.itemDrop?.name))
                } else if (args[0] == "liquids" && tile.floor()?.liquidDrop != null) {
                    resources.add(mapOf("x" to x, "y" to y, "liquid" to tile.floor()?.liquidDrop?.name))
                }
            }
        }
        return CommandResult(true, data = mapOf("type" to args[0], "count" to resources.size, "resources" to resources))
    }
}