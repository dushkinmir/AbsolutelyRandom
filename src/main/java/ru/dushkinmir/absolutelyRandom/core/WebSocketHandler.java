package ru.dushkinmir.absolutelyRandom.core;

import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.network.WebSocketMessageListener;
import ru.dushkinmir.absolutelyRandom.network.WebSocketServer;

public class WebSocketHandler {
    private final Plugin plugin;
    private WebSocketServer wsserver;

    public WebSocketHandler(Plugin plugin) {
        this.plugin = plugin;
    }


    public void enableWebSocketServer() {
        if (!plugin.getConfig().getBoolean("betters.websocket.enabled", false)) {
            plugin.getLogger().info("WebSocket отключен в конфиге");
            return;
        }

        plugin.getLogger().info("Запуск веб-сокет сервера...");
        // Get server IP and port
        String serverIp = plugin.getServer().getIp().isEmpty() ? "localhost" : plugin.getServer().getIp();
        int port = plugin.getServer().getPort() + 1;

        // Initialize WebSocket server
        wsserver = new WebSocketServer(serverIp, port, plugin.getLogger());
        try {
            plugin.getLogger().info("Конфигурация WebSocket успешно загружена.");
            wsserver.start(); // Start WebSocket server
            plugin.getLogger().info("WebSocket сервер запущен на IP " + serverIp + " и порту " + port);
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось активировать слушателей WebSocket. " + e.getMessage());
        }
    }

    public void disableWebSocketServer() {
        // Stopping the WebSocket server
        if (wsserver != null) {
            try {
                // Remove all listeners
                for (WebSocketMessageListener listener : wsserver.getListeners()) {
                    wsserver.removeListener(listener);
                }
                plugin.getLogger().info("Остановка WebSocket сервера.");
                wsserver.stop(); // Stop the server
            } catch (Exception e) {
                plugin.getLogger().severe("Не удалось остановить WebSocket сервер: " + e.getMessage());
            }
            plugin.getLogger().info("WebSocket сервер остановлен.");
        }
    }
}
