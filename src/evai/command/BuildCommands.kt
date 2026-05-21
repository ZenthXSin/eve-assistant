package evai.command

import mindustry.*
import mindustry.gen.*
import mindustry.world.*
import evai.command.CommandRegistry.CommandHandler

class BuildCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "build" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: build <block> <x> <y> [rotate], build area/line/ring <block> <args>")
            val player = Vars.player ?: return CommandResult(false, "no player")
            val sub = args[0].lowercase()
            when (sub) {
                "area" -> {
                    if (args.size < 6) return CommandResult(false, "usage: build area <block> <x1> <y1> <x2> <y2>")
                    val block = resolveBlock(args[1]) ?: return CommandResult(false, "block not found: ${args[1]}")
                    val x1 = args[2].toIntOrNull() ?: return CommandResult(false, "invalid x1")
                    val y1 = args[3].toIntOrNull() ?: return CommandResult(false, "invalid y1")
                    val x2 = args[4].toIntOrNull() ?: return CommandResult(false, "invalid x2")
                    val y2 = args[5].toIntOrNull() ?: return CommandResult(false, "invalid y2")
                    val minX = minOf(x1, x2).coerceAtLeast(0)
                    val maxX = maxOf(x1, x2).coerceAtMost(Vars.world.width() - 1)
                    val minY = minOf(y1, y2).coerceAtLeast(0)
                    val maxY = maxOf(y1, y2).coerceAtMost(Vars.world.height() - 1)
                    var count = 0
                    val unit = player.unit()
                    for (x in minX..maxX) {
                        for (y in minY..maxY) {
                            unit.addBuild(BuildPlan(x, y, 0, block))
                            count++
                        }
                    }
                    CommandResult(true, "Queued $count build plans for ${block.name}")
                }
                "line" -> {
                    if (args.size < 6) return CommandResult(false, "usage: build line <block> <x1> <y1> <x2> <y2>")
                    val block = resolveBlock(args[1]) ?: return CommandResult(false, "block not found: ${args[1]}")
                    val x1 = args[2].toIntOrNull() ?: return CommandResult(false, "invalid x1")
                    val y1 = args[3].toIntOrNull() ?: return CommandResult(false, "invalid y1")
                    val x2 = args[4].toIntOrNull() ?: return CommandResult(false, "invalid x2")
                    val y2 = args[5].toIntOrNull() ?: return CommandResult(false, "invalid y2")
                    val dx = if (x2 > x1) 1 else if (x2 < x1) -1 else 0
                    val dy = if (y2 > y1) 1 else if (y2 < y1) -1 else 0
                    val steps = maxOf(kotlin.math.abs(x2 - x1), kotlin.math.abs(y2 - y1))
                    var count = 0
                    val unit = player.unit()
                    for (i in 0..steps) {
                        val x = x1 + dx * i
                        val y = y1 + dy * i
                        if (x in 0 until Vars.world.width() && y in 0 until Vars.world.height()) {
                            unit.addBuild(BuildPlan(x, y, 0, block))
                            count++
                        }
                    }
                    CommandResult(true, "Queued $count build plans for line of ${block.name}")
                }
                "ring" -> {
                    if (args.size < 5) return CommandResult(false, "usage: build ring <block> <cx> <cy> <radius>")
                    val block = resolveBlock(args[1]) ?: return CommandResult(false, "block not found: ${args[1]}")
                    val cx = args[2].toIntOrNull() ?: return CommandResult(false, "invalid cx")
                    val cy = args[3].toIntOrNull() ?: return CommandResult(false, "invalid cy")
                    val r = args[4].toIntOrNull() ?: return CommandResult(false, "invalid radius")
                    var count = 0
                    val unit = player.unit()
                    for (angle in 0 until 360 step 15) {
                        val rad = Math.toRadians(angle.toDouble())
                        val x = (cx + r * kotlin.math.cos(rad)).toInt().coerceIn(0, Vars.world.width() - 1)
                        val y = (cy + r * kotlin.math.sin(rad)).toInt().coerceIn(0, Vars.world.height() - 1)
                        unit.addBuild(BuildPlan(x, y, 0, block))
                        count++
                    }
                    CommandResult(true, "Queued $count build plans for ring of ${block.name}")
                }
                "queue" -> {
                    CommandResult(false, "build queue not yet implemented")
                }
                else -> {
                    val block = resolveBlock(args[0]) ?: return CommandResult(false, "block not found: ${args[0]}")
                    val x = args.getOrNull(1)?.toIntOrNull() ?: return CommandResult(false, "invalid x")
                    val y = args.getOrNull(2)?.toIntOrNull() ?: return CommandResult(false, "invalid y")
                    val rotate = args.getOrNull(3)?.toIntOrNull() ?: 0
                    val unit = player.unit()
                    unit.addBuild(BuildPlan(x, y, rotate, block))
                    CommandResult(true, "Queued build ${block.name} at ($x, $y)")
                }
            }
        }
        "break" -> {
            val player = Vars.player ?: return CommandResult(false, "no player")
            if (args.isNotEmpty() && args[0] == "area") {
                if (args.size < 5) return CommandResult(false, "usage: break area <x1> <y1> <x2> <y2>")
                val x1 = args[1].toIntOrNull() ?: return CommandResult(false, "invalid x1")
                val y1 = args[2].toIntOrNull() ?: return CommandResult(false, "invalid y1")
                val x2 = args[3].toIntOrNull() ?: return CommandResult(false, "invalid x2")
                val y2 = args[4].toIntOrNull() ?: return CommandResult(false, "invalid y2")
                val minX = minOf(x1, x2).coerceAtLeast(0)
                val maxX = maxOf(x1, x2).coerceAtMost(Vars.world.width() - 1)
                val minY = minOf(y1, y2).coerceAtLeast(0)
                val maxY = maxOf(y1, y2).coerceAtMost(Vars.world.height() - 1)
                var count = 0
                val unit = player.unit()
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        unit.addBuild(BuildPlan(x, y, 0, Block.air, true))
                        count++
                    }
                }
                CommandResult(true, "Queued $count break plans")
            } else {
                val x = args.getOrNull(0)?.toIntOrNull() ?: return CommandResult(false, "usage: break <x> <y>, break area <x1> <y1> <x2> <y2>")
                val y = args.getOrNull(1)?.toIntOrNull() ?: return CommandResult(false, "usage: break <x> <y>")
                val unit = player.unit()
                unit.addBuild(BuildPlan(x, y, 0, Block.air, true))
                CommandResult(true, "Queued break at ($x, $y)")
            }
        }
        else -> CommandResult(false, "unknown build command: $action")
    }

    private fun resolveBlock(name: String): Block? {
        return Vars.content.blocks().find { it.name == name || it.name.endsWith("-$name") || it.name == name.lowercase().replace(" ", "-") }
    }
}