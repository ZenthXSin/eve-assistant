package evai.command;

import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import java.util.*;

public class ScanCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "scan": return scan(args);
            case "check": return check(args);
            case "map": {
                Map<String, Object> d = new HashMap<>();
                d.put("width", Vars.world.width());
                d.put("height", Vars.world.height());
                d.put("map", Vars.state.map != null ? Vars.state.map.name() : "none");
                d.put("wave", (int) Vars.state.wave);
                return new CommandResult(true, "", d);
            }
            default: return new CommandResult(false, "unknown scan command: " + action);
        }
    }

    private CommandResult scan(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        if (args.length > 0 && (args[0].equals("resources") || args[0].equals("liquids"))) {
            return scanResources(player, args);
        }
        int radius = args.length > 0 ? tryParseInt(args[0], 20) : 20;
        int tx = player.tileX(), ty = player.tileY();
        List<Map<String, Object>> blocks = new ArrayList<>();
        int minX = Math.max(tx - radius, 0);
        int maxX = Math.min(tx + radius, Vars.world.width() - 1);
        int minY = Math.max(ty - radius, 0);
        int maxY = Math.min(ty + radius, Vars.world.height() - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null) continue;
                Block blk = tile.block();
                if (blk == null || blk instanceof AirBlock || blk instanceof Floor || blk instanceof OverlayFloor) continue;
                Building bld = tile.build;
                Map<String, Object> m = new HashMap<>();
                m.put("x", x); m.put("y", y);
                m.put("block", blk.name);
                m.put("team", tile.team() != null ? tile.team().name : "none");
                m.put("health", bld != null ? (int) bld.health() : 0);
                blocks.add(m);
            }
        }
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> center = new HashMap<>();
        center.put("x", tx); center.put("y", ty);
        data.put("center", center);
        data.put("radius", radius);
        data.put("count", blocks.size());
        data.put("blocks", blocks);
        return new CommandResult(true, "", data);
    }

    private CommandResult scanResources(Player player, String[] args) {
        int radius = args.length > 1 ? tryParseInt(args[1], 20) : 20;
        int tx = player.tileX(), ty = player.tileY();
        List<Map<String, Object>> resources = new ArrayList<>();
        int minX = Math.max(tx - radius, 0);
        int maxX = Math.min(tx + radius, Vars.world.width() - 1);
        int minY = Math.max(ty - radius, 0);
        int maxY = Math.min(ty + radius, Vars.world.height() - 1);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null) continue;
                if (args[0].equals("resources")) {
                    Block ov = tile.overlay();
                    if (ov != null && !(ov instanceof AirBlock) && ov.itemDrop != null) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("x", x); m.put("y", y);
                        m.put("ore", ov.name);
                        m.put("item", ov.itemDrop.name);
                        resources.add(m);
                    }
                } else {
                    Floor fl = tile.floor();
                    if (fl != null && fl.liquidDrop != null) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("x", x); m.put("y", y);
                        m.put("liquid", fl.liquidDrop.name);
                        resources.add(m);
                    }
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("type", args[0]);
        data.put("count", resources.size());
        data.put("resources", resources);
        return new CommandResult(true, "", data);
    }

    private CommandResult check(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        int idx = 0;
        String field = "";
        if (args.length >= 3 && (args[0].equals("items") || args[0].equals("power") || args[0].equals("liquid")
            || args[0].equals("health") || args[0].equals("tile") || args[0].equals("enemies") || args[0].equals("allies"))) {
            field = args[0]; idx = 1;
        }
        if (field.equals("enemies") || field.equals("allies")) {
            int radius = args.length > idx ? tryParseInt(args[idx], 50) : 50;
            Team playerTeam = player.team();
            final String targetField = field;
            List<Map<String, Object>> nearby = new ArrayList<>();
            Units.nearby(player.x, player.y, radius * 2f, radius * 2f, u -> {
                boolean isAlly = u.team() == playerTeam;
                if ((targetField.equals("allies") && isAlly) || (targetField.equals("enemies") && !isAlly)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", u.type().name);
                    m.put("health", (int) u.health() + "/" + (int) u.maxHealth());
                    m.put("team", u.team().name);
                    m.put("x", (int) u.x());
                    m.put("y", (int) u.y());
                    nearby.add(m);
                }
            });
            Map<String, Object> data = new HashMap<>();
            data.put("type", field);
            data.put("count", nearby.size());
            data.put("units", nearby);
            return new CommandResult(true, "", data);
        }
        Integer x = args.length > idx ? tryParseInt(args[idx], null) : null;
        Integer y = args.length > idx + 1 ? tryParseInt(args[idx + 1], null) : null;
        if (x == null || y == null) return new CommandResult(false, "usage: check [field] <x> <y>");
        Tile tile = Vars.world.tile(x, y);
        if (tile == null) return new CommandResult(false, "tile not found");
        Building bld = tile.build;

        if (field.equals("tile")) {
            Map<String, Object> d = new HashMap<>();
            d.put("x", tile.x); d.put("y", tile.y);
            d.put("block", tile.block() != null ? tile.block().name : "air");
            d.put("floor", tile.floor() != null ? tile.floor().name : "?");
            d.put("overlay", tile.overlay() != null ? tile.overlay().name : "none");
            d.put("team", tile.team() != null ? tile.team().name : "none");
            d.put("solid", tile.solid());
            return new CommandResult(true, "", d);
        } else if (field.equals("items")) {
            if (bld == null) return new CommandResult(false, "no building");
            Map<String, Object> d = new HashMap<>();
            d.put("items", bld.items != null ? bld.items.total() : 0);
            d.put("capacity", bld.block.itemCapacity);
            return new CommandResult(true, "", d);
        } else if (field.equals("power")) {
            if (bld == null) return new CommandResult(false, "no building");
            Map<String, Object> d = new HashMap<>();
            d.put("status", bld.power != null ? bld.power.status : 0.0);
            d.put("produced", bld.power != null && bld.power.graph != null ? bld.power.graph.getPowerProduced() : 0.0);
            d.put("needed", bld.power != null && bld.power.graph != null ? bld.power.graph.getPowerNeeded() : 0.0);
            return new CommandResult(true, "", d);
        } else if (field.equals("liquid")) {
            if (bld == null) return new CommandResult(false, "no building");
            float liquidTotal = 0f;
            if (bld.liquids != null) {
                for (int li = 0; li < Vars.content.liquids().size; li++) {
                    liquidTotal += bld.liquids.get(Vars.content.liquid(li));
                }
            }
            Map<String, Object> d = new HashMap<>();
            d.put("liquids", liquidTotal);
            d.put("capacity", bld.block.liquidCapacity);
            return new CommandResult(true, "", d);
        } else if (field.equals("health")) {
            if (bld == null) return new CommandResult(false, "no building");
            Map<String, Object> d = new HashMap<>();
            d.put("health", (int) bld.health() + "/" + (int) bld.maxHealth());
            return new CommandResult(true, "", d);
        } else {
            Map<String, Object> d = new HashMap<>();
            d.put("x", tile.x); d.put("y", tile.y);
            d.put("block", tile.block() != null ? tile.block().name : "air");
            d.put("floor", tile.floor() != null ? tile.floor().name : "?");
            d.put("team", tile.team() != null ? tile.team().name : "none");
            if (bld != null) {
                Map<String, Object> bd = new HashMap<>();
                bd.put("health", (int) bld.health() + "/" + (int) bld.maxHealth());
                bd.put("enabled", bld.enabled);
                bd.put("warmup", bld.warmup());
                bd.put("efficiency", bld.efficiency);
                bd.put("rotation", bld.rotation);
                bd.put("items", bld.items != null ? bld.items.total() : 0);
                bd.put("itemCapacity", bld.block.itemCapacity);
                d.put("building", bd);
            }
            return new CommandResult(true, "", d);
        }
    }

    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    private static Integer tryParseInt(String s, Integer fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}