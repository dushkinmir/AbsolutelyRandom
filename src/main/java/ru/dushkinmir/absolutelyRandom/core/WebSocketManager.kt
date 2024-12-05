package ru.dushkinmir.absolutelyRandom.core

import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.network.WebSocketServer

class WebSocketManager(private val plugin: Plugin) {
    private var wsserver: WebSocketServer? = null

    fun onEnable() {
        if (!plugin.config.getBoolean("betters.websocket.enabled", false)) {
            plugin.logger.info("WebSocket отключен в конфиге")
            return
        }

        plugin.logger.info("Запуск веб-сокет сервера...")
        // Get server IP and port
        val serverIp = if (plugin.server.ip.isEmpty()) "localhost" else plugin.server.ip
        val port = plugin.server.port + 1

        // Initialize WebSocket server
        wsserver = WebSocketServer(serverIp, port, plugin.logger)
        try {
            plugin.logger.info("Конфигурация WebSocket успешно загружена.")
            wsserver?.start() // Start WebSocket server
            plugin.logger.info("WebSocket сервер запущен на IP $serverIp:$port")
        } catch (e: Exception) {
            plugin.logger.severe("Не удалось активировать слушателей WebSocket. " + e.message)
        }
    }

    fun onDisable() {
        // Stopping the WebSocket server
        if (wsserver != null) {
            try {
                // Remove all listeners
                for (listener in wsserver!!.listeners) {
                    wsserver?.removeListener(listener)
                }
                plugin.logger.info("Остановка WebSocket сервера.")
                wsserver?.stop() // Stop the server
            } catch (e: Exception) {
                plugin.logger.severe("Не удалось остановить WebSocket сервер: " + e.message)
            }
            plugin.logger.info("WebSocket сервер остановлен.")
        }
    }
}