package ru.dushkinmir.absolutelyRandom.events;

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
    private final Plugin plugin;

    public AnalFissureHandler(SQLiteDatabase database, Plugin plugin) throws SQLException {
        this.database = database;
        this.plugin = plugin;
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS analFissures ("
                + "playerName TEXT PRIMARY KEY,"
                + "sleeps INTEGER DEFAULT 0"
                + ");";
        database.getConnection().createStatement().execute(sql);
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
                plugin.getLogger().severe("Не удалось checkForFissure в базу данных!");
                plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
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
            plugin.getLogger().severe("Не удалось incrementSleepCount в базу данных!");
            plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
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
            plugin.getLogger().severe("Не удалось checkFissureState в базу данных!");
            plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
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
                plugin.getLogger().info(player.getName() + " пытается сесть в транспорт.");
                var resultSet = database.getConnection().createStatement()
                        .executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '" + player.getName() + "'");
                if (resultSet.next() && resultSet.getInt("sleeps") <= 2) {
                    event.setCancelled(true); // Отменяем событие, если у игрока анальная трещина
                    player.sendMessage("Вы не можете сесть в транспорт, у вас анальная трещина!");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Не удалось onPlayerEnterVehicle в базу данных!");
                plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
            }
        }
    }
}
