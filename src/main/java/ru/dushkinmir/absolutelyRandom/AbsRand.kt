package ru.dushkinmir.absolutelyRandom

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.core.*
import java.util.*

class AbsRand : JavaPlugin(), Listener {
    companion object {
        private val PLAYER_TASKS: MutableMap<UUID, BukkitRunnable> = HashMap() // Map to store player tasks

        fun getPlayerTasks(): MutableMap<UUID, BukkitRunnable> {
            return PLAYER_TASKS
        }
    }

    // Core modules
    private val actionsManager = ActionsManager(this)
    private val webSocketHandler = WebSocketManager(this)
    private val extensionManager = ExtensionManager(this)
    private val eventsManager = EventsManager(this, extensionManager)
    private val commandsManager = CommandsManager(this, extensionManager)

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).verboseOutput(true).usePluginNamespace())
    }

    override fun onEnable() {
        try {
            saveDefaultConfig() // Save default config if not exist
            extensionManager.initManagers()
            eventsManager.registerEvents()
            actionsManager.registerAllActions()
            commandsManager.registerCommands()
            webSocketHandler.enableWebSocketServer()

            logger.info("Hi there!!")
        } catch (e: Exception) {
            logger.severe("Ошибка при включении плагина: " + e.message)
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        // Cancel and clear player tasks
        PLAYER_TASKS.values.forEach { it.cancel() }
        PLAYER_TASKS.clear()

        webSocketHandler.disableWebSocketServer()
        commandsManager.unregisterCommands()
        eventsManager.onDisable()
        extensionManager.shutdownManagers()
    }
}