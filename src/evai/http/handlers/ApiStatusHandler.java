package evai.http.handlers;

import com.sun.net.httpserver.*;
import mindustry.*;
import java.nio.charset.*;

public class ApiStatusHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"ok\":true,\"data\":{");
            sb.append("\"map\":\"").append(Vars.state.map != null ? Vars.state.map.name() : "none").append("\",");
            sb.append("\"wave\":").append(Vars.state.wave).append(",");
            sb.append("\"team\":\"").append(Vars.player != null ? Vars.player.team().name : "none").append("\",");
            sb.append("\"players\":\"").append(Vars.player != null ? Vars.player.name : "none").append("\",");
            sb.append("\"tiles\":\"").append(Vars.world.width()).append("x").append(Vars.world.height()).append("\",");
            sb.append("\"gamemode\":\"").append(Vars.state.rules != null ? Vars.state.rules.mode().name() : "unknown").append("\",");
            sb.append("\"timestamp\":").append(System.currentTimeMillis());
            sb.append("}}");
            sendJson(exchange, 200, sb.toString());
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