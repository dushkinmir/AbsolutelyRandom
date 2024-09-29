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
import ru.dushkinmir.absolutelyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutelyRandom.randoms.*;

import java.util.*;

public class AbsolutelyRandom extends JavaPlugin {
    private static final long SCHEDULE_PERIOD = 20L;
    private static final long INITIAL_DELAY = 0L;
    private final Random randomGenerator = new Random();
    private int kickChance, groupChance, crashChance, messageChance, vovaChance;
    private boolean isEventActive = false;
    private static final Map<UUID, BukkitRunnable> playerTasks = new HashMap<>();

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
        return playerTasks;
    }

    private void logPluginActivation() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
    }

    private void logPluginDeactivation() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
        playerTasks.values().forEach(BukkitRunnable::cancel);
        playerTasks.clear();
    }

    private void loadConfigValues() {
        kickChance = getConfig().getInt("kick-event-chance");
        groupChance = getConfig().getInt("group-event-chance");
        crashChance = getConfig().getInt("crash-event-chance");
        messageChance = getConfig().getInt("message-event-chance");
        vovaChance = getConfig().getInt("vova-event-chance");
    }

    private void scheduleEventTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEventActive) {
                    executeRandomEvents();
                }
            }
        }.runTaskTimer(this, INITIAL_DELAY, SCHEDULE_PERIOD);
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new DrugsEvent(), this);
        getServer().getPluginManager().registerEvents(new VovaRandom(this), this);
    }

    private void registerCommands() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output
        new CommandAPICommand("debugevent")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <event>")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                "crash", "group", "kick", "message", "vova"))
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

        if (randomGenerator.nextInt(crashChance) == 0) {
            isEventActive = true;
            CrashRandom.triggerCrash(this);
            isEventActive = false;
        }

        checkAndTriggerEvent(() -> MessageRandom.triggerMessage(this), messageChance);
        checkAndTriggerEvent(() -> VovaRandom.triggerVova(this), vovaChance);
    }

    private void checkAndTriggerEvent(Runnable eventTrigger, int eventChance) {
        if (randomGenerator.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}
