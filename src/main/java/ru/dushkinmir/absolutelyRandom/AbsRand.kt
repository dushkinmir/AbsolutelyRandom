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
            logger.info("Starting...")
            saveDefaultConfig() // Save default config if not exist
            extensionManager.onEnable()
            eventsManager.onEnable()
            actionsManager.onEnable()
            commandsManager.onEnable()
            webSocketHandler.onEnable()

            logger.info("Starting is completed.")
            logger.info("Hi there!!")
        } catch (e: Exception) {
            logger.severe("An error occurred while enabling the plugin: ${e.message}")
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        logger.info("Stopping...")
        PLAYER_TASKS.values.forEach { it.cancel() }
        PLAYER_TASKS.clear()

        webSocketHandler.onDisable()
        commandsManager.onDisable()
        eventsManager.onDisable()
        extensionManager.onDisable()
        logger.info("Stopping is completed.")
        logger.info("Goodbye!")
    }
}