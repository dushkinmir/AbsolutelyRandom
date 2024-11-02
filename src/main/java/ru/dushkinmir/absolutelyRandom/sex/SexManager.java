package ru.dushkinmir.absolutelyRandom.sex;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.Random;

public class SexManager {

    private static final CollisionManager collisionManager = new CollisionManager();
    private static final long SEX_DURATION = 15;

    public static void triggerSexEvent(Player initiator, Player targetPlayer, Plugin plugin, AnalFissureHandler fissureHandler) {

        // Проверяем, что целевой игрок существует и в сети
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            PlayerUtils.sendMessageToPlayer(
                    initiator,
                    Component.text("Игрок не найден или не в сети."),
                    PlayerUtils.MessageType.CHAT);
            return;
        }

        // Проверяем расстояние между игроками
        if (initiator.getLocation().distance(targetPlayer.getLocation()) > 1.5) {
            PlayerUtils.sendMessageToPlayer(
                    initiator,
                    Component.text("Игрок слишком далеко. Подойдите ближе."),
                    PlayerUtils.MessageType.CHAT);
            return;
        }

        // Отключаем коллизии для обоих игроков
        collisionManager.addPlayerToNoCollision(initiator);
        collisionManager.addPlayerToNoCollision(targetPlayer);

        // Определяем, кто из них будет двигаться
        boolean initiatorMoves = new Random().nextBoolean(); // true - движется инициатор, false - целевой игрок

        if (initiatorMoves) {
            performMovement(initiator, targetPlayer, plugin);
            fissureHandler.checkForFissure(targetPlayer); // Проверяем фиссуры для целевого игрока
        } else {
            performMovement(targetPlayer, initiator, plugin);
            fissureHandler.checkForFissure(initiator); // Проверяем фиссуры для инициатора
        }

        // Включаем коллизии обратно через 15 секунд
        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    collisionManager.removePlayerFromNoCollision(initiator);
                    collisionManager.removePlayerFromNoCollision(targetPlayer);
                },
                SEX_DURATION * 20L // 15 секунд
        );
    }

    private static void performMovement(Player movingPlayer, Player stationaryPlayer, Plugin plugin) {
        teleportBehind(stationaryPlayer, movingPlayer);
        movePlayer(movingPlayer, plugin);

        PlayerUtils.sendMessageToPlayer(
                movingPlayer,
                Component.text("<%s> даа мой папочка, накажи меня!!~".formatted(stationaryPlayer.getName())),
                PlayerUtils.MessageType.CHAT);
        PlayerUtils.sendMessageToPlayer(
                stationaryPlayer,
                Component.text("<%s> ода, давай сучка, нагибайся)".formatted(movingPlayer.getName())),
                PlayerUtils.MessageType.CHAT);
    }


    // Метод для движения игрока вперед-назад на ограниченное время (в секундах)
    private static void movePlayer(Player player, Plugin plugin) {
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
                SEX_DURATION * 20L // 15 секунд = 300 тиков
        );
    }    // Метод для телепортации игрока "движущегося" за спину "стоячего"

    private static void teleportBehind(Player stationaryPlayer, Player movingPlayer) {
        Location stationaryLocation = stationaryPlayer.getLocation();
        Vector backwardDirection = stationaryLocation.getDirection().multiply(-1); // Вектор, указывающий назад

        // Рассчитываем новую позицию для телепортации игрока на 1.5 блока позади
        Location teleportLocation = stationaryLocation.clone().add(backwardDirection.normalize().multiply(1.1));
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
