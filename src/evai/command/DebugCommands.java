package evai.command;

import mindustry.*;
import mindustry.game.*;
import java.util.*;

public class DebugCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        Rules rules = Vars.state != null ? Vars.state.rules : null;
        if (rules == null) return new CommandResult(false, "no game state");
        if (args.length == 0) return new CommandResult(false, "usage: debug <setting> [value]");
        String setting = args[0].toLowerCase();
        String value = args.length > 1 ? args[1].toLowerCase() : "";

        switch (setting) {
            case "sandbox": {
                rules.infiniteResources = true;
                rules.instantBuild = true;
                return new CommandResult(true, "Sandbox mode enabled");
            }
            case "instant": {
                rules.instantBuild = !(value.equals("off") || value.equals("false"));
                return new CommandResult(true, "Instant build: " + rules.instantBuild);
            }
            case "speed": {
                rules.buildSpeedMultiplier = tryParseFloat(value, 5f);
                return new CommandResult(true, "Build speed: " + rules.buildSpeedMultiplier + "x");
            }
            case "damage": {
                rules.blockDamageMultiplier = tryParseFloat(value, 3f);
                return new CommandResult(true, "Block damage: " + rules.blockDamageMultiplier + "x");
            }
            case "health": {
                rules.blockHealthMultiplier = tryParseFloat(value, 10f);
                return new CommandResult(true, "Block health: " + rules.blockHealthMultiplier + "x");
            }
            case "mine": {
                rules.unitMineSpeedMultiplier = tryParseFloat(value, 5f);
                return new CommandResult(true, "Mine speed: " + rules.unitMineSpeedMultiplier + "x");
            }
            case "buildcost": {
                rules.buildCostMultiplier = tryParseFloat(value, 0.1f);
                return new CommandResult(true, "Build cost: " + rules.buildCostMultiplier + "x");
            }
            case "refund": {
                rules.deconstructRefundMultiplier = tryParseFloat(value, 1f);
                return new CommandResult(true, "Refund rate: " + rules.deconstructRefundMultiplier + "x");
            }
            case "unitdmg": {
                rules.unitDamageMultiplier = tryParseFloat(value, 2f);
                return new CommandResult(true, "Unit damage: " + rules.unitDamageMultiplier + "x");
            }
            case "unithp": {
                rules.unitHealthMultiplier = tryParseFloat(value, 2f);
                return new CommandResult(true, "Unit health: " + rules.unitHealthMultiplier + "x");
            }
            case "unitspeed": {
                rules.unitBuildSpeedMultiplier = tryParseFloat(value, 2f);
                return new CommandResult(true, "Unit build speed: " + rules.unitBuildSpeedMultiplier + "x");
            }
            case "fire": {
                rules.fire = !value.equals("off");
                return new CommandResult(true, "Fire: " + (rules.fire ? "on" : "off"));
            }
            case "reactor": {
                rules.reactorExplosions = !value.equals("off");
                return new CommandResult(true, "Reactor: " + (rules.reactorExplosions ? "on" : "off"));
            }
            case "waves": {
                rules.waveSending = !value.equals("off");
                return new CommandResult(true, "Waves: " + (rules.waveSending ? "on" : "off"));
            }
            case "nospawn": {
                rules.waveTimer = value.equals("off");
                return new CommandResult(true, "Enemy spawn: " + (rules.waveTimer ? "on" : "off"));
            }
            case "edit": {
                rules.allowEditRules = true;
                return new CommandResult(true, "Rule editing enabled");
            }
            case "reset": {
                rules.infiniteResources = false; rules.instantBuild = false;
                rules.buildSpeedMultiplier = 1f; rules.blockHealthMultiplier = 1f;
                rules.blockDamageMultiplier = 1f; rules.unitHealthMultiplier = 1f;
                rules.unitDamageMultiplier = 1f; rules.buildCostMultiplier = 1f;
                rules.deconstructRefundMultiplier = 1f; rules.unitBuildSpeedMultiplier = 1f;
                rules.unitMineSpeedMultiplier = 1f; rules.fire = true;
                rules.reactorExplosions = true; rules.waveSending = true;
                rules.waveTimer = true;
                return new CommandResult(true, "All rules reset to defaults");
            }
            default: return new CommandResult(false, "unknown debug setting: " + setting);
        }
    }

    private static float tryParseFloat(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return fallback; }
    }
}