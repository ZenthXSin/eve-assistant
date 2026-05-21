package evai.command;

import java.util.Map;

public class CommandResult {
    public final boolean ok;
    public final String message;
    public final Map<String, Object> data;

    public CommandResult(boolean ok, String message, Map<String, Object> data) {
        this.ok = ok;
        this.message = message != null ? message : "";
        this.data = data;
    }

    public CommandResult(boolean ok, String message) {
        this(ok, message, null);
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"ok\":").append(ok);
        if (!message.isEmpty()) {
            sb.append(",\"message\":").append(escape(message));
        }
        if (data != null && !data.isEmpty()) {
            sb.append(",\"data\":{");
            boolean first = true;
            for (Map.Entry<String, Object> e : data.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(escape(e.getKey())).append(":").append(toValue(e.getValue()));
            }
            sb.append("}");
        }
        sb.append(",\"timestamp\":").append(System.currentTimeMillis()).append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    @SuppressWarnings("unchecked")
    private static String toValue(Object v) {
        if (v == null) return "null";
        if (v instanceof String) return escape((String) v);
        if (v instanceof Number || v instanceof Boolean) return v.toString();
        if (v instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<Object, Object> e : ((Map<Object, Object>) v).entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toValue(e.getKey())).append(":").append(toValue(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (v instanceof java.util.List) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object o : (java.util.List<Object>) v) {
                if (!first) sb.append(",");
                first = false;
                sb.append(toValue(o));
            }
            sb.append("]");
            return sb.toString();
        }
        return escape(v.toString());
    }
}