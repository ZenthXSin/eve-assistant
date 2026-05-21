package evai.command;

import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import java.util.*;

public class BaseCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "help": return help(args);
            case "status": return status();
            case "whereami": return whereami();
            case "clear": return new CommandResult(true, "chat cleared");
            case "version": return new CommandResult(true, "Eve Assistant Mod v1.0");
            default: return new CommandResult(false, "unknown base command: " + action);
        }
    }

    private CommandResult help(String[] args) {
        return new CommandResult(true, "Eve Assistant Mod v1.0\n" +
            "Commands: help, status, whereami, clear, version\n" +
            "list <type> [filter], info <name>\n" +
            "scan [r], scan resources/liquids, check <field> <x> <y>, map\n" +
            "debug <setting> [value]\n" +
            "set <x> <y> <field> <value>, nuke <x> <y> [r]\n" +
            "build <block> <x> <y> [r], build area/line/ring\n" +
            "break <x> <y>, break area <x1> <y1> <x2> <y2>\n" +
            "schem <create/place/list/delete/rename/export/import>\n" +
            "spawn <unit> <x> <y> [team] [amt]\n" +
            "control [unit], kill <all/enemies/allies> [r]\n" +
            "listen/fire <event>, js/logic/eval <code>\n" +
            "save/load <name>, save list/delete/current\n" +
            "export blocks/state/tile/units");
    }

    private CommandResult status() {
        Map<String, Object> data = new HashMap<>();
        data.put("map", Vars.state.map != null ? Vars.state.map.name() : "none");
        data.put("wave", (int) Vars.state.wave);
        data.put("team", Vars.player != null ? Vars.player.team().name : "none");
        data.put("player", Vars.player != null ? Vars.player.name : "none");
        data.put("tiles", Vars.world.width() + "x" + Vars.world.height());
        data.put("mode", Vars.state.rules != null ? Vars.state.rules.mode().name() : "unknown");
        return new CommandResult(true, "", data);
    }

    private CommandResult whereami() {
        if (Vars.player == null) return new CommandResult(false, "no player");
        Player p = Vars.player;
        Tile tile = Vars.world.tileWorld(p.x, p.y);
        Map<String, Object> data = new HashMap<>();
        data.put("x", (int) p.x);
        data.put("y", (int) p.y);
        data.put("tileX", tile != null ? tile.x : -1);
        data.put("tileY", tile != null ? tile.y : -1);
        data.put("block", tile != null && tile.block() != null ? tile.block().name : "none");
        data.put("floor", tile != null && tile.floor() != null ? tile.floor().name : "none");
        return new CommandResult(true, "", data);
    }
}