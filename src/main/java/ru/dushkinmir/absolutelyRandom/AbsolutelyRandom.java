package ru.dushkinmir.absolutelyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.actions.*;
import ru.dushkinmir.absolutelyRandom.betters.CraftingRecipe;
import ru.dushkinmir.absolutelyRandom.betters.HeadChat;
import ru.dushkinmir.absolutelyRandom.betters.NameHider;
import ru.dushkinmir.absolutelyRandom.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.sex.AnalFissureHandler;
import ru.dushkinmir.absolutelyRandom.sex.SexCommandManager;
import ru.dushkinmir.absolutelyRandom.utils.DatabaseManager;
import ru.dushkinmir.absolutelyRandom.utils.ServerControl;
import ru.dushkinmir.absolutelyRandom.utils.WebSocketMessageListener;
import ru.dushkinmir.absolutelyRandom.utils.WebSocketServer;
import ru.dushkinmir.absolutelyRandom.warp.WarpCommandManager;
import ru.dushkinmir.absolutelyRandom.warp.WarpManager;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbsolutelyRandom extends JavaPlugin implements Listener {
    private static final long SCHEDULE_PERIOD = 20L; // Scheduling period for tasks
    private static final Random RANDOM_GENERATOR = new Random(); // Random number generator
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>(); // Map to store player tasks
    private static final Set<String> MESSAGES_SET = new HashSet<>(); // Set to store messages
    private static final long RELOAD_INTERVAL = 20 * 60 * 5; // Interval for automatic reload (every 5 minutes)
    private final Map<String, Integer> actionsChances = new ConcurrentHashMap<>(); // Chances for actions
    private final Map<String, Boolean> enabledBetters = new ConcurrentHashMap<>(); // Map to enable/disable betters
    private DatabaseManager database; // Database manager
    private AnalFissureHandler fissureHandler; // Handler for anal fissure events
    private WarpManager warpManager; // Warp manager
    private WebSocketServer wsserver; // WebSocket server

    public static void main(String[] args) {
        System.out.println("пидисят два!!!");
    }

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true).usePluginNamespace()); // Load CommandAPI with verbose output
        try {
            openDatabase(); // Open the database
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig(); // Save default config if not exist
            loadConfigValues(); // Load configuration values
            openDatabase(); // Open the database

            // Initialize managers
            warpManager = new WarpManager(database, this);
            fissureHandler = new AnalFissureHandler(database, this);

            registerEvents(); // Register events
            new CraftingRecipe(this); // Initialize crafting recipes
            registerCommands(); // Register commands
            if (getConfig().getBoolean("betters.websocket.enabled", false)) {
                enableWebSocketServer(); // Enable WebSocket server if enabled in config
            }
            startAutoReloadTask(); // Start task for automatic reload
            scheduleActionTrigger(); // Schedule action triggers
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
        // Stop WebSocket server if it exists
        if (wsserver != null) disableWebSocketServer();
        CommandAPI.onDisable(); // Disable CommandAPI
        CommandAPI.unregister("debugrandom");
        CommandAPI.unregister("warp");
        CommandAPI.unregister("sex");
        closeDatabase(); // Close the database
        logPluginDeactivation();
    }

    private void enableWebSocketServer() {
        // Get server IP and port
        String serverIp = this.getServer().getIp().isEmpty() ? "localhost" : this.getServer().getIp();
        int port = this.getServer().getPort() + 1;

        // Initialize WebSocket server
        wsserver = new WebSocketServer(serverIp, port, getLogger());
        try {
            if (getConfig().getBoolean("betters.websocket.server-control", false)) {
                wsserver.addListener(new ServerControl(this)); // Add WebSocket listener if config is true
            }
        } catch (Exception e) {
            this.getLogger().severe("Не удалось активировать слушателей WebSocket. " + e.getMessage());
        }
        wsserver.start(); // Start WebSocket server
    }

    private void disableWebSocketServer() {
        // Stopping the WebSocket server
        if (wsserver != null) {
            try {
                // Remove all listeners
                for (WebSocketMessageListener listener : wsserver.getListeners()) {
                    wsserver.removeListener(listener);
                }
                wsserver.stop(); // Stop the server
            } catch (Exception e) {
                this.getLogger().severe("Не удалось остановить WebSocket сервер: " + e.getMessage());
            }
        }
    }

    private void logPluginActivation() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
    }

    private void logPluginDeactivation() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
    }

    private void loadConfigValues() {
        // Load chances for actions from config
        actionsChances.put("kick", getConfig().getInt("chances.kick-chance", -1));
        actionsChances.put("group", getConfig().getInt("chances.group-chance", -1));
        actionsChances.put("crash", getConfig().getInt("chances.crash-chance", -1));
        actionsChances.put("message", getConfig().getInt("chances.message-chance", -1));
        actionsChances.put("stinky", getConfig().getInt("chances.vova-chance", -1)); // vova -> stinky
        actionsChances.put("storm", getConfig().getInt("chances.storm-chance", -1));
        actionsChances.put("prank", getConfig().getInt("chances.eschkere-chance", -1)); // eschkere -> prank

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
    }

    private void scheduleActionTrigger() {
        // Schedule task to trigger random events at intervals
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomEvents(); // Execute random events
            }
        }.runTaskTimer(this, 0, SCHEDULE_PERIOD);
    }

    private void registerEvents() {
        // List of events to register
        List<Listener> events = new ArrayList<>(Arrays.asList(
                new DrugsEvent(),
                new Stinky(this),
                new ConsentEvent(this),
                fissureHandler
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
        }
    }

    private void openDatabase() throws SQLException {
        database = new DatabaseManager(this); // Создаем экземпляр базы данных
    }

    private void closeDatabase() {
        if (database != null) {
            database.close(); // Закрываем пул соединений при отключении плагина
        }
    }

    private void registerCommands() {
        // Register debugrandom command
        new CommandAPICommand("debugrandom")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <random>")
                .withArguments(new StringArgument("random")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                new ArrayList<>(debugEvents.keySet())))
                )
                .executes((sender, args) -> {
                    String event = (String) args.get("random");
                    assert event != null;
                    handleDebugRandom(sender, event); // Handle debug random event
                })
                .register(this);

        // Initialize and register WarpCommandManager
        WarpCommandManager wcm = new WarpCommandManager(warpManager, this);
        // Initialize and register SexCommandManager
        SexCommandManager scm = new SexCommandManager(fissureHandler, this);
        wcm.registerWarpCommands(); // Register warp commands
        scm.registerSexCommand(); // Register sex commands
    }

    private final Map<String, Runnable> debugEvents = Map.of(
            "kick", () -> triggerRandom(Kick::triggerKick), // Kick event
            "eschkere", () -> triggerRandom(Prank::triggerPrank), // Prank event
            "group", () -> triggerRandom(() -> Group.triggerGroup(this)), // Group event
            "crash", () -> triggerRandom(() -> Crash.triggerCrash(this)), // Crash event
            "message", () -> triggerRandom(() -> RandomMessage.triggerMessage(this, MESSAGES_SET)), // Message event
            "stinky", () -> triggerRandom(() -> Stinky.triggerStinky(this)), // Stinky event
            "storm", () -> triggerRandom(() -> Storm.triggerStorm(this)) // Storm event
    );

    public void handleDebugRandom(CommandSender sender, String event) {
        Runnable eventAction = debugEvents.get(event); // Get event action by key
        if (eventAction != null) {
            eventAction.run(); // Execute event action
            if (sender != null) {
                sender.sendMessage("[DEBUG] Событие " + event + " выполнено."); // Send message to sender
            }
        }
    }

    private void triggerRandom(Runnable eventTrigger) {
        eventTrigger.run(); // Execute the given event trigger
    }

    private void executeRandomEvents() {
        // Get list of online players
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return; // Return if no players are online

        // Check and trigger random events based on chances
        checkAndTriggerRandom(Kick::triggerKick, "kick");
        checkAndTriggerRandom(Prank::triggerPrank, "prank");
        checkAndTriggerRandom(() -> Group.triggerGroup(this), "group");
        checkAndTriggerRandom(() -> Crash.triggerCrash(this), "crash");
        checkAndTriggerRandom(() -> RandomMessage.triggerMessage(this, MESSAGES_SET), "message");
        checkAndTriggerRandom(() -> Stinky.triggerStinky(this), "stinky");
        checkAndTriggerRandom(() -> Storm.triggerStorm(this), "storm");
    }

    private void checkAndTriggerRandom(Runnable eventTrigger, String eventKey) {
        // Get the chance for the given event
        Integer eventChance = actionsChances.get(eventKey);
        if (eventChance != null && eventChance > 0 && RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            eventTrigger.run(); // Trigger event if random chance is met
        }
    }
}
