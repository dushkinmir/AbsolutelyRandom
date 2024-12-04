package ru.dushkinmir.absolutelyRandom.features.warp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.utils.DatabaseManager
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.sql.SQLException

class WarpManager(private val database: DatabaseManager, private val plugin: Plugin) {

    init {
        setupTable()
    }

    private fun setupTable() {
        try {
            database.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS warps (player_uuid TEXT, warp_name TEXT, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT);"
                    )
                }
            }
        } catch (e: SQLException) {
            plugin.logger.severe("Ошибка при создании таблицы варпов: \n${e.message}")
        }
    }

    fun createWarp(player: Player, warpName: String, location: Location) {
        // Проверка на существование варпа
        if (warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Варп с именем '$warpName' уже существует.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }

        // Получаем предмет из конфига
        val requiredItemName: String? =
            plugin.config.getString("warp.requiredItem", "DIAMOND") // Значение по умолчанию DIAMOND
        val requiredMaterial = Material.matchMaterial(requiredItemName.toString())
        if (requiredMaterial == null) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Конфигурация содержит неверный материал для варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }
        val requiredItem = ItemStack(requiredMaterial)

        if (!player.inventory.containsAtLeast(requiredItem, requiredItem.amount)) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("У вас нет необходимого предмета для создания варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }

        val heldItem = player.inventory.itemInMainHand
        if (!heldItem.isSimilar(requiredItem)) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Вы должны держать необходимый предмет в руке.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }

        player.inventory.removeItem(requiredItem)
        try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO warps (player_uuid, warp_name, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?);"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    ps.setDouble(3, location.x)
                    ps.setDouble(4, location.y)
                    ps.setDouble(5, location.z)
                    ps.setFloat(6, location.yaw) // Сохраняем направление взгляда (yaw)
                    ps.setFloat(7, location.pitch) // Сохраняем направление взгляда (pitch)
                    ps.executeUpdate()
                    PlayerUtils.sendMessageToPlayer(
                        player, Component.text("Варп '$warpName' успешно создан!")
                            .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT
                    )
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при создании варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при создании варпа: \n${e.message}")
        }
    }

    fun teleportToWarp(player: Player, warpName: String) {
        if (!warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Варп с именем '$warpName' не найден.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }

        try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "SELECT x, y, z, yaw, pitch FROM warps WHERE player_uuid = ? AND warp_name = ?;"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    ps.executeQuery().use { rs ->
                        if (rs.next()) {
                            val location = Location(
                                player.world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                                rs.getFloat("yaw"), rs.getFloat("pitch")
                            )
                            player.teleport(location)
                            PlayerUtils.sendMessageToPlayer(
                                player, Component.text("Вы телепортировались к варпу '$warpName'.")
                                    .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT
                            )
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при телепортации.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при телепортации: \n${e.message}")
        }
    }

    fun deleteWarp(player: Player, warpName: String) {
        if (!warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Варп с именем '$warpName' не найден.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            return
        }

        try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "DELETE FROM warps WHERE player_uuid = ? AND warp_name = ?;"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    val rowsAffected = ps.executeUpdate()
                    if (rowsAffected > 0) {
                        PlayerUtils.sendMessageToPlayer(
                            player, Component.text("Варп '$warpName' успешно удален.")
                                .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при удалении варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при удалении варпа: \n${e.message}")
        }
    }

    fun deleteAllWarps(player: Player) {
        try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "DELETE FROM warps WHERE player_uuid = ?;"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    val rowsAffected = ps.executeUpdate() // Выполняем запрос и получаем количество затронутых строк
                    if (rowsAffected > 0) {
                        PlayerUtils.sendMessageToPlayer(
                            player, Component.text("Все ваши варпы успешно удалены.")
                                .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT
                        )
                    } else {
                        PlayerUtils.sendMessageToPlayer(
                            player, Component.text("У вас нет варпов для удаления.")
                                .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
                        )
                    }
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при удалении всех варпов.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при удалении всех варпов: \n${e.message}")
        }
    }

    fun getWarps(player: Player, includeCoordinates: Boolean): List<String> {
        val warps = ArrayList<String>()
        try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "SELECT warp_name, x, y, z FROM warps WHERE player_uuid = ?;"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val warpName = rs.getString("warp_name")
                            if (includeCoordinates) {
                                val x = rs.getInt("x")
                                val y = rs.getInt("y")
                                val z = rs.getInt("z")
                                warps.add("$warpName ($x, $y, $z)")
                            } else {
                                warps.add(warpName)
                            }
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при получении списка варпов.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при получении списка варпов: \n${e.message}")
        }
        return warps
    }

    private fun warpExists(player: Player, warpName: String): Boolean {
        return try {
            database.connection.use { connection ->
                connection.prepareStatement(
                    "SELECT COUNT(*) FROM warps WHERE player_uuid = ? AND warp_name = ?;"
                ).use { ps ->
                    ps.setString(1, player.uniqueId.toString())
                    ps.setString(2, warpName)
                    ps.executeQuery().use { rs ->
                        rs.next() && rs.getInt(1) > 0 // Если найден хотя бы один варп с таким именем
                    }
                }
            }
        } catch (e: SQLException) {
            PlayerUtils.sendMessageToPlayer(
                player, Component.text("Ошибка при проверке существования варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT
            )
            plugin.logger.severe("Ошибка при проверке существования варпа: \n${e.message}")
            false
        }
    }
}