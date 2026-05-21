package evai.command;

import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.game.*;
import java.util.*;

public class SpawnCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "spawn": return spawn(args);
            case "control": return control(args);
            case "kill": return kill(args);
            default: return new CommandResult(false, "unknown spawn command: " + action);
        }
    }

    private CommandResult spawn(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: spawn [at] <unit> <x> <y> [team] [amount]");
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        Team team = player.team();

        if (args[0].equalsIgnoreCase("at")) {
            if (args.length < 2) return new CommandResult(false, "usage: spawn at <unit> [amount]");
            mindustry.type.UnitType unitType = Vars.content.units().find(u -> u.name.equals(args[1]));
            if (unitType == null) return new CommandResult(false, "unit not found: " + args[1]);
            int amt = args.length > 2 ? tryParseInt(args[2], 1) : 1;
            for (int i = 0; i < amt; i++) {
                unitType.spawn(team, player.x + (float)(Math.random() * 10 - 5), player.y + (float)(Math.random() * 10 - 5));
            }
            return new CommandResult(true, "Spawned " + amt + " " + unitType.name);
        }

        mindustry.type.UnitType unitType = Vars.content.units().find(u -> u.name.equals(args[0]));
        if (unitType == null) return new CommandResult(false, "unit not found: " + args[0]);
        float x = args.length > 1 ? tryParseFloat(args[1], player.x) : player.x;
        float y = args.length > 2 ? tryParseFloat(args[2], player.y) : player.y;
        if (args.length > 3) {
            Team t = Team.all[0];
            for (Team tt : Team.all) { if (tt != null && tt.name.equalsIgnoreCase(args[3])) { t = tt; break; } }
            team = t;
        }
        int amount = args.length > 4 ? tryParseInt(args[4], 1) : 1;
        for (int i = 0; i < amount; i++) {
            unitType.spawn(team, x + (float)(Math.random() * 5 - 2.5), y + (float)(Math.random() * 5 - 2.5));
        }
        return new CommandResult(true, "Spawned " + amount + " " + unitType.name);
    }

    private CommandResult control(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        if (args.length == 0) {
            return new CommandResult(true, "Current unit: " + player.unit().type().name);
        }
        return new CommandResult(true, "Direct control switch not available");
    }

    private CommandResult kill(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        if (args.length == 0) return new CommandResult(false, "usage: kill <all/enemies/allies> [radius]");
        String target = args[0].toLowerCase();
        float radius = args.length > 1 ? tryParseFloat(args[1], 100f) : 100f;
        List<mindustry.gen.Unit> units = new ArrayList<>();
        Units.nearby(player.x, player.y, radius * 2, radius * 2, units::add);
        int count = 0;
        for (mindustry.gen.Unit u : units) {
            boolean isAlly = u.team() == player.team();
            if (target.equals("all") || (target.equals("enemies") && !isAlly) || (target.equals("allies") && isAlly)) {
                u.kill();
                count++;
            }
        }
        return new CommandResult(true, "Killed " + count + " units");
    }

    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
    private static float tryParseFloat(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return fallback; }
    }
}