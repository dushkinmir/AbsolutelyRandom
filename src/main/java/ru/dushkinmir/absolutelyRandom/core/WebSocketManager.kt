package ru.dushkinmir.absolutelyRandom.core

import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.utils.network.WebSocketServer

class WebSocketManager(private val plugin: Plugin) {
    private var wsserver: WebSocketServer? = null

    fun onEnable() {
        if (!plugin.config.getBoolean("betters.websocket.enabled", false)) {
            plugin.logger.info("WebSocket are disabled in config.")
            return
        }

        plugin.logger.info("Starting WebSocket server...")
        // Get server IP and port
        val serverIp = if (plugin.server.ip.isEmpty()) "localhost" else plugin.server.ip
        val port = plugin.server.port + 1

        // Initialize WebSocket server
        wsserver = WebSocketServer(serverIp, port, plugin.logger)
        try {
            plugin.logger.info("Configuration of WebSocket server is OK.")
            wsserver?.start() // Start WebSocket server
            plugin.logger.info("WebSocket server is running on IP $serverIp:$port")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to activate WebSocket listeners. " + e.message)
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
                plugin.logger.info("Stopping the WebSocket server.")
                wsserver?.stop() // Stop the server
            } catch (e: Exception) {
                plugin.logger.severe("Failed to stop the WebSocket server: " + e.message)
            }
            plugin.logger.info("WebSocket server stopped.")
        }
    }
}