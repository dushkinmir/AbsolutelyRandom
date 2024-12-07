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
import ru.dushkinmir.absolutelyRandom.core.PlayerData
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

class Stinky(private val plugin: Plugin) : Action("stinky"), Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun execute(plugin: Plugin) {
        val randomPlayer = PlayerUtils.getRandomPlayer()
        PlayerUtils.sendMessageToPlayer(randomPlayer, ACTION_BAR_TEXT, PlayerUtils.MessageType.ACTION_BAR)
        sendPlayerWorldMessage(randomPlayer)
        val playerUUID = randomPlayer.uniqueId
        if (!isPlayerTracked(playerUUID)) {
            scheduleEffects(plugin, playerUUID)
        }
    }

    private fun isPlayerTracked(playerUUID: UUID): Boolean {
        val playerData = getPlayerData(playerUUID)
        return playerData.containsKey("stinky")
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (isPlayerTracked(event.player.uniqueId)) {
            scheduleEffects(plugin, event.player.uniqueId)
        }
    }

    private class PlayerEffectTask(private val player: Player, private val playerData: PlayerData) : BukkitRunnable() {
        override fun run() {
            if (player.location.block.type == Material.WATER) {
                PlayerUtils.sendMessageToPlayer(player, NORMAL_PLAYER_MESSAGE, PlayerUtils.MessageType.CHAT)
                this.cancel()
                playerData.remove("stinky")
                return
            }
            applyPoisonEffect(player)
            applySmokeEffect(player)
            if (!player.isOnline) {
                this.cancel()
            }
        }
    }

    companion object {
        private val ACTION_BAR_TEXT = Component.text("фуу ты вонючка!")
        private val STINKY_PLAYER_MESSAGE = Component.text(
            "бля чел иди искупайся, а то от тебя весь сервер щарахается"
        )
        private val NORMAL_PLAYER_MESSAGE = Component.text("воо, молодец!")

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

    private fun sendPlayerWorldMessage(player: Player) {
        val message = "a ${player.name} теперь воняет"
        PlayerUtils.sendMessageToAllPlayers(Component.text(message), PlayerUtils.MessageType.CHAT)
    }

    private fun scheduleEffects(plugin: Plugin, playerUUID: UUID) {
        val player = plugin.server.getPlayer(playerUUID)
        if (player != null) {
            val playerData = getPlayerData(playerUUID)
            val task = PlayerEffectTask(player, playerData)
            task.runTaskTimer(plugin, 0, 20L)
            playerData["stinky"] = task
            PlayerUtils.sendMessageToPlayer(player, STINKY_PLAYER_MESSAGE, PlayerUtils.MessageType.CHAT)
        }
    }
}