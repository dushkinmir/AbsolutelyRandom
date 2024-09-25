package ru.dushkinmir.absolutlyRandom.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
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
import java.util.Objects;
import java.util.Random;

public class VovaEvent implements Listener {
    private static final Random RANDOM = new Random();
    private static final Component ACTION_BAR_TEXT = Component.text("фуу ты вонючка!", NamedTextColor.WHITE);
    private static final Component STINKY_PLAYER_MESSAGE = Component.text("бля чел иди искупайся, а не то от тебя весь сервер щарахаться будет");
    private static final Component NORMAL_PLAYER_MESSAGE = Component.text("воо, молодец!");

    public static void triggerVovaEvent(Plugin plugin) {
        List<Player> players = getOnlinePlayers();
        if (!players.isEmpty()) {
            Player randomPlayer = getRandomPlayer(players);
            sendPlayerActionBarMessage(randomPlayer);
            sendPlayerWorldMessage(randomPlayer);
            scheduleEffects(plugin, randomPlayer);
        }
    }

    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static Player getRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void sendPlayerActionBarMessage(Player player) {
        player.sendActionBar(ACTION_BAR_TEXT);
    }

    private static void sendPlayerWorldMessage(Player player) {
        player.getWorld().sendMessage(Component.text("a %s теперь воняет".formatted(Objects.requireNonNull(player.getPlayer()).getName())));
    }

    private static void scheduleEffects(Plugin plugin, Player player) {
        new PlayerEffectTask(player).runTaskTimer(plugin, 0, 20L);
        player.sendMessage(STINKY_PLAYER_MESSAGE);
    }

    private static class PlayerEffectTask extends BukkitRunnable {
        private final Player player;

        public PlayerEffectTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            if (isPlayerInWater(player)) {
                player.sendMessage(NORMAL_PLAYER_MESSAGE);
                this.cancel();
                return;
            }
            applyPoisonEffect(player);
            applySmokeEffect(player);
        }

        private boolean isPlayerInWater(Player player) {
            return player.getLocation().getBlock().getType() == Material.WATER;
        }
    }

    private static void applyPoisonEffect(Player player) {
        for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0, true, true));
            }
        }
    }

    private static void applySmokeEffect(Player player) {
        float particleSize = RANDOM.nextFloat(1.5f, 2.0f);
        player.getWorld().spawnParticle(
                Particle.DUST,
                player.getLocation(),
                75, 1, 1, 1,
                new Particle.DustOptions(Color.GREEN, particleSize));
    }
}