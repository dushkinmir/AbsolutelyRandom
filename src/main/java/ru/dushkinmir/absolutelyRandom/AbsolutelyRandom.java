package ru.dushkinmir.absolutelyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.core.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbsolutelyRandom extends JavaPlugin implements Listener {
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>(); // Map to store player tasks
    // Core modules
    private final ActionsManager actionsManager = new ActionsManager(this);
    private final WebSocketManager webSocketHandler = new WebSocketManager(this);
    private final ExtensionManager extensionManager = new ExtensionManager(this);
    private final EventsManager eventsManager = new EventsManager(this, extensionManager);
    private final CommandsManager commandsManager = new CommandsManager(this, extensionManager);

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true).usePluginNamespace());
    }

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig(); // Save default config if not exist
            extensionManager.initManagers();
            eventsManager.registerEvents();
            actionsManager.registerAllActions();
            commandsManager.registerCommands();
            webSocketHandler.enableWebSocketServer();

            getLogger().info("Hi there!!");
        } catch (Exception e) {
            getLogger().severe("Ошибка при включении плагина: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Cancel and clear player tasks
        PLAYER_TASKS.values().forEach(BukkitRunnable::cancel);
        PLAYER_TASKS.clear();

        webSocketHandler.disableWebSocketServer();
        commandsManager.unregisterCommands();
        eventsManager.onDisable();
        extensionManager.shutdownManagers();
    }
}
