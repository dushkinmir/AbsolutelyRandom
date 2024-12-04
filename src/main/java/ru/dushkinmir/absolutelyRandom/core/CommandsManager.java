package ru.dushkinmir.absolutelyRandom.core;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.features.sex.SexCommands;
import ru.dushkinmir.absolutelyRandom.features.warp.WarpCommands;

public class CommandsManager {
    private final Plugin plugin;
    private final ExtensionManager extensionManager;

    public CommandsManager(Plugin plugin, ExtensionManager extensionManager) {
        this.plugin = plugin;
        this.extensionManager = extensionManager;
    }

    public void registerCommands() {
        // Initialize and register WarpCommandManager
        WarpCommands wcm = new WarpCommands(extensionManager.getWarpManager(), plugin);
        // Initialize and register SexCommandManager
        SexCommands scm = new SexCommands(extensionManager.getFissureHandler(), plugin);
        wcm.registerWarpCommands(); // Register warp commands
        scm.registerSexCommand(); // Register sex commands
        CommandAPI.onEnable(); // Enable CommandAPI
    }

    public void unregisterCommands() {
        // Unregister commands
        CommandAPI.unregister("debugrandom");
        CommandAPI.unregister("warp");
        CommandAPI.unregister("sex");
        plugin.getLogger().info("Команды CommandAPI отменены.");
        // Disable CommandAPI
        CommandAPI.onDisable();
    }
}
