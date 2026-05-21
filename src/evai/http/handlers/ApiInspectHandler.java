package evai.http.handlers;

import com.sun.net.httpserver.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;
import java.net.*;
import java.nio.charset.*;

public class ApiInspectHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            String query = exchange.getRequestURI().getQuery();
            if (query == null) query = "";
            String[] pairs = query.split("&");
            Integer x = null, y = null;
            for (String p : pairs) {
                String[] kv = p.split("=", 2);
                if (kv.length < 2) continue;
                String val = URLDecoder.decode(kv[1], "UTF-8");
                if (kv[0].equals("x")) x = Integer.parseInt(val);
                else if (kv[0].equals("y")) y = Integer.parseInt(val);
            }
            if (x == null || y == null) {
                sendJson(exchange, 400, "{\"ok\":false,\"error\":\"invalid coordinates\"}");
                return;
            }
            Tile tile = Vars.world.tile(x, y);
            if (tile == null) {
                sendJson(exchange, 404, "{\"ok\":false,\"error\":\"tile not found\"}");
                return;
            }
            Building bld = tile.build;
            StringBuilder sb = new StringBuilder();
            sb.append("{\"ok\":true,\"data\":{");
            sb.append("\"x\":").append(x).append(",\"y\":").append(y).append(",");
            sb.append("\"block\":\"").append(tile.block() != null ? tile.block().name : "air").append("\",");
            sb.append("\"floor\":\"").append(tile.floor() != null ? tile.floor().name : "?").append("\",");
            sb.append("\"overlay\":\"").append(tile.overlay() != null ? tile.overlay().name : "none").append("\",");
            sb.append("\"team\":\"").append(tile.team() != null ? tile.team().name : "none").append("\",");
            sb.append("\"solid\":").append(tile.solid()).append(",\"passable\":").append(tile.passable());
            if (bld != null) {
                sb.append(",\"building\":{");
                sb.append("\"health\":\"").append((int) bld.health()).append("/").append((int) bld.maxHealth()).append("\",");
                sb.append("\"enabled\":").append(bld.enabled).append(",");
                sb.append("\"warmup\":").append(bld.warmup()).append(",");
                sb.append("\"efficiency\":").append(bld.efficiency).append(",");
                sb.append("\"rotation\":").append(bld.rotation).append(",");
                sb.append("\"items\":\"").append(bld.items != null ? bld.items.total() : 0).append("/").append(bld.block.itemCapacity).append("\",");
                sb.append("\"power\":\"").append(bld.power != null ? bld.power.status : 0.0).append("\"");
                sb.append("}");
            }
            sb.append("},\"timestamp\":").append(System.currentTimeMillis()).append("}");
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