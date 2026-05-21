package evai.command

import mindustry.*
import evai.command.CommandRegistry.CommandHandler

class DebugCommands : CommandHandler {
    private val defaultRules = mutableMapOf<String, Any?>()

    override fun handle(action: String, args: Array<String>): CommandResult {
        val rules = Vars.state?.rules ?: return CommandResult(false, "no game state")
        if (args.isEmpty()) return CommandResult(false, "usage: debug <setting> [value]")
        val setting = args[0].lowercase()
        val value = if (args.size > 1) args[1].lowercase() else ""

        return when (setting) {
            "sandbox" -> {
                rules.infiniteResources = true
                rules.instantBuild = true
                CommandResult(true, "Sandbox mode enabled (infinite resources + instant build)")
            }
            "instant" -> {
                rules.instantBuild = !(value == "off" || value == "false")
                CommandResult(true, "Instant build: ${rules.instantBuild}")
            }
            "speed" -> {
                val mult = value.toFloatOrNull() ?: 5f
                rules.buildSpeedMultiplier = mult
                CommandResult(true, "Build speed: ${mult}x")
            }
            "damage" -> {
                val mult = value.toFloatOrNull() ?: 3f
                rules.blockDamageMultiplier = mult
                CommandResult(true, "Block damage: ${mult}x")
            }
            "health" -> {
                val mult = value.toFloatOrNull() ?: 10f
                rules.blockHealthMultiplier = mult
                CommandResult(true, "Block health: ${mult}x")
            }
            "mine" -> {
                val mult = value.toFloatOrNull() ?: 5f
                rules.unitMineSpeedMultiplier = mult
                CommandResult(true, "Mine speed: ${mult}x")
            }
            "buildcost" -> {
                val mult = value.toFloatOrNull() ?: 0.1f
                rules.buildCostMultiplier = mult
                CommandResult(true, "Build cost: ${mult}x")
            }
            "refund" -> {
                val mult = value.toFloatOrNull() ?: 1f
                rules.deconstructRefundMultiplier = mult
                CommandResult(true, "Refund rate: ${mult}x")
            }
            "unitdmg" -> {
                val mult = value.toFloatOrNull() ?: 2f
                rules.unitDamageMultiplier = mult
                CommandResult(true, "Unit damage: ${mult}x")
            }
            "unithp" -> {
                val mult = value.toFloatOrNull() ?: 2f
                rules.unitHealthMultiplier = mult
                CommandResult(true, "Unit health: ${mult}x")
            }
            "unitspeed" -> {
                val mult = value.toFloatOrNull() ?: 2f
                rules.unitBuildSpeedMultiplier = mult
                CommandResult(true, "Unit build speed: ${mult}x")
            }
            "fire" -> {
                rules.fire = value != "off"
                CommandResult(true, "Fire: ${if (rules.fire) "on" else "off"}")
            }
            "reactor" -> {
                rules.reactorExplosions = value != "off"
                CommandResult(true, "Reactor explosions: ${if (rules.reactorExplosions) "on" else "off"}")
            }
            "waves" -> {
                rules.waveSending = value != "off"
                CommandResult(true, "Waves: ${if (rules.waveSending) "on" else "off"}")
            }
            "nospawn" -> {
                rules.waveTimer = value == "off"
                CommandResult(true, "Enemy spawn: ${if (rules.waveTimer) "on" else "off"}")
            }
            "edit" -> {
                rules.allowEditRules = true
                CommandResult(true, "Rule editing enabled")
            }
            "reset" -> {
                rules.infiniteResources = false
                rules.instantBuild = false
                rules.buildSpeedMultiplier = 1f
                rules.blockHealthMultiplier = 1f
                rules.blockDamageMultiplier = 1f
                rules.unitHealthMultiplier = 1f
                rules.unitDamageMultiplier = 1f
                rules.buildCostMultiplier = 1f
                rules.deconstructRefundMultiplier = 1f
                rules.unitBuildSpeedMultiplier = 1f
                rules.unitMineSpeedMultiplier = 1f
                rules.fire = true
                rules.reactorExplosions = true
                rules.waveSending = true
                rules.waveTimer = true
                CommandResult(true, "All rules reset to defaults")
            }
            else -> CommandResult(false, "unknown debug setting: $setting")
        }
    }
}