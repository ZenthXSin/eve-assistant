package evai.monitor;

import arc.*;
import arc.util.*;
import mindustry.game.*;
import java.util.*;

public class EventMonitor {
    private static final Set<String> activeListeners = new HashSet<>();
    private static final Map<String, Class<?>> eventClasses = new HashMap<>();

    static {
        for (Class<?> clazz : EventType.class.getClasses()) {
            String name = clazz.getSimpleName();
            if (name.endsWith("Event")) {
                eventClasses.put(name.substring(0, name.length() - "Event".length()), clazz);
            }
        }
    }

    public static boolean listen(String name) {
        Class<?> clazz = eventClasses.get(name);
        if (clazz == null) return false;
        if (activeListeners.contains(name)) return true;
        Events.on(clazz, e -> Log.info("[Eve] Event fired: " + name + "Event"));
        activeListeners.add(name);
        return true;
    }

    public static int listenAll() {
        int count = 0;
        for (String name : eventClasses.keySet()) {
            if (!activeListeners.contains(name)) {
                listen(name);
                count++;
            }
        }
        return count;
    }

    public static void stop(String name) {
        activeListeners.remove(name);
    }

    public static void stopAll() {
        activeListeners.clear();
    }

    public static List<String> list() {
        List<String> result = new ArrayList<>(activeListeners);
        Collections.sort(result);
        return result;
    }
}