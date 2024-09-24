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

    public static void triggerVovaEvent(Plugin plugin) {
        Player targetPlayer = chooseRandomPlayer();
        if (targetPlayer == null) return;
        createEffect(plugin, targetPlayer, VovaEvent::applyPoisonEffect);
        createEffect(plugin, targetPlayer, VovaEvent::applySmokeEffect);
    }

    private static Player chooseRandomPlayer() {
        List<Player> players = List.copyOf(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return null;
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void createEffect(Plugin plugin, Player player, EffectApplier effectApplier) {
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
                effectApplier.apply(world, location, player);
                ticks++;
            }
        }.runTaskTimer(plugin, 0, 200);
    }

    private static void applyPoisonEffect(World world, Location location, Player player) {
        for (Entity entity : world.getNearbyEntities(location, 1, 1, 1)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0, true, true));
            }
        }
    }

    private static void applySmokeEffect(World world, Location location, Player player) {
        world.spawnParticle(Particle.DUST, location, 50, 1, 1, 1, POISON_SMOKE_OPTIONS);
    }

    @FunctionalInterface
    private interface EffectApplier {
        void apply(World world, Location location, Player player);
    }
}