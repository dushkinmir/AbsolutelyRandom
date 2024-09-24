package ru.dushkinmir.absolutlyRandom;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutlyRandom.events.CrashEvent;
import ru.dushkinmir.absolutlyRandom.events.GroupEvent;
import ru.dushkinmir.absolutlyRandom.events.KickEvent;
import ru.dushkinmir.absolutlyRandom.events.RandomMessageEvent;
import ru.dushkinmir.absolutlyRandom.events.DrugsEvent;
import ru.dushkinmir.absolutlyRandom.events.VovaEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbsolutelyRandom extends JavaPlugin {
    private final Random randomGenerator = new Random();
    private int kickEventChance;
    private int groupEventChance;
    private int crashEventChance;
    private int messageEventChance;
    private int vovaEventChance;
    private boolean isEventActive = false;

    public static void main(String[] args) {
        System.out.println("оаоаоаоао");
    }

    @Override
    public void onEnable() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
        scheduleRandomEventTrigger();
        getServer().getPluginManager().registerEvents(new DrugsEvent(), this);
        saveDefaultConfig();
        loadConfigValues();
    }

    @Override
    public void onDisable() {
        getLogger().info("AbsolutelyRandomPlugin has been disabled!");
    }

    private void loadConfigValues() {
        kickEventChance = getConfig().getInt("kick-event-chance");
        groupEventChance = getConfig().getInt("group-event-chance");
        crashEventChance = getConfig().getInt("crash-event-chance");
        messageEventChance = getConfig().getInt("message-event-chance");
        vovaEventChance = getConfig().getInt("vova-event-chance");
    }

    private void scheduleRandomEventTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isEventActive) {
                    triggerRandomEvents();
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 20L = 1 секунда
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("absolutlyrandom.admin") && !sender.isOp()) {
            sender.sendMessage("У вас нет прав для использования этой команды.");
            return true;
        }
        String commandName = command.getName().toLowerCase();
        switch (commandName) {
            case "triggerkick":
                processKickEvent(sender);
                break;
            case "triggerevent":
                processGroupEvent(sender);
                break;
            case "triggercrash":
                processCrashEvent(sender);
                break;
            case "triggermessage":
                processMessageEvent(sender);
                break;
            case "triggervova":
                processVovaEvent(sender);
                break;
            default:
                return false;
        }
        return true;
    }

    private void processKickEvent(CommandSender sender) {
        KickEvent.triggerKickEvent();
        sender.sendMessage("Событие с киком игрока вызвано.");
    }

    private void processGroupEvent(CommandSender sender) {
        GroupEvent.triggerGroupEvent(this);
        sender.sendMessage("Событие с выпадением блоков вызвано.");
    }

    private void processCrashEvent(CommandSender sender) {
        CrashEvent.triggerCrashEvent(this);
        sender.sendMessage("Краш сервера вызван.");
    }

    private void processMessageEvent(CommandSender sender) {
        RandomMessageEvent.triggerRandomMessageEvent(this);
        sender.sendMessage("Событие с рандомным сообщением вызвано.");
    }

    private void processVovaEvent(CommandSender sender) {
        VovaEvent.triggerVovaEvent(this);
        sender.sendMessage("Событие с облаком дыма вызвано");
    }

    private void triggerRandomEvents() {
        List<Player> players = new ArrayList<>(getServer().getOnlinePlayers());
        if (players.isEmpty()) return;

        if (randomGenerator.nextInt(kickEventChance) == 0) {
            KickEvent.triggerKickEvent();
        }
        if (randomGenerator.nextInt(groupEventChance) == 0) {
            GroupEvent.triggerGroupEvent(this);
        }
        if (randomGenerator.nextInt(crashEventChance) == 0) {
            isEventActive = true;
            CrashEvent.triggerCrashEvent(this);
            isEventActive = false;
        }
        if (randomGenerator.nextInt(messageEventChance) == 0) {
            RandomMessageEvent.triggerRandomMessageEvent(this);
        }
        if (randomGenerator.nextInt(vovaEventChance) == 0) {
            VovaEvent.triggerVovaEvent(this);
        }
    }
}