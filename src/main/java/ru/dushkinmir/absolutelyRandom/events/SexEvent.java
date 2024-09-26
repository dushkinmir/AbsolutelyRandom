package ru.dushkinmir.absolutelyRandom.events;

import dev.geco.gsit.api.event.PreEntitySitEvent;
import dev.geco.gsit.api.event.PrePlayerPoseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.HashMap;
import java.util.Random;

public class SexEvent {

    private final CollisionManager collisionManager;
    private final AnalFissureManager analFissureManager;
    private static HashMap<Player, Long> lastMessageTime; // Хранит время последнего сообщения для игрока
    private static final long messageCooldown = 100; // 5 секунд (5000 миллисекунд)

    public SexEvent(Plugin plugin) {
        this.collisionManager = new CollisionManager(); // Инициализируем менеджер коллизий
        this.analFissureManager = new AnalFissureManager(plugin); // Передаем плагин в менеджер анальной трещины
        lastMessageTime = new HashMap<>();
    }

    public void triggerSexEvent(Player player, String targetName, Plugin plugin) {
        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            player.sendMessage("Игрок не найден или не в сети.");
            return;
        }

        // Проверяем расстояние между игроками
        if (player.getLocation().distance(targetPlayer.getLocation()) > 1.5) {
            player.sendMessage("Игрок слишком далеко. Подойдите ближе.");
            return;
        }
        // Отключаем коллизии для обоих игроков
        collisionManager.addPlayerToNoCollision(player);
        collisionManager.addPlayerToNoCollision(targetPlayer);

        // Рандом определяет, кто из них будет стоять, а кто двигаться
        Random random = new Random();
        boolean playerMoves = random.nextBoolean(); // true - движется вызывающий игрок, false - целевой

        if (playerMoves) {
            teleportBehind(targetPlayer, player);
            movePlayer(player, plugin);
            targetPlayer.sendMessage("Вы остаетесь на месте.");
            analFissureManager.checkForAnalFissure(targetPlayer);
        } else {
            teleportBehind(player, targetPlayer);
            movePlayer(targetPlayer, plugin);
            player.sendMessage("Вы остаетесь на месте.");
            analFissureManager.checkForAnalFissure(player);
        }
        // Через 15 секунд включаем коллизии обратно
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    collisionManager.removePlayerFromNoCollision(player);
                    collisionManager.removePlayerFromNoCollision(targetPlayer);
                },
                15 * 20L // 15 секунд
        );
    }

    // Метод для движения игрока вперед-назад на ограниченное время (в секундах)
    private void movePlayer(Player player, Plugin plugin) {
        player.sendMessage("Вы будете двигаться вперед-назад!");

        // Устанавливаем движение вперед-назад
        int taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                new Runnable() {
                    private boolean moveForward = true;

                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }

                        // Получаем вектор скорости игрока (текущий вектор движения)
                        Location currentLocation = player.getLocation();
                        Vector direction = currentLocation.getDirection().normalize(); // Получаем направление взгляда и нормализуем

                        // Обнуляем вертикальную составляющую
                        direction.setY(0); // Убираем вертикальную составляющую
                        direction = direction.normalize(); // Нормализуем снова после обнуления Y

                        if (moveForward) {
                            player.setVelocity(direction.multiply(0.25)); // Двигаем вперед
                        } else {
                            player.setVelocity(direction.multiply(-0.25)); // Двигаем назад
                        }

                        moveForward = !moveForward; // Меняем направление
                    }
                },
                0L, // Начало сразу
                5L  // Выполняем каждые 5 тиков (1/4 секунды)
        ).getTaskId();

        // Останавливаем задачу через 15 секунд (300 тиков)
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> Bukkit.getScheduler().cancelTask(taskId),
                15 * 20L // 15 секунд = 300 тиков
        );
    }

    // Метод для телепортации игрока "движущегося" за спину "стоячего"
    private void teleportBehind(Player stationaryPlayer, Player movingPlayer) {
        Location stationaryLocation = stationaryPlayer.getLocation();
        Vector backwardDirection = stationaryLocation.getDirection().multiply(-1); // Вектор, указывающий назад

        // Рассчитываем новую позицию для телепортации игрока на 1.5 блока позади
        Location teleportLocation = stationaryLocation.clone().add(backwardDirection.normalize().multiply(1.5));
        teleportLocation.setY(stationaryLocation.getY()); // Оставляем высоту на том же уровне

        // Телепортируем игрока
        movingPlayer.teleport(teleportLocation);
    }

    private static class CollisionManager {

        private Team noCollisionTeam;

        public CollisionManager() {
            setupNoCollisionTeam();
        }

        // Создаём команду с отключёнными коллизиями
        private void setupNoCollisionTeam() {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getMainScoreboard();

            // Проверяем, существует ли команда уже
            noCollisionTeam = board.getTeam("noCollision");
            if (noCollisionTeam == null) {
                // Создаём новую команду с именем "noCollision"
                noCollisionTeam = board.registerNewTeam("noCollision");
                noCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER); // Отключаем коллизии
            }
        }

        // Добавляем игрока в команду без коллизий
        public void addPlayerToNoCollision(Player player) {
            if (noCollisionTeam != null && !noCollisionTeam.hasEntry(player.getName())) {
                noCollisionTeam.addEntry(player.getName());
            }
        }

        // Удаляем игрока из команды (включение коллизий)
        public void removePlayerFromNoCollision(Player player) {
            if (noCollisionTeam != null && noCollisionTeam.hasEntry(player.getName())) {
                noCollisionTeam.removeEntry(player.getName());
            }
        }
    }

    // Менеджер для управления анальными трещинами
    public static class AnalFissureManager implements Listener {
        private final Connection connection;

        public AnalFissureManager(Plugin plugin) {
            this.connection = initDatabase(); // Инициализируем базу данных

            // Регистрируем слушатели событий
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        private Connection initDatabase() {
            try {
                // Соединение с SQLite базой данных
                Connection conn = DriverManager.getConnection("jdbc:sqlite:anal_fissures.db");
                Statement stmt = conn.createStatement();

                // Создаем таблицу для хранения статусов анальных трещин
                String sql = "CREATE TABLE IF NOT EXISTS fissures (" +
                        "player_name TEXT PRIMARY KEY, " +
                        "has_fissure INTEGER, " +
                        "heal_time BIGINT)";
                stmt.executeUpdate(sql);
                stmt.close();

                return conn;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        // Проверяем шанс появления анальной трещины и записываем в базу данных
        public void checkForAnalFissure(Player player) {
            Random random = new Random();
            long healTime = System.currentTimeMillis() + (2 * 24000 * 50); // 2 игровых дня (каждый день ~20 минут в реальном времени)
            try {
                PreparedStatement stmt = connection.prepareStatement("INSERT OR REPLACE INTO fissures (player_name, has_fissure, heal_time) VALUES (?, ?, ?)");
                stmt.setString(1, player.getName());
                stmt.setInt(2, 1);
                stmt.setLong(3, healTime);
                stmt.executeUpdate();
                stmt.close();

                player.sendMessage("Вы получили анальную трещину! Она заживет через 2 игровых дня.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Проверяем, может ли игрок сидеть/спать
        private boolean hasAnalFissure(Player player) {
            try {
                PreparedStatement stmt = connection.prepareStatement("SELECT has_fissure, heal_time FROM fissures WHERE player_name = ?");
                stmt.setString(1, player.getName());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int hasFissure = rs.getInt("has_fissure");
                    long healTime = rs.getLong("heal_time");

                    if (hasFissure == 1 && System.currentTimeMillis() < healTime) {
                        return true; // Анальная трещина ещё не зажила
                    } else if (System.currentTimeMillis() >= healTime) {
                        // Трещина зажила, обновляем запись
                        PreparedStatement updateStmt = connection.prepareStatement("UPDATE fissures SET has_fissure = 0 WHERE player_name = ?");
                        updateStmt.setString(1, player.getName());
                        updateStmt.executeUpdate();
                        updateStmt.close();
                    }
                }

                rs.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        }

        // Событие: игрок пытается сесть в транспорт
        @EventHandler
        public void onVehicleEnter(VehicleEnterEvent event) {
            if (event.getEntered() instanceof Player player) {
                if (hasAnalFissure(player)) {
                    event.setCancelled(true);
                    sendFissureMessage(player);
                }
            }
        }

        // Событие: игрок пытается лечь в кровать
        @EventHandler
        public void onBedEnter(PlayerBedEnterEvent event) {
            Player player = event.getPlayer();
            if (hasAnalFissure(player)) {
                event.setCancelled(true);
                sendFissureMessage(player);

            }
        }

        @EventHandler
        public void onGSitSit(PreEntitySitEvent event){
            Entity entity = event.getEntity();
            if (entity.getType() == EntityType.PLAYER) {
                Player player = (Player) entity;
                if (hasAnalFissure(player)) {
                    event.setCancelled(true);
                    sendFissureMessage(player);
                }
            }
        }

        @EventHandler
        public void onGSitPose(PrePlayerPoseEvent event){
            Player player = event.getPlayer();
            if (hasAnalFissure(player)) {
                event.setCancelled(true);
                sendFissureMessage(player);
            }
        }

        // Метод для отправки сообщения об анальной трещине
        private void sendFissureMessage(Player player) {
            long currentTime = System.currentTimeMillis();
            if (!lastMessageTime.containsKey(player) || (currentTime - lastMessageTime.get(player)) > messageCooldown) {
                player.sendMessage("Из-за анальной трещины вы не можете сидеть/спать!");
                lastMessageTime.put(player, currentTime); // Обновляем время последнего сообщения
            }
        }
    }
}
