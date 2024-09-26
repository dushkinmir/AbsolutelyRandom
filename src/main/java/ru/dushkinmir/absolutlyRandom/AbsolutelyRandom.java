package ru.dushkinmir.absolutlyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutlyRandom.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbsolutelyRandom extends JavaPlugin {
    private static final long SCHEDULE_PERIOD = 20L;
    private static final long INITIAL_DELAY = 0L;
    private final Random randomGenerator = new Random();
    private int kickEventChance, groupEventChance, crashEventChance, messageEventChance, vovaEventChance;
    private boolean isEventActive = false;

    public static void main(String[] args) {
        System.out.println("Z");
    }

    @Override
    public void onEnable() {
        logPluginActivation();
        scheduleEventTrigger();
        registerEventsAndCommands();
        saveDefaultConfig();
        loadConfigValues();
    }

    @Override
    public void onDisable() {
        logPluginDeactivation();
        CommandAPI.unregister("debug");
    }

    private void logPluginActivation() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
    }

    private void logPluginDeactivation() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
    }

    private void loadConfigValues() {
        kickEventChance = getConfig().getInt("kick-event-chance");
        groupEventChance = getConfig().getInt("group-event-chance");
        crashEventChance = getConfig().getInt("crash-event-chance");
        messageEventChance = getConfig().getInt("message-event-chance");
        vovaEventChance = getConfig().getInt("vova-event-chance");
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

    private void registerEventsAndCommands() {
        getServer().getPluginManager().registerEvents(new DrugsEvent(), this);
        registerDebugCommand();
    }

    private void registerDebugCommand() {
        new CommandAPICommand("debug")
                .withPermission("absolutlyrandom.admin")
                .withUsage("/debug <event>")
                .withArguments(new StringArgument("event")
                        .replaceSuggestions(ArgumentSuggestions.strings("crash", "group", "kick", "message", "vova")))
                .executes((sender, args) -> {
                    String event = (String) args.get("event");
                    assert event != null;
                    handleDebugEvent(sender, event);
                })
                .register(this);
    }

    private void handleDebugEvent(CommandSender sender, String event) {
        switch (event) {
            case "kick":
                triggerEvent(KickEvent::triggerKickEvent, sender, "Событие с киком игрока вызвано.");
                break;
            case "group":
                triggerEvent(() -> GroupEvent.triggerGroupEvent(this), sender, "Событие с выпадением блоков вызвано.");
                break;
            case "crash":
                triggerEvent(() -> CrashEvent.triggerCrashEvent(this), sender, "Краш сервера вызван.");
                break;
            case "message":
                triggerEvent(() -> RandomMessageEvent.triggerRandomMessageEvent(this), sender, "Событие с рандомным сообщением вызвано.");
                break;
            case "vova":
                triggerEvent(() -> VovaEvent.triggerVovaEvent(this), sender, "Событие с облаком дыма вызвано");
                break;
            default:
                break;
        }
    }

    private void triggerEvent(Runnable eventTrigger, CommandSender sender, String message) {
        eventTrigger.run();
        sender.sendMessage(message);
    }

    private void executeRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        checkAndTriggerEvent(KickEvent::triggerKickEvent, kickEventChance);
        checkAndTriggerEvent(() -> GroupEvent.triggerGroupEvent(this), groupEventChance);

        if (randomGenerator.nextInt(crashEventChance) == 0) {
            isEventActive = true;
            CrashEvent.triggerCrashEvent(this);
            isEventActive = false;
        }

        checkAndTriggerEvent(() -> RandomMessageEvent.triggerRandomMessageEvent(this), messageEventChance);
        checkAndTriggerEvent(() -> VovaEvent.triggerVovaEvent(this), vovaEventChance);
    }

    private void checkAndTriggerEvent(Runnable eventTrigger, int eventChance) {
        if (randomGenerator.nextInt(eventChance) == 0) {
            eventTrigger.run();
        }
    }
}