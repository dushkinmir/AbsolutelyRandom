package ru.dushkinmir.absolutelyRandom.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ru.dushkinmir.absolutelyRandom.AbsolutelyRandom;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;

public class TelegramHelper {

    public static void startServer(AbsolutelyRandom plugin) {
        Spark.ipAddress("127.0.0.1");
        Spark.port(5000);
        Spark.post("/trigger_event", (request, response) -> {
            response.type("application/json");
            JSONParser jsonParser = new JSONParser();
            Map<String, String> responseMap = new HashMap<>();

            try {
                JSONObject json = parseJsonRequest(jsonParser, request.body());
                String event = (String) json.get("event");

                if (event == null || event.isEmpty()) {
                    return createErrorResponse(responseMap, response, "Invalid event type", 400);
                }

                processEvent(plugin, event);
                return createSuccessResponse(responseMap);
            } catch (Exception e) {
                return createErrorResponse(responseMap, response, "Exception: " + e.getMessage(), 500);
            }
        });
    }

    private static JSONObject parseJsonRequest(JSONParser jsonParser, String body) throws Exception {
        return (JSONObject) jsonParser.parse(body);
    }

    private static void processEvent(AbsolutelyRandom plugin, String event) {
        CommandSender console = Bukkit.getServer().getConsoleSender();
        plugin.handleDebugRandom(console, event);
    }

    private static String createSuccessResponse(Map<String, String> responseMap) {
        responseMap.put("result", "success");
        return new JSONObject(responseMap).toJSONString();
    }

    private static String createErrorResponse(Map<String, String> responseMap, spark.Response response, String message, int statusCode) {
        responseMap.put("result", "error");
        responseMap.put("message", message);
        response.status(statusCode);
        return new JSONObject(responseMap).toJSONString();
    }
}