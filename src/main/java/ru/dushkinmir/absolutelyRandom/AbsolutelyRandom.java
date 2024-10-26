package ru.dushkinmir.absolutelyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.randoms.*;
import ru.dushkinmir.absolutelyRandom.sex.AnalFissureHandler;
import ru.dushkinmir.absolutelyRandom.sex.SexEvent;
import ru.dushkinmir.absolutelyRandom.utils.AbsRandSQLiteDatabase;
import ru.dushkinmir.absolutelyRandom.utils.TelegramHelper;
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
    private boolean botEnabled;
    private AbsRandSQLiteDatabase database;
    private AnalFissureHandler fissureHandler; // Объявляем как нестатическое поле

    public static void main(String[] args) {
        System.out.println("пидисят два!!!");
    }

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
    }

    @Override
    public void onLoad() {
        registerCommands();
    }

    @Override
    public void onEnable() {
        logPluginActivation();
        CommandAPI.onEnable();
        try {
            openDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        scheduleEventTrigger();
        registerEvents();
        saveDefaultConfig();
        loadConfigValues();
        enableTelegramHelper();
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
        botEnabled = getConfig().getBoolean("telegram");
        reloadMessagesAsync();
    }

    private void reloadMessagesAsync() {
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            List<String> configMessages = getConfig().getStringList("random-messages");

            if (!configMessages.isEmpty()) {
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

    private void enableTelegramHelper() {
        if (botEnabled) {
            TelegramHelper.startServer(this);
        }
    }

    private void scheduleEventTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomEvents();
            }
        }.runTaskTimer(this, 0, SCHEDULE_PERIOD);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DrugsEvent(), this);
        getServer().getPluginManager().registerEvents(new VovaRandom(this), this);
        getServer().getPluginManager().registerEvents(new ConsentEvent(this), this);
    }

    private void openDatabase() throws SQLException {
        database = new AbsRandSQLiteDatabase(this); // Создаем экземпляр базы данных
        fissureHandler = new AnalFissureHandler(database, this); // Инициализируем обработчик анальной трещины
        WarpManager warpManager = new WarpManager(database, this);
        new WarpCommandManager(warpManager); // Инициализируем командный менеджер для варпов
        getServer().getPluginManager().registerEvents(fissureHandler, this);
    }

    private void closeDatabase() {
        if (database != null) {
            database.close(); // Закрываем пул соединений при отключении плагина
        }
    }

    private void registerCommands() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        Argument<?> noSelectorSuggestions = new PlayerArgument("target")
                .replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    // Получаем игрока, который вводит команду
                    Player senderPlayer = (Player) info.sender();
                    // Получаем всех онлайн игроков, кроме отправителя
                    return this.getServer().getOnlinePlayers().stream()
                            .filter(player -> !player.equals(senderPlayer)) // исключаем отправителя
                            .toArray(Player[]::new);
                }));

        new CommandAPICommand("debugevent")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <event>")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                "crash", "group", "kick", "message", "vova", "storm", "eschkere"))
                )
                .executes((sender, args) -> {
                    String event = (String) args.get("event");
                    assert event != null;
                    handleDebugRandom(sender, event);
                })
                .register(this);
        new CommandAPICommand("sex")
                .withArguments(noSelectorSuggestions)
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        Player target = (Player) args.get("target");
                        SexEvent.triggerSexEvent(player, target, this, fissureHandler);
                    }
                })
                .register();
    }

    public void handleDebugRandom(CommandSender sender, String event) {
        switch (event) {
            case "kick":
                triggerRandom(KickRandom::triggerKick, sender,
                        "Событие с киком игрока вызвано.");
                break;
            case "eschkere":
                triggerRandom(EschkereRandom::triggerEschkere, sender,
                        "ЕЩКЕРЕЕЕ");
            case "group":
                triggerRandom(() -> GroupRandom.triggerGroup(this), sender,
                        "Событие с выпадением блоков вызвано."
                );
                break;
            case "crash":
                triggerRandom(() -> CrashRandom.triggerCrash(this), sender,
                        "Краш сервера вызван.");
                break;
            case "message":
                triggerRandom(() -> MessageRandom.triggerMessage(this, MESSAGES_SET), sender,
                        "Событие с рандомным сообщением вызвано."
                );
                break;
            case "vova":
                triggerRandom(() -> VovaRandom.triggerVova(this), sender,
                        "Событие с облаком дыма вызвано"
                );
                break;
            case "storm":
                triggerRandom(() -> StormRandom.triggerStorm(this), sender,
                        "Событие с грозой вызвано");
            default:
                break;
        }
    }

    private void triggerRandom(Runnable eventTrigger, CommandSender sender, String message) {
        eventTrigger.run();
        if (sender != null) {
            sender.sendMessage(message);
        }
    }

    private void executeRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        checkAndTriggerEvent(KickRandom::triggerKick, kickChance);
        checkAndTriggerEvent(EschkereRandom::triggerEschkere, eschkereChance);
        checkAndTriggerEvent(() -> GroupRandom.triggerGroup(this), groupChance);
        checkAndTriggerEvent(() -> CrashRandom.triggerCrash(this), crashChance);
        checkAndTriggerEvent(() -> MessageRandom.triggerMessage(this, MESSAGES_SET), messageChance);
        checkAndTriggerEvent(() -> VovaRandom.triggerVova(this), vovaChance);
        checkAndTriggerEvent(() -> StormRandom.triggerStorm(this), stormChance);
    }

    private void checkAndTriggerEvent(Runnable eventTrigger, int eventChance) {
        if (RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}
