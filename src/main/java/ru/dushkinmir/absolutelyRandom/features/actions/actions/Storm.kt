package ru.dushkinmir.absolutelyRandom.features.actions.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Storm : Action("storm") {
    companion object {
        private val STORM_MESSAGE = Component.text("Гроза началась! Убегай!", NamedTextColor.YELLOW)
        private const val STORM_DURATION_SECONDS = 60
        private const val STRIKE_INTERVAL_SECONDS = 10
        private const val STRIKE_DISTANCE = 5.0

        private fun startStorm(plugin: Plugin, players: List<Player>) {
            players.forEach { it.world.setStorm(true) }
            sendStormWarningToPlayers(players)
            StormTask(players).runTaskTimer(plugin, 60, 20L * STRIKE_INTERVAL_SECONDS)
            players.forEach { it.world.setStorm(false) }
        }

        private fun sendStormWarningToPlayers(players: List<Player>) {
            for (player in players) {
                PlayerUtils.sendMessageToPlayer(player, STORM_MESSAGE, PlayerUtils.MessageType.ACTION_BAR)
            }
        }

        private fun strikeRandomPlayer(players: List<Player>) {
            val randomPlayer = PlayerUtils.getRandomPlayer(players)
            val strikeLocation = calculateStrikeLocation(randomPlayer.location)
            createWeatherEffect(strikeLocation)
        }

        private fun calculateStrikeLocation(location: Location): Location {
            // Получаем случайный угол для определения направления
            val angle = Random.nextDouble() * 2 * Math.PI // Угол в радианах
            val xOffset = cos(angle) * STRIKE_DISTANCE // X смещение
            val zOffset = sin(angle) * STRIKE_DISTANCE // Z смещение
            return location.clone().add(xOffset, 0.0, zOffset) // Возвращаем новую локацию
        }

        private fun createWeatherEffect(strikeLocation: Location) {
            strikeLocation.world.strikeLightning(strikeLocation)
            strikeLocation.world.createExplosion(strikeLocation, 4.0f, false, false)
            strikeLocation.world.spawnParticle(Particle.EXPLOSION, strikeLocation, 10)
            strikeLocation.world.playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f)
        }
    }

    override fun execute(plugin: Plugin) {
        val onlinePlayers = PlayerUtils.getOnlinePlayers()
        startStorm(plugin, onlinePlayers)
    }

    private class StormTask(private val players: List<Player>) : BukkitRunnable() {
        private var elapsedSeconds = 0

        override fun run() {
            if (elapsedSeconds >= STORM_DURATION_SECONDS) {
                cancel()
                return
            }
            strikeRandomPlayer(players)
            elapsedSeconds += STRIKE_INTERVAL_SECONDS
        }
    }
}