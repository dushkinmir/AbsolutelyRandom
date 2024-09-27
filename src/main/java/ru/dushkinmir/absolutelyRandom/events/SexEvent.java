package ru.dushkinmir.absolutelyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
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

import java.util.Random;
import java.util.UUID;
import java.util.List;

public class SexEvent implements Listener {

    private final CollisionManager collisionManager;
    private final AnalFissureManager analFissureManager;

    public SexEvent(Plugin plugin) {
        this.collisionManager = new CollisionManager(); // Инициализируем менеджер коллизий
        this.analFissureManager = new AnalFissureManager(plugin); // Инициализируем менеджер анальной трещины
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

        Player standingPlayer = playerMoves ? targetPlayer : player;
        Player movingPlayer = playerMoves ? player : targetPlayer;

        // Телепортируем и двигаем игроков
        teleportBehind(standingPlayer, movingPlayer);
        movePlayer(movingPlayer, plugin);
        standingPlayer.sendMessage("Вы остаетесь на месте.");
        movingPlayer.sendMessage("Вы будете двигаться.");

        // С шансом 50/50 даем анальную трещину стоячему игроку
        if (random.nextBoolean()) {
            analFissureManager.giveAnalFissure(standingPlayer);
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

    // Класс для управления анальной трещиной
    private static class AnalFissureManager implements Listener {

        private final Plugin plugin;
        private final FileConfiguration config;

        public AnalFissureManager(Plugin plugin) {
            this.plugin = plugin;
            this.config = plugin.getConfig(); // Загружаем конфиг
            // Регистрируем события
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        // Метод для добавления игроку анальной трещины
        public void giveAnalFissure(Player player) {
            UUID playerId = player.getUniqueId();
            List<String> affectedPlayers = config.getStringList("analFissures");

            if (!affectedPlayers.contains(playerId.toString())) {
                affectedPlayers.add(playerId.toString());
                config.set("analFissures", affectedPlayers);
                plugin.saveConfig();

                player.sendMessage("Вам нанесена анальная трещина! Вы не сможете сидеть в транспорте и спать на кровати в течение 2 игровых дней.");

                // Запрещаем садиться в транспорт и спать на кровати
                Bukkit.getScheduler().runTaskLater(plugin, () -> removeAnalFissure(player), 2 * 24000L); // 2 игровых дня = 48 минут
            }
        }

        // Метод для удаления анальной трещины
        public void removeAnalFissure(Player player) {
            UUID playerId = player.getUniqueId();
            List<String> affectedPlayers = config.getStringList("analFissures");

            if (affectedPlayers.contains(playerId.toString())) {
                affectedPlayers.remove(playerId.toString());
                config.set("analFissures", affectedPlayers);
                plugin.saveConfig();

                player.sendMessage("Ваша анальная трещина зажила, теперь вы можете садиться в транспорт и спать на кровати.");
            }
        }

        // Проверка, есть ли у игрока анальная трещина
        public boolean hasAnalFissure(Player player) {
            UUID playerId = player.getUniqueId();
            List<String> affectedPlayers = config.getStringList("analFissures");
            return affectedPlayers.contains(playerId.toString());
        }

        // Обработчик запрета спать на кровати
        @EventHandler
        public void onPlayerBedEnter(PlayerBedEnterEvent event) {
            Player player = event.getPlayer();
            if (hasAnalFissure(player)) {
                event.setCancelled(true); // Отменяем вход в кровать
                player.sendMessage("Из-за анальной трещины вы не можете спать в кровати.");
            }
        }

        // Обработчик запрета сидения в транспорте
        @EventHandler
        public void onVehicleEnter(VehicleEnterEvent event) {
            if (event.getEntered() instanceof Player player) {
                if (hasAnalFissure(player)) {
                    event.setCancelled(true); // Отменяем посадку в транспорт
                    player.sendMessage("Из-за анальной трещины вы не можете сесть в транспорт.");
                }
            }
        }
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
}
