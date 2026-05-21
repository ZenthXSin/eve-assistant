package evai.http.handlers;

import com.sun.net.httpserver.*;
import evai.command.*;
import mindustry.*;
import java.net.*;
import java.nio.charset.*;

public class ApiCommandHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                sendJson(exchange, 400, "{\"ok\":false,\"error\":\"missing cmd parameter\"}");
                return;
            }
            String[] pairs = query.split("&");
            String cmd = "";
            for (String p : pairs) {
                String[] kv = p.split("=", 2);
                if (kv[0].equals("cmd") && kv.length > 1) {
                    cmd = URLDecoder.decode(kv[1], "UTF-8");
                }
            }
            if (cmd.isEmpty()) {
                sendJson(exchange, 400, "{\"ok\":false,\"error\":\"missing cmd parameter\"}");
                return;
            }
            CommandResult result = CommandRegistry.execute(cmd);
            sendJson(exchange, result.ok ? 200 : 400, result.toJson());
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"ok\":false,\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) {
        try {
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        } catch (Exception ignored) {}
    }
}