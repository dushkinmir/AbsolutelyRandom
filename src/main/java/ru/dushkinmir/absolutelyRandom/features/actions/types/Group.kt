package ru.dushkinmir.absolutelyRandom.features.actions.types

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import kotlin.random.Random

class Group : Action("group") {

    private companion object {
        private val EVENT_STARTING_MESSAGE =
            Component.text("ГРУППОВАЯ МАСТУРБАЦИЯ НАЧНЕТСЯ ЧЕРЕЗ...", NamedTextColor.RED)
        private val EVENT_END_MESSAGE =
            Component.text("ГРУППОВАЯ МАСТУРБАЦИЯ ОКОНЧЕНА, СПАСИБО ЗА УЧАСТИЕ", NamedTextColor.GREEN)
        private const val COUNTDOWN_SECONDS = 5
        private const val EVENT_DURATION_TICKS = 500
        private var eventActive = false
    }

    override fun execute(plugin: Plugin) {
        if (eventActive) {
            return
        }
        eventActive = true

        PlayerUtils.sendMessageToAllPlayers(EVENT_STARTING_MESSAGE, PlayerUtils.MessageType.CHAT)

        EventCountdownTask(plugin, PlayerUtils.getOnlinePlayers()).runTaskTimer(plugin, 0L, 20L)
        object : BukkitRunnable() {
            override fun run() {
                PlayerUtils.sendMessageToAllPlayers(EVENT_END_MESSAGE, PlayerUtils.MessageType.CHAT)
            }
        }.runTaskLater(plugin, ((COUNTDOWN_SECONDS * 20) + EVENT_DURATION_TICKS).toLong())
    }

    private class EventCountdownTask(private val plugin: Plugin, private val players: List<Player>) : BukkitRunnable() {
        private var countdown = COUNTDOWN_SECONDS

        override fun run() {
            if (countdown > 0) {
                PlayerUtils.sendMessageToAllPlayers(
                    Component.text("$countdown...", NamedTextColor.RED),
                    PlayerUtils.MessageType.CHAT
                )
                countdown--
            } else {
                FallingBlocksTask(plugin, players).runTaskTimer(plugin, 0L, 1L)
                this.cancel()
            }
        }
    }

    class FallingBlocksTask(private val plugin: Plugin, private val players: List<Player>) : BukkitRunnable() {
        private var remainingTicks = EVENT_DURATION_TICKS

        override fun run() {
            if (remainingTicks > 0) {
                for (player in players) {
                    if (!player.isOnline) return
                    dropItemNearPlayer(player)
                }
                remainingTicks--
            } else {
                eventActive = false
                this.cancel()
            }
        }

        private fun dropItemNearPlayer(player: Player) {
            val item = ItemStack(Material.WHITE_WOOL, 3)
            val direction = player.eyeLocation.direction.normalize()
            val droppedItem = player.world.dropItem(player.location.add(direction.multiply(2)), item)
            if (Random.nextInt(10) == 1) {
                droppedItem.pickupDelay = Int.MAX_VALUE
            } else {
                object : BukkitRunnable() {
                    override fun run() {
                        droppedItem.remove()
                    }
                }.runTaskLater(plugin, 9L)
            }
        }
    }
}