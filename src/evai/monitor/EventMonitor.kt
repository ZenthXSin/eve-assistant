package evai.monitor

import arc.*
import arc.util.*
import mindustry.game.*
import java.util.concurrent.*

object EventMonitor {
    private val listeners = mutableMapOf<String, () -> Unit>()
    private val eventClasses = mutableMapOf<String, Class<*>>()

    init {
        for (clazz in EventType::class.java.classes) {
            val name = clazz.simpleName.removeSuffix("Event")
            eventClasses[name] = clazz
        }
    }

    fun listen(name: String): Boolean {
        val eventClass = eventClasses[name]
            ?: run { Log.warn("[Eve] Unknown event: $name"); return false }
        if (listeners.containsKey(name)) return true // already listening
        val handler = {
            Log.info("[Eve] Event fired: ${name}Event")
        }
        Events.on(eventClass, handler)
        listeners[name] = handler
        return true
    }

    fun listenAll(): Int {
        var count = 0
        for ((name, clazz) in eventClasses) {
            if (!listeners.containsKey(name)) {
                val handler = { Log.info("[Eve] Event fired: ${name}Event") }
                Events.on(clazz, handler)
                listeners[name] = handler
                count++
            }
        }
        return count
    }

    fun stop(name: String) {
        listeners.remove(name)
    }

    fun stopAll() {
        listeners.clear()
    }

    fun list(): List<String> = listeners.keys.sorted()
}