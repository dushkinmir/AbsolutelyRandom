package ru.dushkinmir.absolutelyRandom.features.actions.types

import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.AbsRand
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

class Stinky : Action("stinky"), Listener {
    private var plugin: Plugin? = null

    override fun execute(plugin: Plugin) {
        val players = PlayerUtils.getOnlinePlayers()
        val randomPlayer = PlayerUtils.getRandomPlayer(players)
        PlayerUtils.sendMessageToPlayer(randomPlayer, ACTION_BAR_TEXT, PlayerUtils.MessageType.ACTION_BAR)
        sendPlayerWorldMessage(randomPlayer)
        val playerUUID = randomPlayer.uniqueId
        scheduleEffects(plugin, playerUUID)
        this.plugin = plugin
    }

    private fun isPlayerTracked(playerUUID: UUID): Boolean {
        return AbsRand.getPlayerTasks().containsKey(playerUUID)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerUUID = player.uniqueId
        if (isPlayerTracked(playerUUID)) {
            scheduleEffects(plugin!!, playerUUID)
        }
    }

    private class PlayerEffectTask(private val player: Player) : BukkitRunnable() {
        override fun run() {
            val playerTasks = AbsRand.getPlayerTasks()
            val playerUUID = player.uniqueId
            if (isPlayerInWater(player)) {
                PlayerUtils.sendMessageToPlayer(player, NORMAL_PLAYER_MESSAGE, PlayerUtils.MessageType.CHAT)
                this.cancel()
                playerTasks.remove(playerUUID)
                return
            }
            applyPoisonEffect(player)
            applySmokeEffect(player)
            if (!player.isOnline) {
                this.cancel()
            }
        }

        private fun isPlayerInWater(player: Player): Boolean {
            return player.location.block.type == Material.WATER
        }
    }

    companion object {
        private val ACTION_BAR_TEXT = Component.text("фуу ты вонючка!")
        private val STINKY_PLAYER_MESSAGE = Component.text(
            "бля чел иди искупайся, а то от тебя весь сервер щарахается"
        )
        private val NORMAL_PLAYER_MESSAGE = Component.text("воо, молодец!")

        private fun sendPlayerWorldMessage(player: Player) {
            val message = "a %s теперь воняет".format(Objects.requireNonNull(player).name)
            PlayerUtils.sendMessageToAllPlayers(Component.text(message), PlayerUtils.MessageType.CHAT)
        }

        private fun scheduleEffects(plugin: Plugin, playerUUID: UUID) {
            val player = plugin.server.getPlayer(playerUUID)
            if (player != null) {
                val playerTasks = AbsRand.getPlayerTasks()
                val task = PlayerEffectTask(player)
                task.runTaskTimer(plugin, 0, 20L)
                if (!playerTasks.containsKey(playerUUID)) playerTasks[playerUUID] = task
                PlayerUtils.sendMessageToPlayer(player, STINKY_PLAYER_MESSAGE, PlayerUtils.MessageType.CHAT)
            }
        }

        private fun applyPoisonEffect(player: Player) {
            for (entity in player.getNearbyEntities(1.0, 1.0, 1.0)) {
                if (entity is LivingEntity && entity != player) {
                    entity.addPotionEffect(PotionEffect(PotionEffectType.POISON, 60, 1, true, true))
                }
            }
        }

        private fun applySmokeEffect(player: Player) {
            val random = Random()
            val particleSize = random.nextFloat(0.5f, 2.0f)
            player.world.spawnParticle(
                Particle.DUST,
                player.location,
                75, 1.0, 1.0, 1.0,
                Particle.DustOptions(Color.GREEN, particleSize)
            )
        }
    }
}