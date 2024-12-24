package ru.dushkinmir.absolutelyRandom

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import ru.dushkinmir.absolutelyRandom.core.*

class AbsRand : JavaPlugin(), Listener {
    // Core modules
    private val actionsManager = ActionsManager(this)
    private val webSocketManager = WebSocketManager(this)
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
            webSocketManager.onEnable()

            logger.info("Starting is completed.")
            logger.info("Hi there!!")
        } catch (e: Exception) {
            logger.severe("An error occurred while enabling the plugin: ${e.message}")
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        webSocketManager.onDisable()
        commandsManager.onDisable()
        actionsManager.onDisable()
        eventsManager.onDisable()
        extensionManager.onDisable()

        logger.info("Stopping is completed.")
        logger.info("Goodbye!")
    }

    // TODO: добавить следы от шагов
    //  /summon minecraft:text_display ~ ~0.02 ~ {text:{text:'◼',color:'gray'},transformation:{left_rotation:[1f,1f,1f,1f],right_rotation:[1f,1f,1f,1f],translation:[0f,0f,0f],scale:[8f,8f,8f]}}
}