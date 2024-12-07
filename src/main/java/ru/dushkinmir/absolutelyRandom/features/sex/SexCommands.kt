package ru.dushkinmir.absolutelyRandom.features.sex

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.SafeSuggestions
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SexCommands(val fissureHandler: AnalFissureHandler, val plugin: Plugin) {

    fun registerSexCommand() {
        val noSelectorSuggestions: Argument<*> = PlayerArgument("target")
            .replaceSafeSuggestions(SafeSuggestions.suggest { info ->
                val senderPlayer = info.sender() as Player
                plugin.server.onlinePlayers.stream()
                    .filter { player -> player != senderPlayer }
                    .toArray { size -> arrayOfNulls<Player>(size) }
            })
        CommandAPICommand("sex")
            .withArguments(noSelectorSuggestions)
            .withPermission("absolutelyrandom.sex")
            .executes(CommandExecutor { sender, args ->
                if (sender is Player) {
                    val target = args["target"] as Player
                    SexManager.triggerSexEvent(sender, target, plugin, fissureHandler)
                }
            })
            .register()
    }
}