package ru.dushkinmir.absolutelyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.SQLiteDatabase;

import java.sql.SQLException;
import java.util.Random;

public class AnalFissureHandler implements Listener {
    private static final Random random = new Random();
    private final SQLiteDatabase database;

    public AnalFissureHandler(SQLiteDatabase database, Plugin plugin) {
        this.database = database;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void checkForFissure(Player player) {
        // 33% шанс появления анальной трещины
        if (random.nextInt(100) < 33) {
            try {
                // Запись в базу данных
                database.getConnection().createStatement()
                        .executeUpdate("INSERT OR REPLACE INTO analFissures (playerName, sleeps) VALUES ('" + player.getName() + "', 0)");
                player.sendMessage("У вас появилась анальная трещина!");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void incrementSleepCount(Player player) {
        try {
            // Увеличиваем счетчик сна на 1
            database.getConnection().createStatement()
                    .executeUpdate("UPDATE analFissures SET sleeps = sleeps + 1 WHERE playerName = '" + player.getName() + "'");
            checkFissureState(player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void checkFissureState(Player player) {
        try {
            var resultSet = database.getConnection().createStatement()
                    .executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '" + player.getName() + "'");
            if (resultSet.next()) {
                if (resultSet.getInt("sleeps") >= 2) {
                    player.sendMessage("Ваша анальная трещина зажила!");
                    // Здесь можно удалить запись из базы данных, если это необходимо
                    database.getConnection().createStatement()
                            .executeUpdate("DELETE FROM analFissures WHERE playerName = '" + player.getName() + "'");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        incrementSleepCount(player);
    }

    @EventHandler
    public void onPlayerEnterVehicle(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player player) {
            try {
                Bukkit.getServer().getLogger().info(player.getName() + " пытается сесть в транспорт.");
                var resultSet = database.getConnection().createStatement()
                        .executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '" + player.getName() + "'");
                if (resultSet.next() && resultSet.getInt("sleeps") <= 2) {
                    event.setCancelled(true); // Отменяем событие, если у игрока анальная трещина
                    player.sendMessage("Вы не можете сесть в транспорт, у вас анальная трещина!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
