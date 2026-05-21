package evai.command;

import mindustry.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.gen.*;
import mindustry.content.*;
import mindustry.world.consumers.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import java.util.*;

public class SetCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "set": return set(args);
            case "nuke": return nuke(args);
            default: return new CommandResult(false, "unknown set command: " + action);
        }
    }

    private CommandResult set(String[] args) {
        if (args.length < 3) return new CommandResult(false, "usage: set [items/liquid/power/health/team/config/enabled/warmup/progress] <x> <y> <value>");
        int idx = 0;
        String sub = "";
        Set<String> subs = new HashSet<>(Arrays.asList("items", "liquid", "power", "health", "team", "config", "enabled", "warmup", "progress"));
        if (subs.contains(args[0])) { sub = args[0]; idx = 1; }
        Integer x = tryParseInt(args[idx], null);
        Integer y = tryParseInt(args[idx + 1], null);
        if (x == null || y == null) return new CommandResult(false, "invalid coordinates");
        Tile tile = Vars.world.tile(x, y);
        if (tile == null) return new CommandResult(false, "tile not found");
        Building bld = tile.build;
        if (!sub.isEmpty()) {
            String val = args.length > idx + 2 ? args[idx + 2] : "";
            return setSubField(bld, sub, val);
        }
        String field = args[idx + 2];
        String val = args.length > idx + 3 ? args[idx + 3] : "";
        return setReflectField(bld, field, val);
    }

    private CommandResult setSubField(Building bld, String sub, String value) {
        if (bld == null) return new CommandResult(false, "no building");
        try {
            switch (sub) {
                case "items": {
                    String[] parts = value.split(" ");
                    if (parts.length < 2) return new CommandResult(false, "usage: set items <x> <y> <item> <amount>");
                    mindustry.type.Item item = Vars.content.items().find(i -> i.name.equals(parts[0]));
                    if (item == null) return new CommandResult(false, "unknown item: " + parts[0]);
                    int amount = tryParseInt(parts[1], -1);
                    if (amount < 0) return new CommandResult(false, "invalid amount");
                    if (bld.items != null) bld.items.set(item, amount);
                    return new CommandResult(true, "Set " + parts[0] + " to " + amount);
                }
                case "liquid": {
                    String[] parts = value.split(" ");
                    if (parts.length < 2) return new CommandResult(false, "usage: set liquid <x> <y> <liquid> <amount>");
                    mindustry.type.Liquid liquid = Vars.content.liquids().find(l -> l.name.equals(parts[0]));
                    if (liquid == null) return new CommandResult(false, "unknown liquid: " + parts[0]);
                    float amount = tryParseFloat(parts[1], -1f);
                    if (amount < 0) return new CommandResult(false, "invalid amount");
                    if (bld.liquids != null) bld.liquids.set(liquid, amount);
                    return new CommandResult(true, "Set " + parts[0] + " to " + amount);
                }
                case "power": {
                    float amount = tryParseFloat(value, -1f);
                    if (amount < 0) return new CommandResult(false, "invalid amount");
                    float cap = 1f;
                    try { cap = bld.block.consPower.capacity; } catch (Exception ignored) {}
                    if (bld.power != null) bld.power.status = amount / cap;
                    return new CommandResult(true, "Set power to " + amount);
                }
                case "health": {
                    float h = tryParseFloat(value, -1f);
                    if (h < 0) return new CommandResult(false, "invalid amount");
                    bld.health(h);
                    return new CommandResult(true, "Set health to " + h);
                }
                case "team": {
                    mindustry.game.Team team = Team.all[0]; // fallback
                    for (Team t : Team.all) {
                        if (t != null && t.name.equalsIgnoreCase(value)) { team = t; break; }
                    }
                    bld.team(team);
                    return new CommandResult(true, "Set team to " + team.name);
                }
                case "config": {
                    bld.configure(value);
                    return new CommandResult(true, "Sent config");
                }
                case "enabled": {
                    bld.enabled = value.equals("on") || value.equals("true");
                    return new CommandResult(true, "Enabled: " + bld.enabled);
                }
                case "warmup": {
                    float w = tryParseFloat(value, -1f);
                    if (w < 0) return new CommandResult(false, "invalid warmup");
                    return setReflectField(bld, "warmup", value);
                }
                case "progress": {
                    float p = tryParseFloat(value, -1f);
                    if (p < 0) return new CommandResult(false, "invalid progress");
                    return setReflectField(bld, "progress", value);
                }
                default: return new CommandResult(false, "unknown sub: " + sub);
            }
        } catch (Exception e) {
            return new CommandResult(false, "error: " + e.getMessage());
        }
    }

    private CommandResult setReflectField(Building bld, String field, String value) {
        if (bld == null) return new CommandResult(false, "no building");
        try {
            Class<?> clazz = bld.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    java.lang.reflect.Field f = clazz.getDeclaredField(field);
                    f.setAccessible(true);
                    Class<?> type = f.getType();
                    if (type == int.class || type == Integer.class) f.setInt(bld, Integer.parseInt(value));
                    else if (type == float.class || type == Float.class) f.setFloat(bld, Float.parseFloat(value));
                    else if (type == boolean.class || type == Boolean.class) f.setBoolean(bld, Boolean.parseBoolean(value));
                    else if (type == String.class) f.set(bld, value);
                    else if (type == double.class || type == Double.class) f.setDouble(bld, Double.parseDouble(value));
                    else f.set(bld, value);
                    return new CommandResult(true, "Set " + field + " = " + value);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            return new CommandResult(false, "field not found: " + field);
        } catch (Exception e) {
            return new CommandResult(false, "error: " + e.getMessage());
        }
    }

    private CommandResult nuke(String[] args) {
        Integer x = args.length > 0 ? tryParseInt(args[0], null) : null;
        Integer y = args.length > 1 ? tryParseInt(args[1], null) : null;
        if (x == null || y == null) return new CommandResult(false, "usage: nuke <x> <y> [radius]");
        int radius = args.length > 2 ? tryParseInt(args[2], 10) : 10;
        int count = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy > radius * radius) continue;
                Tile tile = Vars.world.tile(x + dx, y + dy);
                if (tile == null) continue;
                Building b = tile.build;
                if (b != null) { b.kill(); count++; }
            }
        }
        return new CommandResult(true, "Nuked " + count + " buildings");
    }

    private static Integer tryParseInt(String s, Integer fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
    private static float tryParseFloat(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return fallback; }
    }
}