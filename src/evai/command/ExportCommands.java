package evai.command;

import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import arc.struct.*;
import java.util.*;

public class ExportCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: export blocks/state/tile/units");
        switch (args[0].toLowerCase()) {
            case "blocks": return exportBlocks(args);
            case "state": return exportState();
            case "tile": return exportTile(args);
            case "units": return exportUnits(args);
            default: return new CommandResult(false, "unknown export type: " + args[0]);
        }
    }

    private CommandResult exportBlocks(String[] args) {
        String filter = args.length > 1 ? args[1] : "";
        Seq<Block> raw = Vars.content.blocks();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < raw.size; i++) {
            Block b = raw.get(i);
            if (filter.isEmpty() || b.name.contains(filter)) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", b.name);
                m.put("type", b.getClass().getSimpleName());
                m.put("health", b.health);
                m.put("size", b.size);
                m.put("itemCapacity", b.itemCapacity);
                m.put("hasPower", b.hasPower);
                m.put("hasLiquids", b.hasLiquids);
                m.put("hasItems", b.hasItems);
                list.add(m);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        data.put("blocks", list);
        return new CommandResult(true, "", data);
    }

    private CommandResult exportState() {
        Map<String, Object> data = new HashMap<>();
        data.put("wave", (int) Vars.state.wave);
        data.put("map", Vars.state.map != null ? Vars.state.map.name() : "none");
        Map<String, Object> rules = new HashMap<>();
        rules.put("infiniteResources", Vars.state.rules.infiniteResources);
        rules.put("instantBuild", Vars.state.rules.instantBuild);
        rules.put("buildSpeedMultiplier", Vars.state.rules.buildSpeedMultiplier);
        data.put("rules", rules);
        return new CommandResult(true, "", data);
    }

    private CommandResult exportTile(String[] args) {
        if (args.length < 3) return new CommandResult(false, "usage: export tile <x> <y>");
        int x = tryParseInt(args[1], -1), y = tryParseInt(args[2], -1);
        if (x < 0 || y < 0) return new CommandResult(false, "invalid coordinates");
        Tile tile = Vars.world.tile(x, y);
        if (tile == null) return new CommandResult(false, "tile not found");
        Map<String, Object> data = new HashMap<>();
        data.put("x", tile.x); data.put("y", tile.y);
        data.put("block", tile.block() != null ? tile.block().name : "air");
        data.put("floor", tile.floor() != null ? tile.floor().name : "?");
        data.put("overlay", tile.overlay() != null ? tile.overlay().name : "none");
        data.put("team", tile.team() != null ? tile.team().name : "none");
        data.put("solid", tile.solid());
        return new CommandResult(true, "", data);
    }

    private CommandResult exportUnits(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        float radius = args.length > 1 ? tryParseFloat(args[1], 100f) : 100f;
        List<Map<String, Object>> list = new ArrayList<>();
        Units.nearby(player.x, player.y, radius * 2, radius * 2, u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("name", u.type().name);
            m.put("health", (int) u.health() + "/" + (int) u.maxHealth());
            m.put("team", u.team().name);
            m.put("x", (int) u.x());
            m.put("y", (int) u.y());
            list.add(m);
        });
        Map<String, Object> data = new HashMap<>();
        data.put("count", list.size());
        data.put("units", list);
        return new CommandResult(true, "", data);
    }

    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
    private static float tryParseFloat(String s, float fallback) {
        try { return Float.parseFloat(s); } catch (NumberFormatException e) { return fallback; }
    }
}