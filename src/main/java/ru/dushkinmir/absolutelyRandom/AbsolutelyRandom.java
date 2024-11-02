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
import ru.dushkinmir.absolutelyRandom.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.randoms.*;
import ru.dushkinmir.absolutelyRandom.sex.AnalFissureHandler;
import ru.dushkinmir.absolutelyRandom.sex.SexCommandManager;
import ru.dushkinmir.absolutelyRandom.utils.AbsRandSQLiteDatabase;
import ru.dushkinmir.absolutelyRandom.warp.WarpCommandManager;
import ru.dushkinmir.absolutelyRandom.warp.WarpManager;

import java.sql.SQLException;
import java.util.*;

public class AbsolutelyRandom extends JavaPlugin implements Listener {
    private static final long SCHEDULE_PERIOD = 20L;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>();
    private static final Set<String> MESSAGES_SET = new HashSet<>();
    private static final long RELOAD_INTERVAL = 20 * 60 * 5; // Каждые 5 минут
    private int kickChance, groupChance, crashChance, messageChance, vovaChance, stormChance, eschkereChance;
    private AbsRandSQLiteDatabase database;
    private AnalFissureHandler fissureHandler; // Объявляем как нестатическое поле
    private WarpManager warpManager;

    public static void main(String[] args) {
        System.out.println("пидисят два!!!");
    }

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
    }

    @Override
    public void onLoad() {
        registerCommands();
        try {
            openDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEnable() {
        logPluginActivation();
        CommandAPI.onEnable();
        scheduleEventTrigger();
        try {
            registerEvents();
            warpManager = new WarpManager(database, this);
            fissureHandler = new AnalFissureHandler(database, this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        saveDefaultConfig();
        loadConfigValues();
        startAutoReloadTask();
    }

    @Override
    public void onDisable() {
        closeDatabase();
        logPluginDeactivation();
        CommandAPI.onDisable();
    }

    private void logPluginActivation() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
    }

    private void logPluginDeactivation() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
        PLAYER_TASKS.values().forEach(BukkitRunnable::cancel);
        PLAYER_TASKS.clear();
    }

    private void loadConfigValues() {
        kickChance = getConfig().getInt("kick-chance");
        groupChance = getConfig().getInt("group-chance");
        crashChance = getConfig().getInt("crash-chance");
        messageChance = getConfig().getInt("message-chance");
        vovaChance = getConfig().getInt("vova-chance");
        stormChance = getConfig().getInt("storm-chance");
        eschkereChance = getConfig().getInt("eschkere-chance");
        reloadMessagesAsync();
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

    private void scheduleEventTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomEvents();
            }
        }.runTaskTimer(this, 0, SCHEDULE_PERIOD);
    }

    private void registerEvents() throws SQLException {
        List<Listener> events = Arrays.asList(
                new DrugsEvent(),
                new StinkyRandom(this),
                new ConsentEvent(this),
                new WarpCommandManager(warpManager),
                fissureHandler
        );

        for (Listener event : events) {
            getServer().getPluginManager().registerEvents(event, this);
        }
    }

    private void openDatabase() throws SQLException {
        database = new AbsRandSQLiteDatabase(this); // Создаем экземпляр базы данных
    }

    private void closeDatabase() {
        if (database != null) {
            database.close(); // Закрываем пул соединений при отключении плагина
        }
    }

    private void registerCommands() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        new CommandAPICommand("debugevent")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <event>")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                new ArrayList<>(debugEvents.keySet())))
                )
                .executes((sender, args) -> {
                    String event = (String) args.get("event");
                    assert event != null;
                    handleDebugRandom(sender, event);
                })
                .register(this);
        WarpCommandManager wcm = new WarpCommandManager(warpManager);
        wcm.registerWarpCommands();
        SexCommandManager scm = new SexCommandManager(fissureHandler, this);
        scm.registerSexCommand();
    }

    private final Map<String, Runnable> debugEvents = Map.of(
            "kick", () -> triggerRandom(KickRandom::triggerKick),
            "eschkere", () -> triggerRandom(EschkereRandom::triggerEschkere),
            "group", () -> triggerRandom(() -> GroupRandom.triggerGroup(this)),
            "crash", () -> triggerRandom(() -> CrashRandom.triggerCrash(this)),
            "message", () -> triggerRandom(() -> MessageRandom.triggerMessage(this, MESSAGES_SET)),
            "vova", () -> triggerRandom(() -> StinkyRandom.triggerVova(this)),
            "storm", () -> triggerRandom(() -> StormRandom.triggerStorm(this))
    );

    public void handleDebugRandom(CommandSender sender, String event) {
        Runnable eventAction = debugEvents.get(event);
        if (eventAction != null) {
            eventAction.run();
            if (sender != null) sender.sendMessage("Событие " + event + " выполнено.");
        }
    }

    private void triggerRandom(Runnable eventTrigger) {
        eventTrigger.run();
    }

    private void executeRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        checkAndTriggerEvent(KickRandom::triggerKick, kickChance);
        checkAndTriggerEvent(EschkereRandom::triggerEschkere, eschkereChance);
        checkAndTriggerEvent(() -> GroupRandom.triggerGroup(this), groupChance);
        checkAndTriggerEvent(() -> CrashRandom.triggerCrash(this), crashChance);
        checkAndTriggerEvent(() -> MessageRandom.triggerMessage(this, MESSAGES_SET), messageChance);
        checkAndTriggerEvent(() -> StinkyRandom.triggerVova(this), vovaChance);
        checkAndTriggerEvent(() -> StormRandom.triggerStorm(this), stormChance);
    }

    private void checkAndTriggerEvent(Runnable eventTrigger, int eventChance) {
        if (RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}
