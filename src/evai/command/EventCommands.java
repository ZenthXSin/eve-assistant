package evai.command;

import arc.*;
import mindustry.game.*;
import evai.monitor.*;
import java.util.*;

public class EventCommands implements CommandRegistry.CommandHandler {
    @Override
    public CommandResult handle(String action, String[] args) {
        switch (action) {
            case "listen": return listen(args);
            case "fire": return fire(args);
            default: return new CommandResult(false, "unknown event command: " + action);
        }
    }

    private CommandResult listen(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: listen <event>/all/stop/list");
        switch (args[0].toLowerCase()) {
            case "all": {
                int count = EventMonitor.listenAll();
                return new CommandResult(true, "Listening to " + count + " events");
            }
            case "stop": {
                if (args.length > 1) {
                    EventMonitor.stop(args[1]);
                    return new CommandResult(true, "Stopped listening to " + args[1]);
                }
                EventMonitor.stopAll();
                return new CommandResult(true, "Stopped all listeners");
            }
            case "list": {
                List<String> events = EventMonitor.list();
                Map<String, Object> data = new HashMap<>();
                data.put("listening", events);
                return new CommandResult(true, "", data);
            }
            default: {
                boolean ok = EventMonitor.listen(args[0]);
                return ok ? new CommandResult(true, "Listening to " + args[0]) : new CommandResult(false, "Unknown event: " + args[0]);
            }
        }
    }

    private CommandResult fire(String[] args) {
        if (args.length == 0) return new CommandResult(false, "usage: fire <event>");
        String name = args[0];
        try {
            for (Class<?> clazz : EventType.class.getClasses()) {
                if (clazz.getSimpleName().equals(name + "Event")) {
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    Events.fire(instance);
                    return new CommandResult(true, "Fired " + name + "Event");
                }
            }
            return new CommandResult(false, "Event not found: " + name + "Event");
        } catch (Exception e) {
            return new CommandResult(false, "Error: " + e.getMessage());
        }
    }
}