package evai.command;

import mindustry.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import arc.struct.*;
import mindustry.game.*;
import java.util.*;

public class BuildCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "build": return build(args);
            case "break": return breakCmd(args);
            default: return new CommandResult(false, "unknown build command: " + action);
        }
    }

    private Block resolveBlock(String name) {
        Seq<Block> blocks = Vars.content.blocks();
        for (int i = 0; i < blocks.size; i++) {
            Block b = blocks.get(i);
            if (b.name.equals(name) || b.name.replace("-", "").equals(name.replace("-", ""))) return b;
        }
        return null;
    }

    private CommandResult build(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: build <block> <x> <y> [rotate]");
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        if (args[0].equals("area")) return buildArea(player, args);
        if (args[0].equals("line")) return buildLine(player, args);
        if (args[0].equals("ring")) return buildRing(player, args);
        Block block = resolveBlock(args[0]);
        if (block == null) return new CommandResult(false, "block not found: " + args[0]);
        Integer x = args.length > 1 ? tryParseInt(args[1], null) : null;
        Integer y = args.length > 2 ? tryParseInt(args[2], null) : null;
        if (x == null || y == null) return new CommandResult(false, "invalid coordinates");
        int rotate = args.length > 3 ? tryParseInt(args[3], 0) : 0;
        player.unit().addBuild(new BuildPlan(x, y, rotate, block));
        return new CommandResult(true, "Queued build " + block.name);
    }

    private CommandResult buildArea(Player player, String[] args) {
        if (args.length < 6) return new CommandResult(false, "usage: build area <block> <x1> <y1> <x2> <y2>");
        Block block = resolveBlock(args[1]);
        if (block == null) return new CommandResult(false, "block not found: " + args[1]);
        int x1 = tryParseInt(args[2], 0), y1 = tryParseInt(args[3], 0);
        int x2 = tryParseInt(args[4], 0), y2 = tryParseInt(args[5], 0);
        int minX = Math.max(Math.min(x1, x2), 0);
        int maxX = Math.min(Math.max(x1, x2), Vars.world.width() - 1);
        int minY = Math.max(Math.min(y1, y2), 0);
        int maxY = Math.min(Math.max(y1, y2), Vars.world.height() - 1);
        int count = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                player.unit().addBuild(new BuildPlan(x, y, 0, block));
                count++;
            }
        }
        return new CommandResult(true, "Queued " + count + " plans for " + block.name);
    }

    private CommandResult buildLine(Player player, String[] args) {
        if (args.length < 6) return new CommandResult(false, "usage: build line <block> <x1> <y1> <x2> <y2>");
        Block block = resolveBlock(args[1]);
        if (block == null) return new CommandResult(false, "block not found");
        int x1 = tryParseInt(args[2], 0), y1 = tryParseInt(args[3], 0);
        int x2 = tryParseInt(args[4], 0), y2 = tryParseInt(args[5], 0);
        int dx = x2 > x1 ? 1 : (x2 < x1 ? -1 : 0);
        int dy = y2 > y1 ? 1 : (y2 < y1 ? -1 : 0);
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        int count = 0;
        for (int i = 0; i <= steps; i++) {
            int px = x1 + dx * i, py = y1 + dy * i;
            if (px >= 0 && px < Vars.world.width() && py >= 0 && py < Vars.world.height()) {
                player.unit().addBuild(new BuildPlan(px, py, 0, block));
                count++;
            }
        }
        return new CommandResult(true, "Queued " + count + " plans");
    }

    private CommandResult buildRing(Player player, String[] args) {
        if (args.length < 5) return new CommandResult(false, "usage: build ring <block> <cx> <cy> <radius>");
        Block block = resolveBlock(args[1]);
        if (block == null) return new CommandResult(false, "block not found");
        int cx = tryParseInt(args[2], 0), cy = tryParseInt(args[3], 0);
        int r = tryParseInt(args[4], 0);
        int count = 0;
        for (int angle = 0; angle < 360; angle += 15) {
            double rad = Math.toRadians(angle);
            int px = (int) Math.round(cx + r * Math.cos(rad));
            int py = (int) Math.round(cy + r * Math.sin(rad));
            px = Math.max(0, Math.min(Vars.world.width() - 1, px));
            py = Math.max(0, Math.min(Vars.world.height() - 1, py));
            player.unit().addBuild(new BuildPlan(px, py, 0, block));
            count++;
        }
        return new CommandResult(true, "Queued " + count + " plans");
    }

    private CommandResult breakCmd(String[] args) {
        Player player = Vars.player;
        if (player == null) return new CommandResult(false, "no player");
        if (args.length > 0 && args[0].equals("area")) {
            if (args.length < 5) return new CommandResult(false, "usage: break area <x1> <y1> <x2> <y2>");
            int x1 = tryParseInt(args[1], 0), y1 = tryParseInt(args[2], 0);
            int x2 = tryParseInt(args[3], 0), y2 = tryParseInt(args[4], 0);
            int minX = Math.max(Math.min(x1, x2), 0), maxX = Math.min(Math.max(x1, x2), Vars.world.width() - 1);
            int minY = Math.max(Math.min(y1, y2), 0), maxY = Math.min(Math.max(y1, y2), Vars.world.height() - 1);
            int count = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    Tile tile = Vars.world.tile(x, y);
                    if (tile != null && tile.block() != null && !(tile.block() instanceof AirBlock) && !(tile.block() instanceof Floor)) {
                        player.unit().addBuild(new BuildPlan(x, y));
                        count++;
                    }
                }
            }
            return new CommandResult(true, "Queued " + count + " break plans");
        }
        Integer x = args.length > 0 ? tryParseInt(args[0], null) : null;
        Integer y = args.length > 1 ? tryParseInt(args[1], null) : null;
        if (x == null || y == null) return new CommandResult(false, "usage: break <x> <y>");
        player.unit().addBuild(new BuildPlan(x, y));
        return new CommandResult(true, "Queued break at (" + x + ", " + y + ")");
    }

    private static int tryParseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
    private static Integer tryParseInt(String s, Integer fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}