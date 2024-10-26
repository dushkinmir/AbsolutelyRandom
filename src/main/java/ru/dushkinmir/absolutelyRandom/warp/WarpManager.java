package ru.dushkinmir.absolutelyRandom.warp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.AbsRandSQLiteDatabase;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarpManager {
    private final AbsRandSQLiteDatabase database;
    private final Plugin plugin;

    public WarpManager(AbsRandSQLiteDatabase database, Plugin plugin) {
        this.database = database;
        this.plugin = plugin;
        setupTable();
    }

    private void setupTable() {
        try (Connection connection = database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS warps (player_uuid TEXT, warp_name TEXT, x DOUBLE, y DOUBLE, z DOUBLE, yaw FLOAT, pitch FLOAT);"
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при создании таблицы варпов: " + e.getMessage());
        }
    }

    public void createWarp(Player player, String warpName, Location location) {
        // Проверка на существование варпа
        if (warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Варп с именем '" + warpName + "' уже существует.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }

        // Получаем предмет из конфига
        String requiredItemName = plugin.getConfig().getString("warp.requiredItem", "DIAMOND"); // Значение по умолчанию DIAMOND
        Material requiredMaterial = Material.matchMaterial(requiredItemName);
        if (requiredMaterial == null) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Конфигурация содержит неверный материал для варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }
        ItemStack requiredItem = new ItemStack(requiredMaterial);

        if (!player.getInventory().containsAtLeast(requiredItem, requiredItem.getAmount())) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("У вас нет необходимого предмета для создания варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!heldItem.isSimilar(requiredItem)) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Вы должны держать необходимый предмет в руке.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }

        player.getInventory().removeItem(requiredItem);
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO warps (player_uuid, warp_name, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?);")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            ps.setDouble(3, location.getX());
            ps.setDouble(4, location.getY());
            ps.setDouble(5, location.getZ());
            ps.setFloat(6, location.getYaw());  // Сохраняем направление взгляда (yaw)
            ps.setFloat(7, location.getPitch()); // Сохраняем направление взгляда (pitch)
            ps.executeUpdate();
            PlayerUtils.sendMessageToPlayer(player, Component.text("Варп '" + warpName + "' успешно создан!")
                    .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT);
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при создании варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при создании варпа: " + e.getMessage());
        }
    }

    public void teleportToWarp(Player player, String warpName) {
        if (!warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Варп с именем '" + warpName + "' не найден.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }

        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT x, y, z, yaw, pitch FROM warps WHERE player_uuid = ? AND warp_name = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Location location = new Location(player.getWorld(), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"));
                player.teleport(location);
                PlayerUtils.sendMessageToPlayer(player, Component.text("Вы телепортировались к варпу '" + warpName + "'.")
                        .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT);
            }
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при телепортации.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при телепортации: " + e.getMessage());
        }
    }

    public void deleteWarp(Player player, String warpName) {
        if (!warpExists(player, warpName)) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Варп с именем '" + warpName + "' не найден.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            return;
        }

        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM warps WHERE player_uuid = ? AND warp_name = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                PlayerUtils.sendMessageToPlayer(player, Component.text("Варп '" + warpName + "' успешно удален.")
                        .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT);
            }
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при удалении варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при удалении варпа: " + e.getMessage());
        }
    }

    public void deleteAllWarps(Player player) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM warps WHERE player_uuid = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            int rowsAffected = ps.executeUpdate(); // Выполняем запрос и получаем количество затронутых строк
            if (rowsAffected > 0) {
                PlayerUtils.sendMessageToPlayer(player, Component.text("Все ваши варпы успешно удалены.")
                        .color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT);
            } else {
                PlayerUtils.sendMessageToPlayer(player, Component.text("У вас нет варпов для удаления.")
                        .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            }
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при удалении всех варпов.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при удалении всех варпов: " + e.getMessage());
        }
    }

    public List<String> getWarps(Player player) {
        List<String> warps = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT warp_name FROM warps WHERE player_uuid = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                warps.add(rs.getString("warp_name"));
            }
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при получении списка варпов.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при получении списка варпов: " + e.getMessage());
        }
        return warps;
    }

    private boolean warpExists(Player player, String warpName) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM warps WHERE player_uuid = ? AND warp_name = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0; // Если найден хотя бы один варп с таким именем
        } catch (SQLException e) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Ошибка при проверке существования варпа.")
                    .color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
            plugin.getLogger().severe("Ошибка при проверке существования варпа: " + e.getMessage());
            return false;
        }
    }
}
