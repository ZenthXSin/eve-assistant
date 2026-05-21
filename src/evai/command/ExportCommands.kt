package evai.command

import mindustry.*
import evai.command.CommandRegistry.CommandHandler

class ExportCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "export" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: export blocks/state/tile/units [filter/radius]")
            when (args[0].lowercase()) {
                "blocks" -> {
                    val filter = if (args.size > 1) args[1] else ""
                    val blocks = Vars.content.blocks().filter { filter.isBlank() || it.name.contains(filter) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "type" to it.javaClass.simpleName, "health" to it.health, "size" to it.size, "itemCapacity" to it.itemCapacity) }))
                }
                "state" -> {
                    val state = Vars.state
                    val rules = state.rules
                    CommandResult(true, data = mapOf("wave" to state.wave, "map" to (state.map?.name() ?: "none"), "rules" to mapOf("infiniteResources" to rules.infiniteResources, "instantBuild" to rules.instantBuild, "buildSpeedMultiplier" to rules.buildSpeedMultiplier)))
                }
                "tile" -> {
                    if (args.size < 3) return CommandResult(false, "usage: export tile <x> <y>")
                    val x = args[1].toIntOrNull() ?: return CommandResult(false, "invalid x")
                    val y = args[2].toIntOrNull() ?: return CommandResult(false, "invalid y")
                    val tile = Vars.world.tile(x, y) ?: return CommandResult(false, "tile not found")
                    CommandResult(true, data = mapOf("x" to tile.x, "y" to tile.y, "block" to (tile.block()?.name ?: "air"), "floor" to (tile.floor()?.name ?: "?"), "overlay" to (tile.overlay()?.name ?: "none"), "team" to (tile.team()?.name ?: "none"), "solid" to tile.solid()))
                }
                "units" -> {
                    val radius = args.getOrNull(1)?.toFloatOrNull() ?: 100f
                    val player = Vars.player ?: return CommandResult(false, "no player")
                    val list = mutableListOf<Map<String, Any?>>()
                    mindustry.entities.Units.nearby(player.x, player.y, radius) { u ->
                        list.add(mapOf("name" to (u.type?.name ?: "?"), "health" to "${u.health.toInt()}/${u.maxHealth.toInt()}", "team" to u.team.name, "x" to u.x.toInt(), "y" to u.y.toInt()))
                    }
                    CommandResult(true, data = mapOf("count" to list.size, "units" to list))
                }
                else -> CommandResult(false, "unknown export type: ${args[0]}")
            }
        }
        else -> CommandResult(false, "unknown export command: $action")
    }
}