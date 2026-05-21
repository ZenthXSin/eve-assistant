package evai.command

import mindustry.*
import evai.command.CommandRegistry.CommandHandler

class BaseCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "help" -> {
            if (args.isNotEmpty()) {
                CommandResult(true, "See /eve <action> [args]. Full docs in PLAN.md")
            } else {
                CommandResult(true, """Eve Assistant Mod v1.0
Commands:
  help, status, whereami, clear, version
  list <type> [filter], info <name>
  scan [r], scan resources/liquids, check <field> <x> <y>, map
  debug <setting> [value]
  set <x> <y> <field> <value>, set items/liquid/power/health/team/config x y <val>
  nuke <x> <y> [r]
  build <block> <x> <y> [r], build area/line/ring <block> <args...>
  break <x> <y>, break area <x1> <y1> <x2> <y2>
  schem <create/place/list/delete/rename/export/import> <args...>
  spawn <unit> <x> <y> [team] [amt], spawn enemy/ally <unit> <x> <y> [amt]
  control [unit], kill <all/enemies/allies> [r]
  listen <event>, listen all/stop/list, fire <event> [args]
  js <code>, logic <code>, eval <expr>
  save/load <name>, save list/delete/current
  export blocks/state/tile/units [filter/r]""")
            }
        }
        "status" -> {
            val state = Vars.state
            val world = Vars.world
            val player = Vars.player
            CommandResult(true, data = mapOf(
                "map" to (state.map?.name() ?: "none"),
                "wave" to state.wave,
                "team" to (player?.team()?.name ?: "none"),
                "player" to (player?.name ?: "none"),
                "tiles" to "${world.width()}x${world.height()}",
                "mode" to (state.rules?.mode()?.name ?: "unknown")
            ))
        }
        "whereami" -> {
            val player = Vars.player
            if (player != null) {
                val x = player.x.toInt()
                val y = player.y.toInt()
                val tile = Vars.world.tileWorld(x.toFloat(), y.toFloat())
                CommandResult(true, data = mapOf(
                    "x" to x, "y" to y,
                    "tileX" to (tile?.x ?: -1),
                    "tileY" to (tile?.y ?: -1),
                    "block" to (tile?.block()?.name ?: "none"),
                    "floor" to (tile?.floor()?.name ?: "none")
                ))
            } else {
                CommandResult(false, "no player")
            }
        }
        "clear" -> CommandResult(true, "chat cleared")
        "version" -> CommandResult(true, "Eve Assistant Mod v1.0")
        else -> CommandResult(false, "unknown base command: $action")
    }
}