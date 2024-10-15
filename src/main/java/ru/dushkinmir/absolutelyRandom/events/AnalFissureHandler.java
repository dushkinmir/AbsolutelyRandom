package ru.dushkinmir.absolutelyRandom.events;

import dev.geco.gsit.api.event.PreEntitySitEvent;
import dev.geco.gsit.api.event.PrePlayerPlayerSitEvent;
import dev.geco.gsit.api.event.PrePlayerPoseEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.AbsRandSQLiteDatabase;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.sql.SQLException;
import java.util.Random;

public class AnalFissureHandler implements Listener {
    private static final Random random = new Random();
    private final AbsRandSQLiteDatabase database;
    private final Plugin plugin;

    public AnalFissureHandler(AbsRandSQLiteDatabase database, Plugin plugin) throws SQLException {
        this.database = database;
        this.plugin = plugin;
        createTable();
        // Проверка на наличие зависимости от GSit
        if (isGsitPluginAvailable()) {
            plugin.getServer().getPluginManager().registerEvents(new GSitEventHandlers(), plugin);
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS analFissures ("
                + "playerName TEXT PRIMARY KEY,"
                + "sleeps INTEGER DEFAULT 0"
                + ");";
        database.getConnection().createStatement().execute(sql);
    }

    private boolean isGsitPluginAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("GSit") != null;
    }

    public void checkForFissure(Player player) {
        // 33% шанс появления анальной трещины
        if (random.nextInt(100) < 33) {
            try {
                // Запись в базу данных
                database.getConnection().createStatement()
                        .executeUpdate("INSERT OR REPLACE INTO analFissures (playerName, sleeps) VALUES ('" + player.getName() + "', 0)");
                PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text("боже, лох, у тебя анальная трещина теперь!"),
                        PlayerUtils.MessageType.ACTION_BAR
                );
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
                    PlayerUtils.sendMessageToPlayer(
                            player,
                            Component.text("анальная трещина зажила, радуйся"),
                            PlayerUtils.MessageType.ACTION_BAR
                    );
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

    private boolean isFissureActive(Player player) {
        try {
            var resultSet = database.getConnection().createStatement().executeQuery("SELECT sleeps FROM analFissures WHERE playerName = '" + player.getName() + "'");
            return resultSet.next() && resultSet.getInt("sleeps") <= 2;
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка при проверке состояния анальной трещины: " + e.getMessage());
            return false; // В случае ошибки возвращаем false
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
            plugin.getLogger().info(player.getName() + " пытается сесть в транспорт.");
            if (isFissureActive(player)) {
                event.setCancelled(true); // Отменяем событие, если у игрока анальная трещина
                PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text("у тя трещина, дурачок"),
                        PlayerUtils.MessageType.ACTION_BAR
                );
            }
        }
    }

    // GSit Events
    private class GSitEventHandlers implements Listener {
        @EventHandler
        public void onGSitPlayerSit(PreEntitySitEvent event) {
            if (event.getEntity() instanceof Player player) {
                handleGSitEvent(player, event);
            }
        }

        @EventHandler
        public void onGSitPlayerPlayerSit(PrePlayerPlayerSitEvent event) {
            Player player = event.getPlayer();
            handleGSitEvent(player, event);
        }

        @EventHandler
        public void onGSitPlayerPose(PrePlayerPoseEvent event) {
            Player player = event.getPlayer();
            handleGSitEvent(player, event);
        }

        private void handleGSitEvent(Player player, org.bukkit.event.Event event) {
            if (isFissureActive(player)) {
                if (event instanceof PreEntitySitEvent) {
                    ((PreEntitySitEvent) event).setCancelled(true);
                } else if (event instanceof PrePlayerPlayerSitEvent) {
                    ((PrePlayerPlayerSitEvent) event).setCancelled(true);
                } else if (event instanceof PrePlayerPoseEvent) {
                    ((PrePlayerPoseEvent) event).setCancelled(true);
                }
                PlayerUtils.sendMessageToPlayer(player, Component.text("у тя трещина, дурачок"), PlayerUtils.MessageType.ACTION_BAR);
            }
        }
    }
}
