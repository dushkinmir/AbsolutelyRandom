package ru.dushkinmir.absolutelyRandom.features.sex

import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SexCommands(val fissureHandler: AnalFissureHandler, val plugin: Plugin) {

    fun registerSexCommand() {
        commandTree("sex") {
            playerArgument("target") {
                replaceSuggestions { info, builder ->
                    val senderPlayer = info.sender() as Player
                    plugin.server.onlinePlayers.stream()
                        .filter { player -> player != senderPlayer }
                        .forEach { builder.suggest(it.name) }
                    builder.buildFuture()
                }
                withPermission(CommandPermission.NONE)
                playerExecutor { player, args ->
                    val target = args["target"] as Player
                    SexManager.triggerSexEvent(player, target, plugin, fissureHandler)
                }
            }
        }
    }
}