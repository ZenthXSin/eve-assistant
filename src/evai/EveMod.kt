package evai

import arc.*
import arc.util.*
import mindustry.mod.*
import evai.command.*
import evai.http.*

class EveMod : Mod() {
    private var httpServer: HttpServer? = null

    override fun init() {
        CommandRegistry.register()
        HttpServer.start()
        Log.info("[Eve] Eve Assistant Mod initialized")
        Log.info("[Eve] HTTP API: http://127.0.0.1:18090")
        Log.info("[Eve] Chat commands: /eve help")
    }
}