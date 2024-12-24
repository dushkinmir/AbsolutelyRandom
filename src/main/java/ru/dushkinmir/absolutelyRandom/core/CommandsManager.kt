package ru.dushkinmir.absolutelyRandom.core

import dev.jorel.commandapi.CommandAPI
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.sex.SexCommands
import ru.dushkinmir.absolutelyRandom.features.warp.WarpCommands

class CommandsManager(private val plugin: Plugin, private val extensionManager: ExtensionManager) {

    fun onEnable() {
        // Initialize and register WarpCommandManager
        val wcm = WarpCommands(extensionManager.getWarpManager(), plugin)
        // Initialize and register SexCommandManager
        val scm = SexCommands(extensionManager.getFissureHandler(), plugin)
        wcm.registerWarpCommands() // Register warp commands
        scm.registerSexCommand() // Register sex commands
        CommandAPI.onEnable() // Enable CommandAPI
    }

    fun onDisable() {
        // Unregister commands
        CommandAPI.getRegisteredCommands().forEach { CommandAPI.unregister(it.commandName) }
        plugin.logger.info("All commands are deregistered.")
        // Disable CommandAPI
        CommandAPI.onDisable()
    }
}