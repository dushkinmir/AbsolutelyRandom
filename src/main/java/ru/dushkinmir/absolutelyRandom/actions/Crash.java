package ru.dushkinmir.absolutelyRandom.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

public class Crash {
    private static volatile boolean isMaintenanceMode = false; // Добавлена volatile для синхронизации
    private static final Component MAINTENANCE_KICK_MSG =
            Component.text("The server is currently offline for maintenance. Please try again later.", NamedTextColor.RED);
    private static final int RESTART_DELAY_TICKS = 400; // 20 секунд
    private static final Component MAINTENANCE_MOTD = Component.text("This server is offline for maintenance.");
    private static final Component RESTART_KICK_MSG =
            Component.text("Please wait a moment, the server is restarting...", NamedTextColor.YELLOW);

    private static Listener joinListener;

    public static void triggerCrash(Plugin plugin) {
        PlayerUtils.kickAllPlayers(MAINTENANCE_KICK_MSG);
        Component oldMotd = Bukkit.getServer().motd();
        enableMaintenanceMode();
        registerJoinListener(plugin);
        scheduleServerRestart(plugin, oldMotd);
    }

    private static void enableMaintenanceMode() {
        isMaintenanceMode = true;
        Bukkit.getServer().motd(MAINTENANCE_MOTD);
    }

    private static void registerJoinListener(Plugin plugin) {
        if (joinListener != null) {
            HandlerList.unregisterAll(joinListener);
        }

        joinListener = new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                kickPlayerOnJoin(event);
            }
        };

        Bukkit.getPluginManager().registerEvents(joinListener, plugin);
    }

    private static void kickPlayerOnJoin(PlayerJoinEvent event) {
        if (isMaintenanceMode) {
            Player player = event.getPlayer();
            PlayerUtils.kickPlayer(player, RESTART_KICK_MSG);
        }
    }

    private static void scheduleServerRestart(Plugin plugin, Component oldMotd) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            isMaintenanceMode = false;
            restoreMotd(oldMotd);
        }, RESTART_DELAY_TICKS);
    }

    private static void restoreMotd(Component oldMotd) {
        Bukkit.getServer().motd(oldMotd);
    }
}