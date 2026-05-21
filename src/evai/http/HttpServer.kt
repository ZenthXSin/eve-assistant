package evai.http

import arc.util.*
import com.sun.net.httpserver.*
import evai.http.handlers.*
import java.net.*

object HttpServer {
    private var server: com.sun.net.httpserver.HttpServer? = null

    fun start(port: Int = 18090) {
        try {
            server = com.sun.net.httpserver.HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)
            server!!.createContext("/api/run", CommandHandler())
            server!!.createContext("/api/status", StatusHandler())
            server!!.createContext("/api/inspect", InspectHandler())
            server!!.createContext("/api/state", StateHandler())
            server!!.executor = Threads.daemon("Eve-HTTP", 2)
            server!!.start()
            Log.info("[Eve] HTTP server started on port $port")
        } catch (e: Exception) {
            Log.err("[Eve] Failed to start HTTP server: ${e.message}")
        }
    }

    fun stop() {
        server?.stop(0)
        server = null
    }
}