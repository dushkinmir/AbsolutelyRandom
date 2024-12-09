package ru.dushkinmir.absolutelyRandom.features.actions.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

class RandomMessage : Action("message") {
    private val MESSAGES = ArrayList(listOf("bruh"))

    override fun execute(plugin: Plugin) {
        val randomPlayer = PlayerUtils.getRandomPlayer()
        scheduleRandomMessagesTask(plugin, randomPlayer)
    }

    private fun scheduleRandomMessagesTask(plugin: Plugin, player: Player) {
        MessageTask(player, RANDOM.nextBoolean(), MESSAGES).runTaskTimer(plugin, 0L, TASK_INTERVAL_TICKS)
    }

    private class MessageTask(
        private val player: Player,
        private val isPlayerMessage: Boolean,
        private val MESSAGES: List<String>
    ) : BukkitRunnable() {
        private var messageCount = 0

        override fun run() {
            if (messageCount < MAX_MESSAGE_COUNT) {
                showRandomMessageToPlayer()
                messageCount++
            } else {
                this.cancel()
            }
        }

        private fun showRandomMessageToPlayer() {
            val messageList = ArrayList(MESSAGES)
            val randomMessage = messageList[RANDOM.nextInt(MESSAGES.size)]
            if (isPlayerMessage) {
                sendPlayerMessage(player, randomMessage)
            } else {
                showTitleMessage(player, randomMessage)
            }
        }

        private fun createTitleText(): Component {
            return Component.text("пениспиздасиськи", NamedTextColor.GRAY, TextDecoration.OBFUSCATED)
        }

        private fun createSubTitleText(message: String): Component {
            return Component.text(message, NamedTextColor.GOLD)
        }

        private fun showTitleMessage(player: Player, message: String) {
            val titleText = createTitleText()
            val subTitleText = createSubTitleText(message)
            player.showTitle(Title.title(titleText, subTitleText))
            PlayerUtils.sendMessageToPlayer(player, titleText, PlayerUtils.MessageType.ACTION_BAR)
        }

        private fun sendPlayerMessage(player: Player, message: String) {
            val messageFromPlayer = Component.text("<${player.name}> $message")
            PlayerUtils.sendMessageToAllPlayers(messageFromPlayer, PlayerUtils.MessageType.CHAT)
        }
    }

    private companion object {
        private val RANDOM = Random()
        private const val MAX_MESSAGE_COUNT = 3
        private const val TASK_INTERVAL_TICKS: Long = 20 * 4
    }
}