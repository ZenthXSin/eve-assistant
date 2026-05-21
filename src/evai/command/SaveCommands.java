package evai.command;

import mindustry.*;
import mindustry.game.Saves.SaveSlot;
import arc.struct.*;
import java.util.*;

public class SaveCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "save": return save(args);
            case "load": return load(args);
            default: return new CommandResult(false, "unknown save command: " + action);
        }
    }

    private CommandResult save(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: save <name>/list/delete/current");
        switch (args[0].toLowerCase()) {
            case "list": {
                Seq<SaveSlot> slots = Vars.control.saves.getSaveSlots();
                List<Map<String, Object>> list = new ArrayList<>();
                for (int i = 0; i < slots.size; i++) {
                    SaveSlot s = slots.get(i);
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", s.getName());
                    m.put("date", s.getDate());
                    list.add(m);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("saves", list);
                return new CommandResult(true, "", data);
            }
            case "delete": {
                if (args.length < 2) return new CommandResult(false, "usage: save delete <name>");
                String name = join(args, 1);
                Seq<SaveSlot> slots = Vars.control.saves.getSaveSlots();
                for (int i = 0; i < slots.size; i++) {
                    if (slots.get(i).getName().equals(name)) {
                        slots.get(i).file.delete();
                        return new CommandResult(true, "Deleted save '" + name + "'");
                    }
                }
                return new CommandResult(false, "save not found: " + name);
            }
            case "current": {
                SaveSlot cur = Vars.control.saves.getCurrent();
                Map<String, Object> data = new HashMap<>();
                data.put("current", cur != null ? cur.getName() : "none");
                return new CommandResult(true, "", data);
            }
            default: {
                String name = join(args, 0);
                try {
                    Vars.control.saves.addSave(name);
                    return new CommandResult(true, "Saved as '" + name + "'");
                } catch (Exception e) {
                    return new CommandResult(false, "Save failed: " + e.getMessage());
                }
            }
        }
    }

    private CommandResult load(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: load <name>");
        String name = join(args, 0);
        Seq<SaveSlot> slots = Vars.control.saves.getSaveSlots();
        for (int i = 0; i < slots.size; i++) {
            if (slots.get(i).getName().equals(name)) {
                try {
                    slots.get(i).load();
                    return new CommandResult(true, "Loaded '" + name + "'");
                } catch (Exception e) {
                    return new CommandResult(false, "Load failed: " + e.getMessage());
                }
            }
        }
        return new CommandResult(false, "save not found: " + name);
    }

    private static String join(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(" ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}