package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Random;

public class SexEvent {

    private final CollisionManager collisionManager;
    private final AnalFissureManager analFissureManager;

    public SexEvent() {
        this.collisionManager = new CollisionManager(); // Инициализируем менеджер коллизий
        this.analFissureManager = new AnalFissureManager();

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
    }    // Метод для телепортации игрока "движущегося" за спину "стоячего"

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
}



