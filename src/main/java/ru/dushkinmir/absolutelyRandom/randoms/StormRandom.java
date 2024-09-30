package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StormRandom {

    private static final Random RANDOM = new Random();
    private static final String STORM_MESSAGE = "Гроза началась! Убегай!";
    private static final NamedTextColor STORM_MESSAGE_COLOR = NamedTextColor.YELLOW;
    private static final int STORM_DURATION = 60; // Длительность шторма в секундах
    private static final int STRIKE_INTERVAL = 15; // Интервал между ударами в секундах

    public static void triggerStorm(Plugin plugin) {
        List<Player> onlinePlayers = getOnlinePlayers();
        if (!onlinePlayers.isEmpty()) {
            startStorm(plugin, onlinePlayers);
        }
    }

    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static void startStorm(Plugin plugin, List<Player> players) {
        for (Player player : players) {
            sendStormMessage(player);
        }

        new BukkitRunnable() {
            int ticks = 0; // Счетчик тиков (каждый тик - 1/20 секунды)

            @Override
            public void run() {
                if (ticks >= STORM_DURATION * 20) {
                    cancel(); // Останавливаем задачу через минуту
                    return;
                }

                strikeLightningRandomPlayer(players);
                ticks += STRIKE_INTERVAL; // Увеличиваем счетчик на интервал
            }
        }.runTaskTimer(plugin, 0, 20 * STRIKE_INTERVAL); // Запускать через 15 секунд
    }

    private static void strikeLightningRandomPlayer(List<Player> players) {
        Player randomPlayer = pickRandomPlayer(players);
        strikeLightning(randomPlayer.getLocation());
    }

    private static Player pickRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void sendStormMessage(Player player) {
        player.sendMessage(Component.text(STORM_MESSAGE, STORM_MESSAGE_COLOR));
    }

    private static void strikeLightning(Location location) {
        // Генерация случайных координат в радиусе 2-3 блока от игрока
        double offsetX = RANDOM.nextDouble() * 6 - 3; // Случайное значение от -3 до 3
        double offsetZ = RANDOM.nextDouble() * 6 - 3; // Случайное значение от -3 до 3

        Location strikeLocation = location.clone().add(offsetX, 0, offsetZ);
        strikeLocation.getWorld().strikeLightning(strikeLocation);
        strikeLocation.getWorld().spawnParticle(Particle.EXPLOSION, strikeLocation, 10);
        strikeLocation.getWorld().playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
}
