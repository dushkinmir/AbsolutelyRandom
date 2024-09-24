package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class VovaEvent implements Listener {
    private static final Random RANDOM = new Random();
    private static final Particle.DustOptions POISON_SMOKE_OPTIONS = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1.0f);
    private static final int MAX_TICKS = 100;

    /**
     * Triggers the Vova event by selecting a random player and creating a poison smoke effect.
     */
    public static void triggerVovaEvent(Plugin plugin) {
        Player targetPlayer = chooseRandomPlayer();
        if (targetPlayer == null) return;
        createPoisonSmokeEffect(plugin, targetPlayer);
    }

    /**
     * Chooses a random player from the online players.
     *
     * @return a randomly selected Player or null if no players are online.
     */
    private static Player chooseRandomPlayer() {
        List<Player> players = List.copyOf(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return null;
        return players.get(RANDOM.nextInt(players.size()));
    }

    /**
     * Creates a poison smoke effect around the given player.
     *
     * @param player The player around whom the poison smoke effect will be created.
     */
    private static void createPoisonSmokeEffect(Plugin plugin, Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks > MAX_TICKS) {
                    this.cancel();
                    return;
                }
                world.spawnParticle(Particle.DUST, location, 50, 1, 1, 1, POISON_SMOKE_OPTIONS);
                for (Entity entity : world.getNearbyEntities(location, 1, 1, 1)) {
                    if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}