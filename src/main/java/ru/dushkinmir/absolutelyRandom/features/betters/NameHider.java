package ru.dushkinmir.absolutelyRandom.features.betters;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NameHider implements Listener {
    private final Map<UUID, Map<UUID, Boolean>> visibilityMap = new ConcurrentHashMap<>();
    private final Plugin plugin;

    public NameHider(Plugin plugin) {
        this.plugin = plugin;
        startVisibilityTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        visibilityMap.putIfAbsent(playerId, new ConcurrentHashMap<>());
    }

    private void startVisibilityTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    updateVisibility();
                } catch (Exception e) {
                    plugin.getLogger().severe(e.getMessage());
                }
            }
        }.runTaskTimer(plugin, 0L, 3L); // Обновление каждые 20 тиков (1 секунда)
    }

    void updateVisibility() {
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            UUID viewerId = viewer.getUniqueId();
            visibilityMap.putIfAbsent(viewerId, new ConcurrentHashMap<>());
            Map<UUID, Boolean> viewerVisibilityMap = visibilityMap.get(viewerId);

            for (Player target : plugin.getServer().getOnlinePlayers()) {
                if (viewer.equals(target)) continue;

                UUID targetId = target.getUniqueId();
                boolean canSee = viewer.hasLineOfSight(target);

                if (canSee && (!viewerVisibilityMap.containsKey(targetId) || !viewerVisibilityMap.get(targetId))) {
                    viewer.showPlayer(plugin, target);
                    viewerVisibilityMap.put(targetId, true);
                } else if (!canSee && (!viewerVisibilityMap.containsKey(targetId) || viewerVisibilityMap.get(targetId))) {
                    viewer.hidePlayer(plugin, target);
                    viewerVisibilityMap.put(targetId, false);
                }
            }
        }
    }
}
