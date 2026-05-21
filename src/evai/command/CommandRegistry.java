package evai.command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();

    @FunctionalInterface
    public interface CommandHandler {
        CommandResult handle(String action, String[] args);
    }

    public static void register() {
        handlers.put("help", new BaseCommands());
        handlers.put("status", new BaseCommands());
        handlers.put("whereami", new BaseCommands());
        handlers.put("clear", new BaseCommands());
        handlers.put("version", new BaseCommands());
        handlers.put("list", new InfoCommands());
        handlers.put("info", new InfoCommands());
        handlers.put("scan", new ScanCommands());
        handlers.put("check", new ScanCommands());
        handlers.put("map", new ScanCommands());
        handlers.put("debug", new DebugCommands());
        handlers.put("set", new SetCommands());
        handlers.put("nuke", new SetCommands());
        handlers.put("build", new BuildCommands());
        handlers.put("break", new BuildCommands());
        handlers.put("schem", new SchemCommands());
        handlers.put("spawn", new SpawnCommands());
        handlers.put("control", new SpawnCommands());
        handlers.put("kill", new SpawnCommands());
        handlers.put("listen", new EventCommands());
        handlers.put("fire", new EventCommands());
        handlers.put("js", new JsCommands());
        handlers.put("logic", new JsCommands());
        handlers.put("eval", new JsCommands());
        handlers.put("save", new SaveCommands());
        handlers.put("load", new SaveCommands());
        handlers.put("export", new ExportCommands());
    }

    public static CommandResult execute(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return new CommandResult(false, "empty command");
        String[] parts = cmd.trim().split("\\s+");
        String action = parts[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        CommandHandler handler = handlers.get(action);
        if (handler == null) return new CommandResult(false, "unknown command: " + action);
        return handler.handle(action, args);
    }
}