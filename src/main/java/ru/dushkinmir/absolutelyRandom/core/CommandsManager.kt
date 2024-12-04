package ru.dushkinmir.absolutelyRandom.core

import dev.jorel.commandapi.CommandAPI
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.sex.SexCommands
import ru.dushkinmir.absolutelyRandom.features.warp.WarpCommands

class CommandsManager(private val plugin: Plugin, private val extensionManager: ExtensionManager) {

    fun registerCommands() {
        // Initialize and register WarpCommandManager
        val wcm = WarpCommands(extensionManager.getWarpManager(), plugin)
        // Initialize and register SexCommandManager
        val scm = SexCommands(extensionManager.getFissureHandler(), plugin)
        wcm.registerWarpCommands() // Register warp commands
        scm.registerSexCommand() // Register sex commands
        CommandAPI.onEnable() // Enable CommandAPI
    }

    fun unregisterCommands() {
        // Unregister commands
        CommandAPI.unregister("debugrandom")
        CommandAPI.unregister("warp")
        CommandAPI.unregister("sex")
        plugin.logger.info("Команды CommandAPI отменены.")
        // Disable CommandAPI
        CommandAPI.onDisable()
    }
}