package ru.dushkinmir.absolutelyRandom.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ServerControl implements WebSocketMessageListener {
    private final Set<WebSocket> clients = new HashSet<>(); // Список клиентов
    private final Plugin plugin;

    public ServerControl(Plugin plugin) {
        this.plugin = plugin;
        plugin.getLogger().info("ServerControl activated!");
    }

    @Override
    public void onMessageReceived(WebSocket conn, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String action = json.has("action") ? json.get("action").getAsString() : null;

            switch (action) {
                case "ban":
                    handleBan(conn, json);
                    break;
                case "kick":
                    handleKick(conn, json);
                    break;
                case "op":
                    handleOp(conn, json);
                    break;
                case "deop":
                    handleDeop(conn, json);
                    break;
                case "get_players":
                    handleGetPlayers(conn, json);
                    break;
                case "tp":
                    handleTeleport(conn, json);
                    break;
                default:
                    logError("Неизвестное действие: " + action);
                    break;
            }
        } catch (Exception e) {
            logError("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    @Override
    public void onClientConnected(WebSocket conn) {
        logInfo("пыщщщ, контролирующий " + conn.getRemoteSocketAddress() + " подключился!");
    }

    @Override
    public void onClientDisconnected(WebSocket conn, int code, String reason) {
        logInfo("пыщщщ, контролирующий " + conn.getRemoteSocketAddress() + " отключился(!");
    }

    private void handleBan(WebSocket conn, JsonObject json) {
        String playerName = json.has("player") ? json.get("player").getAsString() : null;
        if (playerName != null) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.banPlayer(json.has("reason") ? json.get("reason").getAsString() : "No reason provided");
                sendResponse(conn, json, "success", "ban", playerName, "Игрок забанен");
            } else {
                sendResponse(conn, json, "error", "ban", playerName, "Игрок не найден");
            }
        } else {
            sendResponse(conn, json, "error", "ban", null, "Не указан игрок");
        }
    }

    private void handleKick(WebSocket conn, JsonObject json) {
        String playerName = json.has("player") ? json.get("player").getAsString() : null;
        if (playerName != null) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.kickPlayer(json.has("reason") ? json.get("reason").getAsString() : "No reason provided");
                sendResponse(conn, json, "success", "kick", playerName, "Игрок кикнут");
            } else {
                sendResponse(conn, json, "error", "kick", playerName, "Игрок не найден");
            }
        } else {
            sendResponse(conn, json, "error", "kick", null, "Не указан игрок");
        }
    }

    private void handleOp(WebSocket conn, JsonObject json) {
        String playerName = json.has("player") ? json.get("player").getAsString() : null;
        if (playerName != null) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.setOp(true);
                sendResponse(conn, json, "success", "op", playerName, "Игрок стал опом");
            } else {
                sendResponse(conn, json, "error", "op", playerName, "Игрок не найден");
            }
        } else {
            sendResponse(conn, json, "error", "op", null, "Не указан игрок");
        }
    }

    private void handleDeop(WebSocket conn, JsonObject json) {
        String playerName = json.has("player") ? json.get("player").getAsString() : null;
        if (playerName != null) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.setOp(false);
                sendResponse(conn, json, "success", "deop", playerName, "Игрок больше не оп");
            } else {
                sendResponse(conn, json, "error", "deop", playerName, "Игрок не найден");
            }
        } else {
            sendResponse(conn, json, "error", "deop", null, "Не указан игрок");
        }
    }

    private void handleGetPlayers(WebSocket conn, JsonObject json) {
        Set<String> playerNames = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }

        String playerList = String.join(", ", playerNames);
        sendResponse(conn, json, "success", "get_players", playerList, "Список игроков");
    }

    private void handleTeleport(WebSocket conn, JsonObject json) {
        String playerName = json.has("player") ? json.get("player").getAsString() : null;
        String targetName = json.has("target") ? json.get("target").getAsString() : null;
        if (playerName != null && targetName != null) {
            Player player = Bukkit.getPlayer(playerName);
            Player target = Bukkit.getPlayer(targetName);
            if (player != null && target != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.teleport(target);

                    }
                }.runTask(plugin);
                sendResponse(conn, json, "success", "tp", playerName, "Игрок телепортирован");
            } else {
                sendResponse(conn, json, "error", "tp", null, "Игроки не найдены");
            }
        } else {
            sendResponse(conn, json, "error", "tp", null, "Не указаны игроки");
        }
    }

    private void sendResponse(WebSocket conn, JsonObject request, String status, String action, String playerOrPlayers, String descStatus) {
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("action", action);
        if (playerOrPlayers != null) {
            response.addProperty("players", playerOrPlayers);
        }
        response.addProperty("desc_status", descStatus);

        // Отправка ответа всем подключенным клиентам
        conn.send(response.toString());

        if (request != null) {
            logInfo("Ответ на запрос: " + response.toString());
        }
    }

    private void logInfo(String message) {
        Bukkit.getLogger().log(Level.INFO, message);
    }

    private void logError(String message) {
        Bukkit.getLogger().log(Level.SEVERE, message);
    }
}
