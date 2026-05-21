package evai

import arc.util.*
import mindustry.mod.*
import evai.command.*

class EveMod : Mod() {
    override fun init() {
        CommandRegistry.register()
        evai.http.HttpServer.start()
        Log.info("[Eve] Eve Assistant Mod v1.0 initialized")
        Log.info("[Eve] HTTP API: http://127.0.0.1:18090")
    }
}