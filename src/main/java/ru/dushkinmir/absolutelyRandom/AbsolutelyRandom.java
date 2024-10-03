package ru.dushkinmir.absolutelyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.randoms.*;

import java.util.*;

public class AbsolutelyRandom extends JavaPlugin {
    private static final long SCHEDULE_PERIOD = 20L;
    private static final long INITIAL_DELAY = 0L;
    private int kickChance, groupChance, crashChance, messageChance, vovaChance, stormChance;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final Map<UUID, BukkitRunnable> PLAYER_TASKS = new HashMap<>();


    public static void main(String[] args) {
        System.out.println("Z");
    }

    @Override
    public void onLoad() {
        registerCommands();
    }

    @Override
    public void onEnable() {
        logPluginActivation();
        CommandAPI.onEnable();
        scheduleEventTrigger();
        registerEvents();
        saveDefaultConfig();
        loadConfigValues();
    }

    @Override
    public void onDisable() {
        logPluginDeactivation();
        CommandAPI.onDisable();
    }

    public static Map<UUID, BukkitRunnable> getPlayerTasks() {
        return PLAYER_TASKS;
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
    }

    private void scheduleEventTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomEvents();
            }
        }.runTaskTimer(this, INITIAL_DELAY, SCHEDULE_PERIOD);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DrugsEvent(), this);
        getServer().getPluginManager().registerEvents(new VovaRandom(this), this);
        getServer().getPluginManager().registerEvents(new ConsentEvent(this), this);
    }

    private void registerCommands() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        new CommandAPICommand("debugevent")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <event>")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                "crash", "group", "kick", "message", "vova", "storm"))
                )
                .executes((sender, args) -> {
                    String event = (String) args.get("event");
                    assert event != null;
                    handleDebugRandom(sender, event);
                })
                .register(this);
    }

    private void handleDebugRandom(CommandSender sender, String event) {
        switch (event) {
            case "kick":
                triggerRandom(KickRandom::triggerKick, sender, "Событие с киком игрока вызвано.");
                break;
            case "group":
                triggerRandom(() -> GroupRandom.triggerGroup(this), sender,
                        "Событие с выпадением блоков вызвано."
                );
                break;
            case "crash":
                triggerRandom(() -> CrashRandom.triggerCrash(this), sender, "Краш сервера вызван.");
                break;
            case "message":
                triggerRandom(() -> MessageRandom.triggerMessage(this), sender,
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
        sender.sendMessage(message);
    }

    private void executeRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        checkAndTriggerEvent(KickRandom::triggerKick, kickChance);
        checkAndTriggerEvent(() -> GroupRandom.triggerGroup(this), groupChance);
        checkAndTriggerEvent(() -> CrashRandom.triggerCrash(this), crashChance);
        checkAndTriggerEvent(() -> MessageRandom.triggerMessage(this), messageChance);
        checkAndTriggerEvent(() -> VovaRandom.triggerVova(this), vovaChance);
        checkAndTriggerEvent(() -> StormRandom.triggerStorm(this), stormChance);
    }

    private void checkAndTriggerEvent(Runnable eventTrigger, int eventChance) {
        if (RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}
