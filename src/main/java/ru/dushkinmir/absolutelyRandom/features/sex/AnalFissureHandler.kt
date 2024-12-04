package ru.dushkinmir.absolutelyRandom.features.sex

import dev.geco.gsit.api.event.PreEntitySitEvent
import dev.geco.gsit.api.event.PrePlayerPlayerSitEvent
import dev.geco.gsit.api.event.PrePlayerPoseEvent
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.bossbar.BossBar.Color
import net.kyori.adventure.bossbar.BossBar.Overlay
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.utils.DatabaseManager
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.sql.SQLException
import java.util.*

class AnalFissureHandler(private val database: DatabaseManager, private val plugin: Plugin) : Listener {
    private val random = Random()
    private val playerBossBars: MutableMap<Player, BossBar> = HashMap()

    init {
        createTable()
        if (isGsitPluginAvailable()) {
            plugin.server.pluginManager.registerEvents(GSitEventHandlers(), plugin)
        }
    }

    @Throws(SQLException::class)
    private fun createTable() {
        val sql = "CREATE TABLE IF NOT EXISTS analFissures (" +
                "playerName TEXT PRIMARY KEY," +
                "sleeps INTEGER DEFAULT 0" +
                ");"
        database.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sql)
            }
        }
    }

    private fun isGsitPluginAvailable(): Boolean {
        return plugin.server.pluginManager.getPlugin("GSit") != null
    }

    fun checkForFissure(player: Player) {
        if (random.nextInt(100) < 33) {
            try {
                database.connection.use { conn ->
                    conn.createStatement().use { stmt ->
                        stmt.executeUpdate("INSERT OR REPLACE INTO analFissures (playerName, sleeps) VALUES ('${player.name}', 0)")

                        // Создание и регистрация нового боссбара
                        val bossBar = BossBar.bossBar(
                            Component.text("Оставшиеся дни до выздоровления"),
                            1.0f, // стартовое значение прогресса (например 100%)
                            Color.RED,
                            Overlay.PROGRESS
                        )
                        playerBossBars[player] = bossBar
                        plugin.server.showBossBar(bossBar)

                        PlayerUtils.sendMessageToPlayer(
                            player,
                            Component.text("боже, лох, у тебя анальная трещина теперь!"),
                            PlayerUtils.MessageType.ACTION_BAR
                        )
                    }
                }
            } catch (e: SQLException) {
                plugin.logger.severe("Не удалось checkForFissure в базу данных!")
                plugin.logger.severe("Ошибка: " + e.message)
            }
        }
    }

    fun incrementSleepCount(player: Player) {
        try {
            database.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeUpdate("UPDATE analFissures SET sleeps = sleeps + 1 WHERE playerName = '${player.name}'")
                    checkFissureState(player)
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Не удалось incrementSleepCount в базу данных!")
            plugin.logger.severe("Ошибка: " + e.message)
        }
    }

    fun checkFissureState(player: Player) {
        try {
            database.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    val resultSet =
                        stmt.executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '${player.name}'")
                    if (resultSet.next()) {
                        val sleeps = resultSet.getInt("sleeps")
                        val progress = (2 - sleeps) / 2.0f // обновление прогресса

                        playerBossBars[player]?.progress(progress)

                        if (sleeps >= 2) {
                            PlayerUtils.sendMessageToPlayer(
                                player,
                                Component.text("анальная трещина зажила, радуйся"),
                                PlayerUtils.MessageType.ACTION_BAR
                            )
                            stmt.executeUpdate("DELETE FROM analFissures WHERE playerName = '${player.name}'")

                            // Удаление боссбара
                            playerBossBars[player]?.let { plugin.server.hideBossBar(it) }
                            playerBossBars.remove(player)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Не удалось checkFissureState в базу данных!")
            plugin.logger.severe("Ошибка: " + e.message)
        }
    }

    private fun isFissureActive(player: Player): Boolean {
        try {
            database.connection.use { conn ->
                conn.createStatement().use { stmt ->
                    val resultSet =
                        stmt.executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '${player.name}'")
                    return resultSet.next() && resultSet.getInt("sleeps") <= 2
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Ошибка при проверке состояния анальной трещины: " + e.message)
            return false
        }
    }

    @EventHandler
    fun onPlayerSleep(event: PlayerBedEnterEvent) {
        val player = event.player
        incrementSleepCount(player)
    }

    @EventHandler
    fun onPlayerEnterVehicle(event: VehicleEnterEvent) {
        if (event.entered is Player) {
            val player = event.entered as Player
            plugin.logger.info("${player.name} пытается сесть в транспорт.")
            if (isFissureActive(player)) {
                event.isCancelled = true
                PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("у тя трещина, дурачок"),
                    PlayerUtils.MessageType.ACTION_BAR
                )
            }
        }
    }

    // GSit Events
    private inner class GSitEventHandlers : Listener {
        @EventHandler
        fun onGSitPlayerSit(event: PreEntitySitEvent) {
            if (event.entity is Player) {
                val player = event.entity as Player
                handleGSitEvent(player, event)
            }
        }

        @EventHandler
        fun onGSitPlayerPlayerSit(event: PrePlayerPlayerSitEvent) {
            val player = event.player
            handleGSitEvent(player, event)
        }

        @EventHandler
        fun onGSitPlayerPose(event: PrePlayerPoseEvent) {
            val player = event.player
            handleGSitEvent(player, event)
        }

        private fun handleGSitEvent(player: Player, event: org.bukkit.event.Event) {
            if (isFissureActive(player)) {
                when (event) {
                    is PreEntitySitEvent -> event.isCancelled = true
                    is PrePlayerPlayerSitEvent -> event.isCancelled = true
                    is PrePlayerPoseEvent -> event.isCancelled = true
                }
                PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("у тя трещина, дурачок"),
                    PlayerUtils.MessageType.ACTION_BAR
                )
            }
        }
    }
}