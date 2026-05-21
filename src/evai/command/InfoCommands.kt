package evai.command

import mindustry.*
import mindustry.ctype.*
import evai.command.CommandRegistry.CommandHandler

class InfoCommands : CommandHandler {
    override fun handle(action: String, args: Array<String>): CommandResult = when (action) {
        "list" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: list <type> [filter]; types: blocks/items/liquids/units/bullets/status/turrets/storage/crafter/walls/conveyors/drills/mod")
            val type = args[0].lowercase()
            val filter = if (args.size > 1) args[1].lowercase() else ""
            val content = Vars.content
            when (type) {
                "blocks" -> {
                    val blocks = content.blocks().filter { filter.isBlank() || it.name.contains(filter) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "localizedName" to it.localizedName) }))
                }
                "items" -> listContent(content.items(), filter)
                "liquids" -> listContent(content.liquids(), filter)
                "units" -> listContent(content.units(), filter)
                "bullets" -> listContent(content.bullets(), filter)
                "status" -> listContent(content.statusEffects(), filter)
                "turrets" -> {
                    val blocks = content.blocks().filter { it.hasTurrets && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "localizedName" to it.localizedName) }))
                }
                "storage" -> {
                    val blocks = content.blocks().filter { it is mindustry.world.blocks.storage.StorageBlock && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "itemCapacity" to it.itemCapacity) }))
                }
                "crafter" -> {
                    val blocks = content.blocks().filter { it is mindustry.world.blocks.production.GenericCrafter && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "localizedName" to it.localizedName) }))
                }
                "walls" -> {
                    val blocks = content.blocks().filter { it is mindustry.world.blocks.defense.Wall && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name, "health" to it.health) }))
                }
                "conveyors" -> {
                    val blocks = content.blocks().filter { it is mindustry.world.blocks.distribution.Conveyor && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name) }))
                }
                "drills" -> {
                    val blocks = content.blocks().filter { it is mindustry.world.blocks.production.Drill && (filter.isBlank() || it.name.contains(filter)) }
                    CommandResult(true, data = mapOf("count" to blocks.size, "blocks" to blocks.map { mapOf("name" to it.name) }))
                }
                "mod" -> {
                    val mods = Vars.mods.list()
                    CommandResult(true, data = mapOf("mods" to mods.map { mapOf("name" to it.meta.name, "displayName" to it.meta.displayName, "version" to (it.meta.version ?: "?"), "author" to (it.meta.author ?: "?")) }))
                }
                else -> CommandResult(false, "unknown type: $type")
            }
        }
        "info" -> {
            if (args.isEmpty()) return CommandResult(false, "usage: info <name>")
            val name = args[0].lowercase()
            val content = Vars.content
            for (type in ContentType.all()) {
                val c = content.getByName(type, name)
                if (c != null) {
                    return try {
                        val fields = mutableMapOf<String, Any?>()
                        val clazz = c.javaClass
                        while (clazz != Any::class.java) {
                            for (f in clazz.declaredFields) {
                                f.isAccessible = true
                                try {
                                    fields[f.name] = f.get(c)?.toString() ?: "null"
                                } catch (_: Exception) {}
                            }
                            clazz.superclass
                        }
                        CommandResult(true, data = mapOf("name" to c.name, "type" to c.javaClass.simpleName, "contentType" to c.contentType().name, "fields" to fields))
                    } catch (e: Exception) {
                        CommandResult(false, "error reading fields: ${e.message}")
                    }
                }
            }
            CommandResult(false, "content not found: $name")
        }
        else -> CommandResult(false, "unknown info command: $action")
    }

    private fun listContent(list: Seq<out UnlockableContent>, filter: String): CommandResult {
        val items = list.filter { filter.isBlank() || it.name.contains(filter) }
        return CommandResult(true, data = mapOf("count" to items.size, "items" to items.map { mapOf("name" to it.name, "localizedName" to it.localizedName) }))
    }
}

// Seq alias for Mindustry's arc.struct.Seq
typealias Seq<T> = arc.struct.Seq<T>