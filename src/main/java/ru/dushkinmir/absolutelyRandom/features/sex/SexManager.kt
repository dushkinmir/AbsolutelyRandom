package ru.dushkinmir.absolutelyRandom.features.sex

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Team
import ru.dushkinmir.absolutelyRandom.features.actions.types.Group.FallingBlocksTask
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

object SexManager {

    private val collisionManager = CollisionManager()
    private val SEX_DURATION: Long = 15

    fun triggerSexEvent(initiator: Player, targetPlayer: Player?, plugin: Plugin, fissureHandler: AnalFissureHandler) {

        // Проверяем, что целевой игрок существует и в сети
        if (targetPlayer == null || !targetPlayer.isOnline) {
            PlayerUtils.sendMessageToPlayer(
                initiator,
                Component.text("Игрок не найден или не в сети."),
                PlayerUtils.MessageType.CHAT
            )
            return
        }

        // Проверяем расстояние между игроками
        if (initiator.location.distance(targetPlayer.location) > 1.5) {
            PlayerUtils.sendMessageToPlayer(
                initiator,
                Component.text("Игрок слишком далеко. Подойдите ближе."),
                PlayerUtils.MessageType.CHAT
            )
            return
        }

        // Отключаем коллизии для обоих игроков
        collisionManager.addPlayerToNoCollision(initiator)
        collisionManager.addPlayerToNoCollision(targetPlayer)

        // Определяем, кто из них будет двигаться
        val initiatorMoves = Random().nextBoolean() // true - движется инициатор, false - целевой игрок

        if (initiatorMoves) {
            performMovement(initiator, targetPlayer, plugin, fissureHandler)
        } else {
            performMovement(targetPlayer, initiator, plugin, fissureHandler)
        }

        // Включаем коллизии обратно через 15 секунд
        Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable {
                collisionManager.removePlayerFromNoCollision(initiator)
                collisionManager.removePlayerFromNoCollision(targetPlayer)
            },
            SEX_DURATION * 20L // 15 секунд
        )
    }

    private fun performMovement(
        movingPlayer: Player,
        stationaryPlayer: Player,
        plugin: Plugin,
        fissureHandler: AnalFissureHandler
    ) {
        teleportBehind(stationaryPlayer, movingPlayer)
        movePlayer(movingPlayer, stationaryPlayer, plugin, fissureHandler)

        PlayerUtils.sendMessageToPlayer(
            movingPlayer,
            Component.text("<${stationaryPlayer.name}> даа мой папочка, накажи меня!!~"),
            PlayerUtils.MessageType.CHAT
        )
        PlayerUtils.sendMessageToPlayer(
            stationaryPlayer,
            Component.text("<${movingPlayer.name}> ода, давай сучка, нагибайся)"),
            PlayerUtils.MessageType.CHAT
        )
    }


    // Метод для движения игрока вперед-назад на ограниченное время (в секундах)
    private fun movePlayer(
        player: Player,
        stationaryPlayer: Player,
        plugin: Plugin,
        fissureHandler: AnalFissureHandler
    ) {
        player.sendMessage("Вы будете двигаться вперед-назад!")

        // Устанавливаем движение вперед-назад
        val taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
            plugin,
            object : Runnable {
                private var moveForward = true

                override fun run() {
                    if (!player.isOnline) {
                        return
                    }

                    // Получаем вектор скорости игрока (текущий вектор движения)
                    val currentLocation = player.location
                    var direction = currentLocation.direction.normalize() // Получаем направление взгляда и нормализуем

                    // Обнуляем вертикальную составляющую
                    direction.y = 0.0 // Убираем вертикальную составляющую
                    direction = direction.normalize() // Нормализуем снова после обнуления Y


                    if (moveForward) {
                        player.velocity = direction.multiply(0.25) // Двигаем вперед
                    } else {
                        player.velocity = direction.multiply(-0.25) // Двигаем назад
                    }

                    moveForward = !moveForward // Меняем направление
                }
            },
            0L, // Начало сразу
            2L
        ).taskId

        // Останавливаем задачу через 15 секунд (300 тиков)
        Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable {
                Bukkit.getScheduler().cancelTask(taskId)
                fissureHandler.checkForFissure(stationaryPlayer)
                FallingBlocksTask(plugin, ArrayList(listOf(player, stationaryPlayer))).runTaskTimer(
                    plugin,
                    0L,
                    5L
                )
            },
            SEX_DURATION * 20L // 15 секунд = 300 тиков
        )
    }

    // Метод для телепортации игрока "движущегося" за спину "стоячего"
    private fun teleportBehind(stationaryPlayer: Player, movingPlayer: Player) {
        val stationaryLocation = stationaryPlayer.location
        val backwardDirection = stationaryLocation.direction.multiply(-1.0) // Вектор, указывающий назад

        // Рассчитываем новую позицию для телепортации игрока на 1.5 блока позади
        val teleportLocation = stationaryLocation.clone().add(backwardDirection.normalize().multiply(1.1))
        teleportLocation.y = stationaryLocation.y // Оставляем высоту на том же уровне

        // Телепортируем игрока
        movingPlayer.teleport(teleportLocation)
    }

    private class CollisionManager {

        private var noCollisionTeam: Team?

        init {
            noCollisionTeam = setupNoCollisionTeam()
        }

        // Создаём команду с отключёнными коллизиями
        private fun setupNoCollisionTeam(): Team? {
            val manager = Bukkit.getScoreboardManager()
            val board = manager.mainScoreboard

            // Проверяем, существует ли команда уже
            var team = board.getTeam("noCollision")
            if (team == null) {
                // Создаём новую команду с именем "noCollision"
                team = board.registerNewTeam("noCollision")
                team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER) // Отключаем коллизии
            }
            return team
        }

        // Добавляем игрока в команду без коллизий
        fun addPlayerToNoCollision(player: Player) {
            if (noCollisionTeam != null && !noCollisionTeam!!.hasEntry(player.name)) {
                noCollisionTeam!!.addEntry(player.name)
            }
        }

        // Удаляем игрока из команды (включение коллизий)
        fun removePlayerFromNoCollision(player: Player) {
            if (noCollisionTeam != null && noCollisionTeam!!.hasEntry(player.name)) {
                noCollisionTeam!!.removeEntry(player.name)
            }
        }
    }
}