package evai.http;

import arc.util.*;
import com.sun.net.httpserver.*;
import evai.http.handlers.*;
import java.net.*;
import java.util.concurrent.*;

public class HttpServer {
    private static com.sun.net.httpserver.HttpServer server;

    public static void start() {
        start(18090);
    }

    public static void start(int port) {
        try {
            server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
            server.createContext("/api/run", new ApiCommandHandler());
            server.createContext("/api/status", new ApiStatusHandler());
            server.createContext("/api/inspect", new ApiInspectHandler());
            server.createContext("/api/state", new ApiStateHandler());
            server.setExecutor(Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "Eve-HTTP");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            Log.info("[Eve] HTTP server started on port " + port);
        } catch (Exception e) {
            Log.err("[Eve] Failed to start HTTP server: " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) server.stop(0);
    }
}