package evai.http.handlers;

import com.sun.net.httpserver.*;
import mindustry.*;
import mindustry.game.*;
import java.nio.charset.*;

public class ApiStateHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (Vars.state == null || Vars.state.rules == null) {
                sendJson(exchange, 400, "{\"ok\":false,\"error\":\"no game state\"}");
                return;
            }
            Rules rules = Vars.state.rules;
            StringBuilder sb = new StringBuilder();
            sb.append("{\"ok\":true,\"data\":{\"rules\":{");
            sb.append("\"infiniteResources\":").append(rules.infiniteResources).append(",");
            sb.append("\"instantBuild\":").append(rules.instantBuild).append(",");
            sb.append("\"buildSpeedMultiplier\":").append(rules.buildSpeedMultiplier).append(",");
            sb.append("\"blockHealthMultiplier\":").append(rules.blockHealthMultiplier).append(",");
            sb.append("\"blockDamageMultiplier\":").append(rules.blockDamageMultiplier).append(",");
            sb.append("\"unitHealthMultiplier\":").append(rules.unitHealthMultiplier).append(",");
            sb.append("\"unitDamageMultiplier\":").append(rules.unitDamageMultiplier).append(",");
            sb.append("\"buildCostMultiplier\":").append(rules.buildCostMultiplier).append(",");
            sb.append("\"deconstructRefundMultiplier\":").append(rules.deconstructRefundMultiplier).append(",");
            sb.append("\"unitBuildSpeedMultiplier\":").append(rules.unitBuildSpeedMultiplier).append(",");
            sb.append("\"unitMineSpeedMultiplier\":").append(rules.unitMineSpeedMultiplier).append(",");
            sb.append("\"fire\":").append(rules.fire).append(",");
            sb.append("\"reactorExplosions\":").append(rules.reactorExplosions).append(",");
            sb.append("\"waveTimer\":").append(rules.waveTimer).append(",");
            sb.append("\"waveSending\":").append(rules.waveSending);
            sb.append("}}}");
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