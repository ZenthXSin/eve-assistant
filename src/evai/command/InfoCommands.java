package evai.command;

import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.distribution.*;
import mindustry.entities.bullet.*;
import mindustry.mod.Mods.*;
import arc.struct.*;
import java.util.*;

public class InfoCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "list": return list(args);
            case "info": return info(args);
            default: return new CommandResult(false, "unknown info command: " + action);
        }
    }

    private CommandResult list(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: list <type> [filter]");
        String type = args[0].toLowerCase();
        String filter = args.length > 1 ? args[1].toLowerCase() : "";

        switch (type) {
            case "blocks": return listBlocks(Vars.content.blocks(), filter);
            case "items": return listUnlockable(Vars.content.items(), filter);
            case "liquids": return listUnlockable(Vars.content.liquids(), filter);
            case "units": return listUnlockable(Vars.content.units(), filter);
            case "bullets": {
                Seq<BulletType> bulletSeq = Vars.content.bullets();
                if (bulletSeq == null) return new CommandResult(false, "no bullets data");
                List<Map<String, Object>> names = new ArrayList<>();
                for (int i = 0; i < bulletSeq.size; i++) {
                    BulletType b = bulletSeq.get(i);
                    String bName = b.getClass().getSimpleName() + "#" + b.id;
                    if (filter.isEmpty() || bName.contains(filter)) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("name", bName);
                        m.put("id", (int) b.id);
                        names.add(m);
                    }
                }
                Map<String, Object> data = new HashMap<>();
                data.put("count", names.size());
                data.put("bullets", names);
                return new CommandResult(true, "", data);
            }
            case "status": return listUnlockable(Vars.content.statusEffects(), filter);
            case "turrets": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof Turret);
            case "storage": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof StorageBlock);
            case "crafter": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof GenericCrafter);
            case "walls": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof Wall);
            case "conveyors": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof Conveyor);
            case "drills": return listBlockType(Vars.content.blocks(), filter, b -> b instanceof Drill);
            case "mod": {
                Seq<LoadedMod> mods = Vars.mods.list();
                List<Map<String, Object>> names = new ArrayList<>();
                for (int i = 0; i < mods.size; i++) {
                    LoadedMod m = mods.get(i);
                    Map<String, Object> mm = new HashMap<>();
                    mm.put("name", m.meta.name);
                    mm.put("displayName", m.meta.displayName);
                    mm.put("version", m.meta.version != null ? m.meta.version : "?");
                    mm.put("author", m.meta.author != null ? m.meta.author : "?");
                    names.add(mm);
                }
                Map<String, Object> data = new HashMap<>();
                data.put("mods", names);
                return new CommandResult(true, "", data);
            }
            default: return new CommandResult(false, "unknown type: " + type);
        }
    }

    private CommandResult listBlocks(Seq<Block> blocks, String filter) {
        List<Map<String, Object>> names = new ArrayList<>();
        for (int i = 0; i < blocks.size; i++) {
            Block b = blocks.get(i);
            if (filter.isEmpty() || b.name.contains(filter)) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", b.name);
                m.put("localizedName", b.localizedName);
                names.add(m);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", names.size());
        data.put("blocks", names);
        return new CommandResult(true, "", data);
    }

    private CommandResult listBlockType(Seq<Block> blocks, String filter, java.util.function.Predicate<Block> check) {
        List<Map<String, Object>> names = new ArrayList<>();
        for (int i = 0; i < blocks.size; i++) {
            Block b = blocks.get(i);
            if (check.test(b) && (filter.isEmpty() || b.name.contains(filter))) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", b.name);
                m.put("localizedName", b.localizedName);
                names.add(m);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", names.size());
        data.put("blocks", names);
        return new CommandResult(true, "", data);
    }

    private CommandResult listUnlockable(Seq<? extends UnlockableContent> seq, String filter) {
        List<Map<String, Object>> names = new ArrayList<>();
        for (int i = 0; i < seq.size; i++) {
            UnlockableContent c = seq.get(i);
            if (filter.isEmpty() || c.name.contains(filter)) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", c.name);
                m.put("localizedName", c.localizedName);
                names.add(m);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", names.size());
        data.put("items", names);
        return new CommandResult(true, "", data);
    }

    private CommandResult info(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: info <name>");
        String name = args[0].toLowerCase();
        for (ContentType type : ContentType.all) {
            Seq<?> raw = Vars.content.getBy(type);
            if (raw == null) continue;
            for (int i = 0; i < raw.size; i++) {
                Object obj = raw.get(i);
                if (obj instanceof Content) {
                    Content cobj = (Content) obj;
                    String cname;
                    if (cobj instanceof MappableContent) {
                        cname = ((MappableContent) cobj).name;
                    } else {
                        cname = cobj.getClass().getSimpleName() + "#" + cobj.id;
                    }
                    if (!cname.equals(name)) continue;
                    Map<String, Object> fields = new HashMap<>();
                    Class<?> clazz = cobj.getClass();
                    while (clazz != null && clazz != Object.class) {
                        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                            f.setAccessible(true);
                            try {
                                Object val = f.get(cobj);
                                fields.put(f.getName(), val != null ? val.toString() : "null");
                            } catch (Exception ignored) {}
                        }
                        clazz = clazz.getSuperclass();
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", cname);
                    data.put("type", cobj.getClass().getSimpleName());
                    data.put("contentType", type.name());
                    data.put("fields", fields);
                    return new CommandResult(true, "", data);
                }
            }
        }
        return new CommandResult(false, "content not found: " + name);
    }
}