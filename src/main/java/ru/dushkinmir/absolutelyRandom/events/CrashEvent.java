package ru.dushkinmir.absolutelyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.Plugin;

public class CrashEvent {
    private static boolean isOffline = false;
    private static final String MAINTENANCE_KICK_MSG =
            "The server is currently offline for maintenance. Please try again later.";
    private static final int RESTART_DELAY_TICKS = 400; // 20 секунд
    private static final Component MAINTENANCE_MOTD = Component.text("This server is offline for maintenance.");
    private static final Component RESTART_KICK_MSG = Component.text(
            "Please wait a moment, the server is restarting...", NamedTextColor.YELLOW
    );

    public static void triggerCrashEvent(Plugin plugin) {
        kickAllPlayers();
        enableMaintenanceMode();
        registerJoinListener(plugin);
        scheduleServerRestart(plugin);
    }

    private static void kickAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(Component.text(MAINTENANCE_KICK_MSG, NamedTextColor.RED));
        }
    }

    private static void enableMaintenanceMode() {
        isOffline = true;
        Bukkit.getServer().motd(MAINTENANCE_MOTD);
    }

    private static void registerJoinListener(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                if (isOffline) {
                    Player player = event.getPlayer();
                    player.kick(RESTART_KICK_MSG);
                }
            }
        }, plugin);
    }

    private static void scheduleServerRestart(Plugin plugin) {
        Component oldMotd = Bukkit.getServer().motd();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            isOffline = false;
            Bukkit.getServer().motd(oldMotd);
        }, RESTART_DELAY_TICKS);
    }
}