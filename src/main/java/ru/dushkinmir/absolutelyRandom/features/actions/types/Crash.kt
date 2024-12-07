package ru.dushkinmir.absolutelyRandom.features.actions.types

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

class Crash : Action("crash") {

    companion object {
        private val RANDOM = Random()
        private const val GLITCH_DURATION_TICKS = 200 // Длительность хаоса
        private val DISCONNECT_MESSAGE = Component.text(
            "Critical error: Server crashed. Please try again later.", NamedTextColor.RED
        )
        private val GLITCH_BLOCK = Material.BARRIER // Используем барьер как эффект замены

    }

    override fun execute(plugin: Plugin) {
        // Шаг 1: Стартуем визуальный хаос
        startGlitches(plugin)

        // Шаг 2: Через некоторое время кикаем всех игроков
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            kickAllPlayersWithStyle()
        }, GLITCH_DURATION_TICKS.toLong())
    }

    private fun startGlitches(plugin: Plugin) {
        object : BukkitRunnable() {
            var iterations = 0

            override fun run() {
                if (iterations++ > 10) {
                    cancel() // Останавливаем хаос через 10 итераций
                    return
                }

                for (player in Bukkit.getOnlinePlayers()) {
                    createGlitchEffects(player)
                }
            }
        }.runTaskTimer(plugin, 0, 20) // Повторяем каждую секунду
    }

    private fun createGlitchEffects(player: Player) {
        // Телепортируем в случайную точку неподалёку
        val randomLocation = getRandomNearbyLocation(player.location, 20)
        player.teleport(randomLocation)

        // Отправляем "глючные" сообщения в чат
        val glitchMessage = generateRandomGlitchMessage(player.name)
        PlayerUtils.sendMessageToPlayer(
            player, Component.text(glitchMessage, NamedTextColor.DARK_RED),
            PlayerUtils.MessageType.ACTION_BAR
        )

        // Добавляем ослепление или другие эффекты
        player.addPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS.createEffect(40, 1))

        // Спавним молнии рядом
        val world = player.world
        world.strikeLightning(randomLocation)

        // Заменяем блоки вокруг игрока на блоки барьеров (только для него)
        replaceNearbyBlocksWithGlitch(player)
    }

    private fun replaceNearbyBlocksWithGlitch(player: Player) {
        val playerLocation = player.location
        playerLocation.world ?: return

        // Проверяем блоки вокруг игрока (в радиусе 5 блоков)
        for (x in -5..5) {
            for (y in -2..2) { // Небольшая высота для эффекта
                for (z in -5..5) {
                    val loc = playerLocation.clone().add(x.toDouble(), y.toDouble(), z.toDouble())

                    // Заменяем блок только для этого игрока, на барьер (или другой блок)
                    player.sendBlockChange(loc, GLITCH_BLOCK.createBlockData())
                }
            }
        }
    }

    private fun getRandomNearbyLocation(base: Location, range: Int): Location {
        base.world ?: return base

        val offsetX = RANDOM.nextInt(range * 2) - range
        val offsetZ = RANDOM.nextInt(range * 2) - range
        return base.clone().add(offsetX.toDouble(), 0.0, offsetZ.toDouble())
    }

    private fun generateRandomGlitchMessage(playerName: String): String {
        return "ERROR_" + RANDOM.nextInt(9999) + " Player " + playerName + " disconnected"
    }

    private fun kickAllPlayersWithStyle() {
        for (player in Bukkit.getOnlinePlayers()) {
            PlayerUtils.kickPlayer(player, DISCONNECT_MESSAGE)
        }
    }

    private fun setMotd(motd: Component) {
        Bukkit.getServer().motd(motd)
    }
}
