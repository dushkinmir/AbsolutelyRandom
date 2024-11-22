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
import ru.dushkinmir.absolutelyRandom.utils.ARDatabaseManager;
import ru.dushkinmir.absolutelyRandom.utils.ARWebSocketServer;
import ru.dushkinmir.absolutelyRandom.warp.WarpCommandManager;
import ru.dushkinmir.absolutelyRandom.warp.WarpManager;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbsolutelyRandom extends JavaPlugin implements Listener {
    private static final long SCHEDULE_PERIOD = 20L;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>();
    private static final Set<String> MESSAGES_SET = new HashSet<>();
    private static final long RELOAD_INTERVAL = 20 * 60 * 5; // Каждые 5 минут
    private final Map<String, Integer> actionsChances = new ConcurrentHashMap<>();
    private final Map<String, Boolean> enabledBetters = new ConcurrentHashMap<>();
    private ARDatabaseManager database;
    private AnalFissureHandler fissureHandler; // Объявляем как нестатическое поле
    private WarpManager warpManager;
    private ARWebSocketServer wsserver;

    public static void main(String[] args) {
        System.out.println("пидисят два!!!");
    }

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        try {
            openDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        logPluginActivation();
        saveDefaultConfig();
        loadConfigValues();
        startAutoReloadTask();
        scheduleActionTrigger();
        enableWebSocketServer();
        try {
            warpManager = new WarpManager(database, this);
            fissureHandler = new AnalFissureHandler(database, this);
            registerEvents();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        new CraftingRecipe(this);
        registerCommands();
    }

    @Override
    public void onDisable() {
        closeDatabase();
        logPluginDeactivation();
        try {
            wsserver.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void enableWebSocketServer() {
        String serverIp = this.getServer().getIp().isEmpty() ? "localhost" : this.getServer().getIp();
        int port = this.getServer().getPort() + 1;
        wsserver = new ARWebSocketServer(serverIp, port, getLogger());
        wsserver.start();
    }

    private void logPluginActivation() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
        CommandAPI.onEnable();
    }

    private void logPluginDeactivation() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
        PLAYER_TASKS.values().forEach(BukkitRunnable::cancel);
        PLAYER_TASKS.clear();
        CommandAPI.onDisable();
    }

    private void loadConfigValues() {
        actionsChances.put("kick", getConfig().getInt("chances.kick-chance", -1));
        actionsChances.put("group", getConfig().getInt("chances.group-chance", -1));
        actionsChances.put("crash", getConfig().getInt("chances.crash-chance", -1));
        actionsChances.put("message", getConfig().getInt("chances.message-chance", -1));
        actionsChances.put("stinky", getConfig().getInt("chances.vova-chance", -1)); // vova -> stinky
        actionsChances.put("storm", getConfig().getInt("chances.storm-chance", -1));
        actionsChances.put("prank", getConfig().getInt("chances.eschkere-chance", -1)); // eschkere -> prank
        enabledBetters.put("name-hider", getConfig().getBoolean("betters.name-hider", false));
        enabledBetters.put("head-chat", getConfig().getBoolean("betters.head-chat", false));
    }

    private void reloadMessagesAsync() {
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
        new BukkitRunnable() {
            @Override
            public void run() {
                reloadMessagesAsync(); // Автоматическая перезагрузка
            }
        }.runTaskTimer(this, 0L, RELOAD_INTERVAL);
    }

    private void scheduleActionTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomEvents();
            }
        }.runTaskTimer(this, 0, SCHEDULE_PERIOD);
    }

    private void registerEvents() {
        List<Listener> events = Arrays.asList(
                new DrugsEvent(),
                new Stinky(this),
                new ConsentEvent(this),
                fissureHandler
        );

        addConditionalEvent(events, "name-hider", new NameHider(this));
        addConditionalEvent(events, "head-chat", new HeadChat(this));

        for (Listener event : events) {
            getServer().getPluginManager().registerEvents(event, this);
        }
    }

    private void addConditionalEvent(List<Listener> events, String conditionKey, Listener listener) {
        if (enabledBetters.get(conditionKey).equals(true)) {
            events.add(listener);
        }
    }

    private void openDatabase() throws SQLException {
        database = new ARDatabaseManager(this); // Создаем экземпляр базы данных
    }

    private void closeDatabase() {
        if (database != null) {
            database.close(); // Закрываем пул соединений при отключении плагина
        }
    }

    private void registerCommands() {
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
                    handleDebugRandom(sender, event);
                })
                .register(this);
        WarpCommandManager wcm = new WarpCommandManager(warpManager, this);
        SexCommandManager scm = new SexCommandManager(fissureHandler, this);
        wcm.registerWarpCommands();
        scm.registerSexCommand();
    }

    private final Map<String, Runnable> debugEvents = Map.of(
            "kick", () -> triggerRandom(Kick::triggerKick),
            "eschkere", () -> triggerRandom(Prank::triggerPrank),
            "group", () -> triggerRandom(() -> Group.triggerGroup(this)),
            "crash", () -> triggerRandom(() -> Crash.triggerCrash(this)),
            "message", () -> triggerRandom(() -> RandomMessage.triggerMessage(this, MESSAGES_SET)),
            "stinky", () -> triggerRandom(() -> Stinky.triggerStinky(this)),
            "storm", () -> triggerRandom(() -> Storm.triggerStorm(this))
    );

    public void handleDebugRandom(CommandSender sender, String event) {
        Runnable eventAction = debugEvents.get(event);
        if (eventAction != null) {
            eventAction.run();
            if (sender != null) {
                sender.sendMessage("[DEBUG] Событие " + event + " выполнено.");
            }
        }
    }

    private void triggerRandom(Runnable eventTrigger) {
        eventTrigger.run();
    }

    private void executeRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        checkAndTriggerRandom(Kick::triggerKick, "kick");
        checkAndTriggerRandom(Prank::triggerPrank, "prank");
        checkAndTriggerRandom(() -> Group.triggerGroup(this), "group");
        checkAndTriggerRandom(() -> Crash.triggerCrash(this), "crash");
        checkAndTriggerRandom(() -> RandomMessage.triggerMessage(this, MESSAGES_SET), "message");
        checkAndTriggerRandom(() -> Stinky.triggerStinky(this), "stinky");
        checkAndTriggerRandom(() -> Storm.triggerStorm(this), "storm");
    }

    private void checkAndTriggerRandom(Runnable eventTrigger, String eventKey) {
        Integer eventChance = actionsChances.get(eventKey);
        if (eventChance != null && eventChance > 0 && RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}
