package evai.command;

import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import arc.struct.*;
import java.util.*;

public class SchemCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: schem <create/place/list/delete/rename/export/import>");
        switch (args[0].toLowerCase()) {
            case "create": return create(args);
            case "place": return place(args);
            case "list": return list();
            case "delete": return delete(args);
            case "rename": return rename(args);
            case "export": return export(args);
            case "import": return imp(args);
            default: return new CommandResult(false, "unknown schem subcommand: " + args[0]);
        }
    }

    private Schematic find(String name) {
        Seq<Schematic> all = Vars.schematics.all();
        for (int i = 0; i < all.size; i++) {
            if (name.equals(all.get(i).tags.get("name"))) return all.get(i);
        }
        return null;
    }

    private CommandResult create(String[] args) {
        if (args.length < 6) return new CommandResult(false, "usage: schem create <name> <x1> <y1> <x2> <y2>");
        String name = args[1];
        int x1 = tryParseInt(args[2], 0), y1 = tryParseInt(args[3], 0);
        int x2 = tryParseInt(args[4], 0), y2 = tryParseInt(args[5], 0);
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        Seq<Schematic.Stile> tiles = new Seq<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null) continue;
                Block blk = tile.block();
                if (blk == null || blk instanceof AirBlock || blk instanceof Floor || blk instanceof OverlayFloor) continue;
                Building bld = tile.build;
                Object config = bld != null ? bld.config() : null;
                tiles.add(new Schematic.Stile(blk, x - minX, y - minY, config, (byte) 0));
            }
        }
        Schematic schem = new Schematic(tiles, new StringMap(), maxX - minX + 1, maxY - minY + 1);
        schem.tags.put("name", name);
        Vars.schematics.add(schem);
        return new CommandResult(true, "Created schematic '" + name + "' with " + tiles.size + " tiles");
    }

    private CommandResult place(String[] args) {
        if (args.length < 4) return new CommandResult(false, "usage: schem place <name> <x> <y>");
        Schematic schem = find(args[1]);
        if (schem == null) return new CommandResult(false, "schematic not found: " + args[1]);
        int x = tryParseInt(args[2], 0), y = tryParseInt(args[3], 0);
        Team team = Vars.player != null ? Vars.player.team() : Team.sharded;
        Schematics.place(schem, x, y, team, true);
        return new CommandResult(true, "Placed schematic");
    }

    private CommandResult list() {
        Seq<Schematic> all = Vars.schematics.all();
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < all.size; i++) {
            Schematic s = all.get(i);
            Map<String, Object> m = new HashMap<>();
            m.put("name", s.tags.get("name", "unnamed"));
            m.put("width", s.width);
            m.put("height", s.height);
            m.put("tiles", s.tiles.size);
            list.add(m);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("schematics", list);
        return new CommandResult(true, "", data);
    }

    private CommandResult delete(String[] args) {
        if (args.length < 2) return new CommandResult(false, "usage: schem delete <name>");
        Schematic schem = find(args[1]);
        if (schem == null) return new CommandResult(false, "not found: " + args[1]);
        Vars.schematics.remove(schem);
        return new CommandResult(true, "Deleted schematic");
    }

    private CommandResult rename(String[] args) {
        if (args.length < 3) return new CommandResult(false, "usage: schem rename <old> <new>");
        Schematic schem = find(args[1]);
        if (schem == null) return new CommandResult(false, "not found: " + args[1]);
        schem.tags.put("name", args[2]);
        return new CommandResult(true, "Renamed to '" + args[2] + "'");
    }

    private CommandResult export(String[] args) {
        if (args.length < 2) return new CommandResult(false, "usage: schem export <name>");
        Schematic schem = find(args[1]);
        if (schem == null) return new CommandResult(false, "not found: " + args[1]);
        String base64 = Vars.schematics.writeBase64(schem);
        Map<String, Object> data = new HashMap<>();
        data.put("name", args[1]);
        data.put("base64", base64);
        return new CommandResult(true, "", data);
    }

    private CommandResult imp(String[] args) {
        if (args.length < 2) return new CommandResult(false, "usage: schem import <base64> [name]");
        Schematic schem = Schematics.readBase64(args[1]);
        if (args.length >= 3) schem.tags.put("name", args[2]);
        Vars.schematics.add(schem);
        return new CommandResult(true, "Imported schematic");
    }

    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}