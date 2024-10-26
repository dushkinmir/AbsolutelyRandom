package ru.dushkinmir.absolutelyRandom.warp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.AbsRandSQLiteDatabase;

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
                    "CREATE TABLE IF NOT EXISTS warps (player_uuid TEXT, warp_name TEXT, x DOUBLE, y DOUBLE, z DOUBLE);"
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при создании таблицы варпов: " + e.getMessage());
        }
    }

    public void createWarp(Player player, String warpName, Location location, ItemStack requiredItem) {
        if (!player.getInventory().containsAtLeast(requiredItem, requiredItem.getAmount())) {
            player.sendMessage("У вас нет необходимого предмета для создания варпа.");
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!heldItem.isSimilar(requiredItem)) {
            player.sendMessage("Вы должны держать необходимый предмет в руке.");
            return;
        }

        player.getInventory().removeItem(requiredItem);
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "INSERT INTO warps (player_uuid, warp_name, x, y, z) VALUES (?, ?, ?, ?, ?);")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            ps.setDouble(3, location.getX());
            ps.setDouble(4, location.getY());
            ps.setDouble(5, location.getZ());
            ps.executeUpdate();
            player.sendMessage("Варп '" + warpName + "' успешно создан!");
        } catch (SQLException e) {
            player.sendMessage("Ошибка при создании варпа.");
            plugin.getLogger().severe("Ошибка при создании варпа.");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
        }
    }

    public void teleportToWarp(Player player, String warpName) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT x, y, z FROM warps WHERE player_uuid = ? AND warp_name = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Location location = new Location(player.getWorld(), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"));
                player.teleport(location);
                player.sendMessage("Вы телепортировались к варпу '" + warpName + "'.");
            } else {
                player.sendMessage("Варп с именем '" + warpName + "' не найден.");
            }
        } catch (SQLException e) {
            player.sendMessage("Ошибка при телепортации.");
            plugin.getLogger().severe("Ошибка при телепортации.");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
        }
    }

    public void deleteWarp(Player player, String warpName) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM warps WHERE player_uuid = ? AND warp_name = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, warpName);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                player.sendMessage("Варп '" + warpName + "' успешно удален.");
            } else {
                player.sendMessage("Варп с именем '" + warpName + "' не найден.");
            }
        } catch (SQLException e) {
            player.sendMessage("Ошибка при удалении варпа.");
            plugin.getLogger().severe("Ошибка при удалении варпа.");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
        }
    }

    public void deleteAllWarps(Player player) {
        try (Connection connection = database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "DELETE FROM warps WHERE player_uuid = ?;")) {
            ps.setString(1, player.getUniqueId().toString());
            int rowsAffected = ps.executeUpdate();
            player.sendMessage(rowsAffected > 0 ? "Все ваши варпы успешно удалены." : "У вас нет варпов для удаления.");
        } catch (SQLException e) {
            player.sendMessage("Ошибка при удалении всех варпов.");
            plugin.getLogger().severe("Ошибка при удалении всех варпов.");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
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
            player.sendMessage("Ошибка при получении списка варпов.");
            plugin.getLogger().severe("Ошибка при получении списка варпов.");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
        }
        return warps;
    }
}
