package evai.command

import mindustry.*
import mindustry.content.*
import mindustry.gen.*
import mindustry.game.*
import mindustry.entities.*
import evai.command.CommandRegistry.CommandHandler

class SpawnCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "spawn" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: spawn [enemy/ally/at] <unit> <x> <y> [team] [amount]")
            var team: Team? = null
            var nameIdx = 0
            var amount = 1

            when (args[0].lowercase()) {
                "enemy" -> {
                    team = Vars.player?.team()?.enemies ?: Team.derelict
                    nameIdx = 1
                }
                "ally" -> {
                    team = Vars.player?.team()
                    nameIdx = 1
                }
                "at" -> {
                    val player = Vars.player ?: return CommandResult(false, "no player")
                    val uName = args.getOrNull(1) ?: return CommandResult(false, "usage: spawn at <unit> [amount]")
                    val amt = args.getOrNull(2)?.toIntOrNull() ?: 1
                    val unitType = Vars.content.units().find { it.name == uName } ?: return CommandResult(false, "unit not found: $uName")
                    for (i in 0 until amt) {
                        val unit = unitType.spawn(player.team(), player.x + (Math.random() * 10 - 5).toFloat(), player.y + (Math.random() * 10 - 5).toFloat())
                    }
                    return CommandResult(true, "Spawned $amt ${unitType.name} at player position")
                }
                else -> {
                    // spawn <unit> <x> <y> [team] [amount]
                    nameIdx = 0
                }
            }

            val unitName = args.getOrNull(nameIdx) ?: return CommandResult(false, "usage: spawn <unit> <x> <y> [team] [amount]")
            val unitType = Vars.content.units().find { it.name == unitName } ?: return CommandResult(false, "unit not found: $unitName")

            val x = args.getOrNull(nameIdx + 1)?.toFloatOrNull() ?: (Vars.player?.x ?: return CommandResult(false, "no position"))
            val y = args.getOrNull(nameIdx + 2)?.toFloatOrNull() ?: (Vars.player?.y ?: return CommandResult(false, "no position"))
            if (team == null) {
                val teamStr = args.getOrNull(nameIdx + 3)
                team = if (teamStr != null) Team.all.find { it.name.equals(teamStr, true) } ?: Team.sharded else Team.sharded
            }
            val amtIdx = if (team != null && nameIdx == 0 && args.size > nameIdx + 4) nameIdx + 4 else nameIdx + 3
            amount = args.getOrNull(amtIdx)?.toIntOrNull() ?: 1

            for (i in 0 until amount) {
                val ox = x + (Math.random() * 5 - 2.5).toFloat()
                val oy = y + (Math.random() * 5 - 2.5).toFloat()
                val unit = unitType.spawn(team, ox, oy)
            }
            CommandResult(true, "Spawned $amount ${unitType.name} at (${x.toInt()}, ${y.toInt()}) team=${team.name}")
        }
        "control" -> {
            val player = Vars.player ?: return CommandResult(false, "no player")
            if (args.isEmpty()) {
                // Switch back to original unit
                player.unit(player.deathHandler)
                CommandResult(true, "Switched back to default unit")
            } else {
                val unitName = args[0]
                val unit = Vars.content.units().find { it.name == unitName } ?: return CommandResult(false, "unit not found: $unitName")
                player.unit(unit)
                CommandResult(true, "Controlling unit type: ${unit.name}")
            }
        }
        "kill" -> {
            val target = args.getOrNull(0)?.lowercase() ?: return CommandResult(false, "usage: kill <all/enemies/allies> [radius]")
            val radius = args.getOrNull(1)?.toFloatOrNull() ?: 100f
            val player = Vars.player ?: return CommandResult(false, "no player")
            var count = 0
            when (target) {
                "all" -> {
                    val units = mutableListOf<Unit>()
                    Units.nearby(player.x, player.y, radius) { units.add(it) }
                    for (u in units) { u.kill(); count++ }
                }
                "enemies" -> {
                    val enemyTeam = player.team().enemies
                    val units = mutableListOf<Unit>()
                    Units.nearby(player.x, player.y, radius) { if (it.team == enemyTeam) units.add(it) }
                    for (u in units) { u.kill(); count++ }
                }
                "allies" -> {
                    val allyTeam = player.team()
                    val units = mutableListOf<Unit>()
                    Units.nearby(player.x, player.y, radius) { if (it.team == allyTeam) units.add(it) }
                    for (u in units) { u.kill(); count++ }
                }
                else -> return CommandResult(false, "unknown kill target: $target")
            }
            CommandResult(true, "Killed $count units in radius $radius")
        }
        else -> CommandResult(false, "unknown spawn command: $action")
    }
}