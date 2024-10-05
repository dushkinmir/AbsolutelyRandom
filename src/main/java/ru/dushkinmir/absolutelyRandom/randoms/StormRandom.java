package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.List;
import java.util.Random;

public class StormRandom {
    private static final Random RANDOM = new Random();
    private static final Component STORM_MESSAGE = Component.text("Гроза началась! Убегай!", NamedTextColor.YELLOW);
    private static final int STORM_DURATION_SECONDS = 60;
    private static final int STRIKE_INTERVAL_SECONDS = 15;
    private static final double MAX_OFFSET_DISTANCE = 3.0;

    public static void triggerStorm(Plugin plugin) {
        List<Player> onlinePlayers = PlayerUtils.getOnlinePlayers();
        if (!onlinePlayers.isEmpty()) {
            startStorm(plugin, onlinePlayers);
        }
    }

    private static void startStorm(Plugin plugin, List<Player> players) {
        players.forEach(player -> player.getWorld().setStorm(true));
        sendStormWarningToPlayers(players);
        new StormTask(players).runTaskTimer(plugin, 60, 20 * STRIKE_INTERVAL_SECONDS);
        players.forEach(player -> player.getWorld().setStorm(false));

    }

    private static void sendStormWarningToPlayers(List<Player> players) {
        for (Player player : players) {
            PlayerUtils.sendMessageToPlayer(player, STORM_MESSAGE, PlayerUtils.MessageType.ACTION_BAR);
        }
    }

    private static class StormTask extends BukkitRunnable {
        private final List<Player> players;
        private int elapsedSeconds = 0;

        public StormTask(List<Player> players) {
            this.players = players;
        }

        @Override
        public void run() {
            if (elapsedSeconds >= STORM_DURATION_SECONDS) {
                cancel();
                return;
            }
            StormRandom.strikeRandomPlayer(players);
            elapsedSeconds += STRIKE_INTERVAL_SECONDS;
        }
    }

    private static void strikeRandomPlayer(List<Player> players) {
        Player randomPlayer = PlayerUtils.getRandomPlayer(players);
        Location strikeLocation = calculateStrikeLocation(randomPlayer.getLocation());
        createWeatherEffect(strikeLocation);
    }

    private static Location calculateStrikeLocation(Location location) {
        return location.clone().add(getRandomOffset(), 0, getRandomOffset());
    }

    private static double getRandomOffset() {
        return RANDOM.nextDouble() * 2 * MAX_OFFSET_DISTANCE - MAX_OFFSET_DISTANCE;
    }

    private static void createWeatherEffect(Location strikeLocation) {
        strikeLocation.getWorld().strikeLightning(strikeLocation);
        strikeLocation.getWorld().createExplosion(strikeLocation, 4.0f, false, false);
        strikeLocation.getWorld().spawnParticle(Particle.EXPLOSION, strikeLocation, 10);
        strikeLocation.getWorld().playSound(strikeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
    }
}