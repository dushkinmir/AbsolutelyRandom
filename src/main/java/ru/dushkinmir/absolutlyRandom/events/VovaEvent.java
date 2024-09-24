package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VovaEvent implements Listener {

    private static final Random RANDOM = new Random();
    private static final int MAX_SECONDS = 20;

    public static void triggerVovaEvent(Plugin plugin) {
        List<Player> players = getOnlinePlayers();
        if (!players.isEmpty()) {
            Player randomPlayer = getRandomPlayer(players);
            createEffect(plugin, randomPlayer);
        }
    }
    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static Player getRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void createEffect(Plugin plugin, Player player) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > MAX_SECONDS) {
                    this.cancel();
                    return;
                }
                applyPoisonEffect(player);
                applySmokeEffect(player);
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private static void applyPoisonEffect(Player player) {
        for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 400, 0, true, true));
            }
        }
    }

    private static void applySmokeEffect(Player player) {
        float particleSize = RANDOM.nextFloat(1.5f, 2.0f);
        player.spawnParticle(Particle.DUST, player.getLocation(), 150, 1, 1, 1, new Particle.DustOptions(Color.GREEN, particleSize));
    }

}