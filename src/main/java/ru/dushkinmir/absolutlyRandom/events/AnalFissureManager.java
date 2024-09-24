package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.HashMap;
import java.util.Random;

public class AnalFissureManager implements Listener {
    private final HashMap<Player, Integer> analFissureMap = new HashMap<>(); // Хранит игрока и количество оставшихся дней
    private final Random random = new Random();

    // Проверка на получение анальной трещины
    public void checkForAnalFissure(Player player) {
        if (random.nextInt(100) < 10) { // 10% шанс
            analFissureMap.put(player, 2); // Устанавливаем трещину с 2 днями до заживления
            player.sendMessage("Вы получили анальную трещину! Она заживет через два игровых дня.");
        } else {
            player.sendMessage("В этот раз все прошло гладко.");
        }
    }

    // Метод для обновления состояния анальной трещины
    private void updateAnalFissure(Player player) {
        if (analFissureMap.containsKey(player)) {
            int daysRemaining = analFissureMap.get(player);
            if (daysRemaining > 0) {
                analFissureMap.put(player, daysRemaining - 1); // Уменьшаем количество оставшихся дней
                if (daysRemaining - 1 == 0) {
                    analFissureMap.remove(player); // Удаляем анальную трещину
                    player.sendMessage("Ваша анальная трещина зажила. Вы можете снова сидеть и спать.");
                }
            }
        }
    }

    @EventHandler
    private void onTimeSkip(TimeSkipEvent event) {
        // Проверяем смену дня
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateAnalFissure(player); // Обновляем состояние анальной трещины у всех игроков
            }
        }
    }

    @EventHandler
    private void onWorldDayChange() {
        // Убедимся, что при смене дня обновляем анальные трещины
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateAnalFissure(player); // Обновляем состояние анальной трещины у всех игроков
        }
    }

    @EventHandler
    private void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (analFissureMap.containsKey(player)) {
            event.setCancelled(true); // Отменяем событие, если у игрока есть трещина
            player.sendMessage("Вы не можете сесть из-за анальной трещины!");
        }
    }

    @EventHandler
    private void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (analFissureMap.containsKey(player)) {
            event.setCancelled(true); // Отменяем попытку лечь
            player.sendMessage("Вы не можете лечь из-за анальной трещины!");
        }
    }

    @EventHandler
    private void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        if (analFissureMap.containsKey(player)) {
            event.setCancelled(true); // Отменяем попытку встать с кровати
            player.sendMessage("Вы не можете встать из-за анальной трещины!");
        }
    }
}
