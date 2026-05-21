package evai.command

import arc.*
import mindustry.game.*
import evai.command.CommandRegistry.CommandHandler
import evai.monitor.*

class EventCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "listen" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: listen <event>/all/stop/list")
            when (args[0].lowercase()) {
                "all" -> {
                    val count = EventMonitor.listenAll()
                    CommandResult(true, "Listening to all $count events")
                }
                "stop" -> {
                    if (args.size > 1) {
                        EventMonitor.stop(args[1])
                        CommandResult(true, "Stopped listening to ${args[1]}")
                    } else {
                        EventMonitor.stopAll()
                        CommandResult(true, "Stopped all listeners")
                    }
                }
                "list" -> {
                    val events = EventMonitor.list()
                    CommandResult(true, data = mapOf("listening" to events))
                }
                else -> {
                    EventMonitor.listen(args[0])
                    CommandResult(true, "Listening to ${args[0]}Event")
                }
            }
        }
        "fire" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: fire <event> [args]")
            val eventName = args[0]
            val eventArgs = args.drop(1).toTypedArray()
            try {
                val eventClass = EventType::class.java.classes.find { it.simpleName == "${eventName}Event" }
                    ?: return CommandResult(false, "event not found: ${eventName}Event")
                val instance = eventClass.getDeclaredConstructor().newInstance()
                Events.fire(instance)
                CommandResult(true, "Fired ${eventName}Event")
            } catch (e: Exception) {
                CommandResult(false, "error firing event: ${e.message}")
            }
        }
        else -> CommandResult(false, "unknown event command: $action")
    }
}