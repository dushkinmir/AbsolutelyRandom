package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.AbsolutelyRandom;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.*;

public class VovaRandom implements Listener {
    private static final Component ACTION_BAR_TEXT = Component.text("фуу ты вонючка!");
    private static final Component STINKY_PLAYER_MESSAGE = Component.text(
            "бля чел иди искупайся, а то от тебя весь сервер щарахается"
    );
    private static final Component NORMAL_PLAYER_MESSAGE = Component.text("воо, молодец!");

    private final Plugin plugin;

    public VovaRandom(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void triggerVova(Plugin plugin) {
        List<Player> players = PlayerUtils.getOnlinePlayers();
        if (!players.isEmpty()) {
            Player randomPlayer = PlayerUtils.getRandomPlayer(players);
            PlayerUtils.sendMessageToPlayer(randomPlayer, ACTION_BAR_TEXT, true);
            sendPlayerWorldMessage(randomPlayer);
            UUID playerUUID = randomPlayer.getUniqueId();
            scheduleEffects(plugin, playerUUID);
        }
    }

    private static void sendPlayerWorldMessage(Player player) {
        String message = "a %s теперь воняет".formatted(Objects.requireNonNull(player.getPlayer()).getName());
        player.getWorld().sendMessage(Component.text(message));
    }

    private static void scheduleEffects(Plugin plugin, UUID playerUUID) {
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player != null) {
            Map<UUID, BukkitRunnable> playerTasks = AbsolutelyRandom.getPlayerTasks();
            PlayerEffectTask task = new PlayerEffectTask(player);
            task.runTaskTimer(plugin, 0, 20L);
            if (!playerTasks.containsKey(playerUUID)) playerTasks.put(playerUUID, task);
            PlayerUtils.sendMessageToPlayer(player, STINKY_PLAYER_MESSAGE, false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (isPlayerTracked(playerUUID)) {
            scheduleEffects(plugin, playerUUID);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (isPlayerTracked(playerUUID)) {
            AbsolutelyRandom.getPlayerTasks().get(playerUUID).cancel();
        }
    }

    private boolean isPlayerTracked(UUID playerUUID) {
        return AbsolutelyRandom.getPlayerTasks().containsKey(playerUUID);
    }

    private static class PlayerEffectTask extends BukkitRunnable {
        private final Player player;

        public PlayerEffectTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            Map<UUID, BukkitRunnable> playerTasks = AbsolutelyRandom.getPlayerTasks();
            UUID playerUUID = player.getUniqueId();
            if (isPlayerInWater(player)) {
                PlayerUtils.sendMessageToPlayer(player, NORMAL_PLAYER_MESSAGE, false);
                this.cancel();
                playerTasks.remove(playerUUID);
                return;
            }
            if (player.isOnline()) {
                applyPoisonEffect(player);
                applySmokeEffect(player);
            }
        }

        private boolean isPlayerInWater(Player player) {
            return player.getLocation().getBlock().getType() == Material.WATER;
        }
    }

    private static void applyPoisonEffect(Player player) {
        for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 1, true, true));
            }
        }
    }

    private static void applySmokeEffect(Player player) {
        Random random = new Random();
        float particleSize = random.nextFloat(0.5f, 2.0f);
        player.getWorld().spawnParticle(
                Particle.DUST,
                player.getLocation(),
                75, 1, 1, 1,
                new Particle.DustOptions(Color.GREEN, particleSize));
    }
}