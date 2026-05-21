package evai.command;

import mindustry.*;
import mindustry.logic.*;
import java.util.*;

public class JsCommands implements CommandRegistry.CommandHandler {
 @Override
 public CommandResult handle(String action, String[] args) {
 switch (action) {
 case "js": return js(args);
 case "logic": return logic(args);
 case "eval": return eval(args);
 default: return new CommandResult(false, "unknown js command: " + action);
 }
 }

 private CommandResult js(String[] args) {
 if (args.length == 0) return new CommandResult(false, "usage: js <code>");
 String code = String.join(" ", args);
 try {
 Object result = Vars.mods.getScripts().runConsole(code);
 Map<String, Object> data = new HashMap<>();
 data.put("code", code);
 data.put("result", result != null ? result.toString() : "undefined");
 return new CommandResult(true, "", data);
 } catch (Exception e) {
 return new CommandResult(false, "JS error: " + e.getMessage());
 }
 }

 private CommandResult logic(String[] args) {
 if (args.length == 0) return new CommandResult(false, "usage: logic <code>");
 String code = String.join(" ", args);
 try {
                LExecutor.runLogicScript(code);
                Map<String, Object> data = new HashMap<>();
                data.put("code", code);
                data.put("result", "executed");
                return new CommandResult(true, "", data);
 } catch (Exception e) {
 return new CommandResult(false, "Logic error: " + e.getMessage());
 }
 }

 private CommandResult eval(String[] args) {
 if (args.length == 0) return new CommandResult(false, "usage: eval <expr>");
 String expr = String.join(" ", args);
 try {
 Object result = Vars.mods.getScripts().runConsole("print(" + expr + ");");
 Map<String, Object> data = new HashMap<>();
 data.put("expression", expr);
 data.put("result", result != null ? result.toString() : "undefined");
 return new CommandResult(true, "", data);
 } catch (Exception e) {
 return new CommandResult(false, "Eval error: " + e.getMessage());
 }
 }
}