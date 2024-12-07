package ru.dushkinmir.absolutelyRandom.features.actions.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils

class Kick : Action("kick") {

    companion object {
        private val PLAYER_KICK_MESSAGE = Component.text("хахаха лошара", NamedTextColor.RED)
        private const val BROADCAST_KICK_MESSAGE_TEMPLATE = "вот же %s лох!"
        private val BROADCAST_KICK_MESSAGE_COLOR = NamedTextColor.YELLOW

    }

    override fun execute(plugin: Plugin) {
        kickPlayer(PlayerUtils.getRandomPlayer())
    }

    private fun kickPlayer(player: Player) {
        val broadcastMessage = generateKickMessage(player.name)
        PlayerUtils.sendMessageToAllPlayers(broadcastMessage, PlayerUtils.MessageType.CHAT)
        PlayerUtils.kickPlayer(player, PLAYER_KICK_MESSAGE)
    }

    private fun generateKickMessage(playerName: String): Component {
        return Component.text(
            String.format(BROADCAST_KICK_MESSAGE_TEMPLATE, playerName),
            BROADCAST_KICK_MESSAGE_COLOR
        )
    }
}