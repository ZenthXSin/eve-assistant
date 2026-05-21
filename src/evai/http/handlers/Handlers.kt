package evai.http.handlers

import com.sun.net.httpserver.*
import evai.command.*
import evai.http.*
import mindustry.*
import mindustry.core.*
import mindustry.game.*
import mindustry.world.*
import java.net.*
import java.nio.charset.*

class CommandHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        try {
            val query = exchange.requestURI.query ?: ""
            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                URLDecoder.decode(parts[0], "UTF-8") to if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
            }
            val cmd = params["cmd"] ?: ""
            if (cmd.isBlank()) {
                sendJson(exchange, 400, """{"ok":false,"error":"missing cmd parameter"}""")
                return
            }
            val result = CommandRegistry.execute(cmd)
            sendJson(exchange, if (result.ok) 200 else 400, result.toJson())
        } catch (e: Exception) {
            sendJson(exchange, 500, """{"ok":false,"error":"${e.message}"}""")
        }
    }

    private fun sendJson(exchange: HttpExchange, status: Int, json: String) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
}

class StatusHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val state = Vars.state
        val world = Vars.world
        val json = JsonUtil.buildObject(
            "ok" to true,
            "map" to (state.map?.name() ?: "none"),
            "wave" to state.wave,
            "team" to (Vars.player?.team()?.name ?: "none"),
            "players" to (Vars.player?.name ?: "none"),
            "tiles" to "${world.width()}x${world.height()}",
            "gamemode" to (state.rules?.mode()?.name ?: "unknown"),
            "infiniteResources" to (state.rules?.infiniteResources ?: false),
            "instantBuild" to (state.rules?.instantBuild ?: false),
            "timestamp" to System.currentTimeMillis()
        )
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
}

class InspectHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        try {
            val query = exchange.requestURI.query ?: ""
            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                URLDecoder.decode(parts[0], "UTF-8") to if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
            }
            val x = params["x"]?.toIntOrNull() ?: run {
                sendJson(exchange, 400, """{"ok":false,"error":"missing/invalid x"}""")
                return
            }
            val y = params["y"]?.toIntOrNull() ?: run {
                sendJson(exchange, 400, """{"ok":false,"error":"missing/invalid y"}""")
                return
            }
            val tile = Vars.world.tile(x, y) ?: run {
                sendJson(exchange, 404, """{"ok":false,"error":"tile not found"}""")
                return
            }
            val building = tile.build()
            val data = mutableMapOf<String, Any?>(
                "x" to tile.x, "y" to tile.y,
                "block" to tile.block()?.name,
                "floor" to tile.floor()?.name,
                "overlay" to tile.overlay()?.name,
                "team" to tile.team()?.name,
                "solid" to tile.solid(),
                "passable" to tile.passable()
            )
            if (building != null) {
                data["building"] = mapOf(
                    "health" to "${building.health}/${building.maxHealth}",
                    "enabled" to building.enabled,
                    "warmup" to building.warmup,
                    "efficiency" to building.efficiency,
                    "rotation" to building.rotation,
                    "items" to (if (building.items != null) "${building.items.total()}/${building.block.itemCapacity}" else "none"),
                    "power.status" to (building.power?.status ?: "none"),
                    "power.graph" to (building.power?.graph?.getPowerProduced()?.let {
                        "produced=${it} consumed=${building.power?.graph?.getPowerNeeded()}"
                    } ?: "none")
                )
            }
            val json = JsonUtil.buildObject("ok" to true, "data" to data)
            sendJson(exchange, 200, json)
        } catch (e: Exception) {
            sendJson(exchange, 500, """{"ok":false,"error":"${e.message}"}""")
        }
    }

    private fun sendJson(exchange: HttpExchange, status: Int, json: String) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
}

class StateHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val rules = Vars.state?.rules ?: run {
            val bytes = """{"ok":false,"error":"no game state"}""".toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
            exchange.sendResponseHeaders(400, bytes.size.toLong())
            exchange.responseBody.write(bytes)
            exchange.responseBody.close()
            return
        }
        val json = JsonUtil.buildObject(
            "ok" to true,
            "rules" to mapOf(
                "infiniteResources" to rules.infiniteResources,
                "instantBuild" to rules.instantBuild,
                "buildSpeedMultiplier" to rules.buildSpeedMultiplier,
                "blockHealthMultiplier" to rules.blockHealthMultiplier,
                "blockDamageMultiplier" to rules.blockDamageMultiplier,
                "unitHealthMultiplier" to rules.unitHealthMultiplier,
                "unitDamageMultiplier" to rules.unitDamageMultiplier,
                "buildCostMultiplier" to rules.buildCostMultiplier,
                "deconstructRefundMultiplier" to rules.deconstructRefundMultiplier,
                "unitBuildSpeedMultiplier" to rules.unitBuildSpeedMultiplier,
                "unitMineSpeedMultiplier" to rules.unitMineSpeedMultiplier,
                "fire" to rules.fire,
                "reactorExplosions" to rules.reactorExplosions,
                "waveTimer" to rules.waveTimer,
                "waveSending" to rules.waveSending,
                "unitCapVariable" to rules.unitCapVariable,
                "enemyCoreBuildRadius" to rules.enemyCoreBuildRadius
            )
        )
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
}