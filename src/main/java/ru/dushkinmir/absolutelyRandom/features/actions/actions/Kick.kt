package ru.dushkinmir.absolutelyRandom.features.actions.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils

class Kick : Action("kick") {
    override fun execute(plugin: Plugin) {
        kickPlayer(PlayerUtils.getRandomPlayer())
    }

    private fun kickPlayer(player: Player) {
        PlayerUtils.sendMessageToAllPlayers(
            Component.text("вот же %s лох! ${player.name}").color(
                NamedTextColor.YELLOW
            ),
            PlayerUtils.MessageType.CHAT
        )
        PlayerUtils.kickPlayer(player, Component.text("хахаха лошара", NamedTextColor.RED))
    }
}