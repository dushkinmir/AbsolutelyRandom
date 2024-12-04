package ru.dushkinmir.absolutelyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.core.ExtensionManager;
import ru.dushkinmir.absolutelyRandom.core.WebSocketHandler;
import ru.dushkinmir.absolutelyRandom.features.actions.ActionsManager;
import ru.dushkinmir.absolutelyRandom.features.actions.types.Stinky;
import ru.dushkinmir.absolutelyRandom.features.betters.HeadChat;
import ru.dushkinmir.absolutelyRandom.features.betters.NameHider;
import ru.dushkinmir.absolutelyRandom.features.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.features.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.features.sex.SexCommandManager;
import ru.dushkinmir.absolutelyRandom.features.warp.WarpCommandManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbsolutelyRandom extends JavaPlugin implements Listener {
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>(); // Map to store player tasks
    public static final Set<String> MESSAGES_SET = new HashSet<>(); // Set to store messages
    private static final long RELOAD_INTERVAL = 20 * 60 * 5; // Interval for automatic reload (every 5 minutes)
    private final Map<String, Boolean> enabledBetters = new ConcurrentHashMap<>(); // Map to enable/disable betters
    // Core modules
    private final ActionsManager actionsManager = new ActionsManager(this);
    private final WebSocketHandler webSocketHandler = new WebSocketHandler(this);
    private final ExtensionManager extensionManager = new ExtensionManager(this);

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true).usePluginNamespace());
    }

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig(); // Save default config if not exist
            loadConfigValues(); // Load configuration values
            getLogger().info("Конфигурация загружена.");

            extensionManager.initManagers();

            // Register events
            registerEvents();
            getLogger().info("События зарегистрированы.");

            actionsManager.registerAllActions(); // Регистрируем все экшены
            actionsManager.registerCommands();
            registerCommands(); // Register commands
            getLogger().info("Команды зарегистрированы.");

            webSocketHandler.enableWebSocketServer();

            startAutoReloadTask(); // Start task for automatic reload
//            scheduleActionTrigger(); // Schedule action triggers
            CommandAPI.onEnable(); // Enable CommandAPI

            logPluginActivation();
        } catch (Exception e) {
            getLogger().severe("Ошибка при включении плагина: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this); // Disable plugin on error
        }
    }

    @Override
    public void onDisable() {
        // Cancel and clear player tasks
        PLAYER_TASKS.values().forEach(BukkitRunnable::cancel);
        PLAYER_TASKS.clear();

        // Stop WebSocket server
        webSocketHandler.disableWebSocketServer();

        // Unregister commands
        CommandAPI.unregister("debugrandom");
        CommandAPI.unregister("warp");
        CommandAPI.unregister("sex");
        getLogger().info("Команды CommandAPI отменены.");
        // Disable CommandAPI
        CommandAPI.onDisable();

        if (enabledBetters.get("head-chat")) HeadChat.onDisable(this);

        extensionManager.shutdownManagers();

        logPluginDeactivation();
    }

    private void logPluginActivation() {
        getLogger().info("Плагин AbsolutelyRandom активирован!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
    }

    private void logPluginDeactivation() {
        getLogger().info("Плагин AbsolutelyRandom деактивирован!");
    }

    private void loadConfigValues() {
        getLogger().info("Загрузка значений конфигурации...");
        // Load enabled status for betters from config
        enabledBetters.put("name-hider", getConfig().getBoolean("betters.name-hider", false));
        enabledBetters.put("head-chat", getConfig().getBoolean("betters.head-chat", false));
    }

    private void reloadMessagesAsync() {
        // Reload messages from config asynchronously
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            List<String> configMessages = getConfig().getStringList("random-messages");
            if (!configMessages.isEmpty() && !configMessages.equals(new ArrayList<>(MESSAGES_SET))) {
                synchronized (MESSAGES_SET) {
                    MESSAGES_SET.clear();
                    MESSAGES_SET.addAll(configMessages);
                }
                getLogger().info("Сообщения конфигурации успешно перезагружены.");
            }
        });
    }

    private void startAutoReloadTask() {
        // Start a task to automatically reload messages at intervals
        new BukkitRunnable() {
            @Override
            public void run() {
                reloadMessagesAsync(); // Автоматическая перезагрузка
            }
        }.runTaskTimer(this, 0L, RELOAD_INTERVAL);
        getLogger().info("Автоматическая перезагрузка сообщений включена.");
    }


    private void registerEvents() {
        // List of events to register
        List<Listener> events = new ArrayList<>(Arrays.asList(
                new DrugsEvent(),
                new Stinky(),
                new ConsentEvent(this),
                extensionManager.getFissureHandler()
        ));

        // Add conditional events based on config
        addConditionalEvent(events, "name-hider", new NameHider(this));
        addConditionalEvent(events, "head-chat", new HeadChat(this));

        // Register events with the server
        for (Listener event : events) {
            getServer().getPluginManager().registerEvents(event, this);
        }
    }

    private void addConditionalEvent(List<Listener> events, String conditionKey, Listener listener) {
        // Add event to list if condition is met
        if (enabledBetters.get(conditionKey).equals(true)) {
            events.add(listener);
            getLogger().info("Событие " + conditionKey + " добавлено согласно условию.");
        }
    }

    private void registerCommands() {
        // Initialize and register WarpCommandManager
        WarpCommandManager wcm = new WarpCommandManager(extensionManager.getWarpManager(), this);
        // Initialize and register SexCommandManager
        SexCommandManager scm = new SexCommandManager(extensionManager.getFissureHandler(), this);
        wcm.registerWarpCommands(); // Register warp commands
        scm.registerSexCommand(); // Register sex commands
    }
}
