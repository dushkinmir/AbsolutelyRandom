package ru.dushkinmir.absolutelyRandom.CBO;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import ru.dushkinmir.absolutelyRandom.events.AnalFissureHandler;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SexEvent {
    private static final CollisionManager collisionManager = new CollisionManager();
    private static final Map<String, Integer> activeTasks = new HashMap<>(); // Хранит активные таски

    public static void triggerSexEvent(Player player, Player targetPlayer, Plugin plugin, AnalFissureHandler fissureHandler) {

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("Игрок не найден или не в сети."),
                    PlayerUtils.MessageType.CHAT);
            return;
        }

        // Проверяем расстояние между игроками
        if (player.getLocation().distance(targetPlayer.getLocation()) > 1.5) {
            PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("Игрок слишком далеко. Подойдите ближе."),
                    PlayerUtils.MessageType.CHAT);
            return;
        }

        // Отключаем коллизии для обоих игроков
        collisionManager.addPlayerToNoCollision(player);
        collisionManager.addPlayerToNoCollision(targetPlayer);

        // Генерация ключа для идентификации пары игроков
        String taskKey = getTaskKey(player, targetPlayer);

        // Если уже существует активный таск для этой пары, отменяем его
        if (activeTasks.containsKey(taskKey)) {
            Bukkit.getScheduler().cancelTask(activeTasks.get(taskKey));
        }

        // Рандом определяет, кто из них будет стоять, а кто двигаться
        Random random = new Random();
        boolean playerMoves = random.nextBoolean(); // true - движется вызывающий игрок, false - целевой

        if (playerMoves) {
            teleportBehind(targetPlayer, player);
            int taskId = movePlayer(player, plugin);
            PlayerUtils.sendMessageToPlayer(
                    targetPlayer,
                    Component.text("<%s> даа мой папочка, накажи меня!!~".formatted(player.getName())),
                    PlayerUtils.MessageType.CHAT);
            activeTasks.put(taskKey, taskId); // Сохраняем ID нового таска
        } else {
            teleportBehind(player, targetPlayer);
            int taskId = movePlayer(targetPlayer, plugin);
            PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("<%s> ода, давай сучка, нагибайся)".formatted(targetPlayer.getName())),
                    PlayerUtils.MessageType.CHAT);
            fissureHandler.checkForFissure(player);
            activeTasks.put(taskKey, taskId); // Сохраняем ID нового таска
        }

        // Через 15 секунд включаем коллизии обратно
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    collisionManager.removePlayerFromNoCollision(player);
                    collisionManager.removePlayerFromNoCollision(targetPlayer);
                    activeTasks.remove(taskKey); // Удаляем таск из карты по истечении времени
                },
                15 * 20L // 15 секунд
        );
    }

    // Метод для движения игрока вперед-назад на ограниченное время (в секундах)
    private static int movePlayer(Player player, Plugin plugin) {
        // Устанавливаем движение вперед-назад
        return Bukkit.getScheduler().runTaskTimerAsynchronously(
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
    }

    // Метод для телепортации игрока "движущегося" за спину "стоячего"
    private static void teleportBehind(Player stationaryPlayer, Player movingPlayer) {
        Location stationaryLocation = stationaryPlayer.getLocation();
        Vector backwardDirection = stationaryLocation.getDirection().multiply(-1); // Вектор, указывающий назад

        // Рассчитываем новую позицию для телепортации игрока на 1.5 блока позади
        Location teleportLocation = stationaryLocation.clone().add(backwardDirection.normalize().multiply(1.5));
        teleportLocation.setY(stationaryLocation.getY()); // Оставляем высоту на том же уровне

        // Телепортируем игрока
        movingPlayer.teleport(teleportLocation);
    }

    // Метод для генерации ключа для пары игроков
    private static String getTaskKey(Player player1, Player player2) {
        return player1.getName() + "_" + player2.getName();
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
