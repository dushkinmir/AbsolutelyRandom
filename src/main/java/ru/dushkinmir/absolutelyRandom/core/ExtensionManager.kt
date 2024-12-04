package ru.dushkinmir.absolutelyRandom.core;

import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.features.sex.AnalFissureHandler;
import ru.dushkinmir.absolutelyRandom.features.warp.WarpManager;
import ru.dushkinmir.absolutelyRandom.utils.DatabaseManager;

import java.sql.SQLException;

public class ExtensionManager {
    private final Plugin plugin;

    private DatabaseManager database; // Database manager
    private AnalFissureHandler fissureHandler; // Handler for anal fissure events
    private WarpManager warpManager; // Warp manager

    public ExtensionManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void initManagers() throws SQLException {
        // Open database
        database = new DatabaseManager(plugin);
        plugin.getLogger().info("База данных открыта.");
        // Initialize managers
        warpManager = new WarpManager(database, plugin);
        fissureHandler = new AnalFissureHandler(database, plugin);
        plugin.getLogger().info("Менеджеры инициализированы.");
    }

    public AnalFissureHandler getFissureHandler() {
        return fissureHandler;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public void shutdownManagers() {
        if (database != null) {
            database.close();
            plugin.getLogger().info("База данных закрыта.");
        }
    }
}
