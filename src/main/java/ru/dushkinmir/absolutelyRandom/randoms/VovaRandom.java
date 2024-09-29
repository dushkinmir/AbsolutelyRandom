package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

import java.util.*;

public class VovaRandom implements Listener {
    private static final Random RANDOM = new Random();
    private static final Component ACTION_BAR_TEXT = Component.text("фуу ты вонючка!", NamedTextColor.WHITE);
    private static final Component STINKY_PLAYER_MESSAGE = Component.text(
            "бля чел иди искупайся, а то от тебя весь сервер щарахается"
    );
    private static final Component NORMAL_PLAYER_MESSAGE = Component.text("воо, молодец!");

    private final Plugin plugin;

    public VovaRandom(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void triggerVova(Plugin plugin) {
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
        player.getWorld().sendMessage(Component.text(
                "a %s теперь воняет".formatted(Objects.requireNonNull(player.getPlayer()).getName()))
        );
    }

    private static void scheduleEffects(Plugin plugin, Player player) {
        UUID playerUUID = player.getUniqueId();
        Map<UUID, BukkitRunnable> playerTasks = AbsolutelyRandom.getPlayerTasks();
        PlayerEffectTask task = new PlayerEffectTask(player);
        task.runTaskTimer(plugin, 0, 20L);
        if (!playerTasks.containsKey(playerUUID)) AbsolutelyRandom.getPlayerTasks().put(playerUUID, task);
        player.sendMessage(STINKY_PLAYER_MESSAGE);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Map<UUID, BukkitRunnable> playerTasks = AbsolutelyRandom.getPlayerTasks();
        if (playerTasks.containsKey(playerUUID)) {
            scheduleEffects(plugin, player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Map<UUID, BukkitRunnable> playerTasks = AbsolutelyRandom.getPlayerTasks();
        if (playerTasks.containsKey(playerUUID)) {
            playerTasks.get(playerUUID).cancel();
        }
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
                player.sendMessage(NORMAL_PLAYER_MESSAGE);
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
        float particleSize = random.nextFloat(1.5f, 2.0f);
        player.getWorld().spawnParticle(
                Particle.DUST,
                player.getLocation(),
                75, 1, 1, 1,
                new Particle.DustOptions(Color.GREEN, particleSize));
    }
}