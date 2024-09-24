package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VovaEvent {

    private static final Random RANDOM = new Random();
    private static final int CLOUD_DURATION_TICKS = 200; // 10 seconds in ticks
    private static final int POISON_DURATION_TICKS = 40; // 2 seconds in ticks
    private static final int POISON_RADIUS = 1;
    private static final int TICK_INTERVAL = 20; // 1 second in ticks

    public static void triggerVovaEvent(Plugin plugin) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return;

        Player selectedPlayer = getRandomPlayer(onlinePlayers);
        spawnPoisonCloud(plugin, selectedPlayer);
    }

    private static Player getRandomPlayer(List<Player> players) {
        int randomIndex = RANDOM.nextInt(players.size());
        return players.get(randomIndex);
    }

    private static void spawnPoisonCloud(Plugin plugin, Player player) {
        new BukkitRunnable() {
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= CLOUD_DURATION_TICKS) {
                    this.cancel();
                    return;
                }

                createPoisonCloud(player);
                applyPoisonToNearbyEntities(player);

                ticks += TICK_INTERVAL;
            }
        }.runTaskTimer(plugin, 0L, TICK_INTERVAL);
    }

    private static void createPoisonCloud(Player player) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.GREEN, 1.0F);
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 10, 0.5, 0.5, 0.5, dustOptions);
    }

    private static void applyPoisonToNearbyEntities(Player player) {
        List<Entity> nearbyEntities = player.getNearbyEntities(POISON_RADIUS, POISON_RADIUS, POISON_RADIUS);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && !entity.equals(player)) {
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(
                        PotionEffectType.POISON, POISON_DURATION_TICKS, 0, true, false, true));
            }
        }
    }
}