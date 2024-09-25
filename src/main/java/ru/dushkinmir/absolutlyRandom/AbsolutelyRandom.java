package ru.dushkinmir.absolutlyRandom;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutlyRandom.events.*;
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
        System.out.println("Z");
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).verboseOutput(true)); // Load with verbose output

    }

    @Override
    public void onEnable() {
        getLogger().info("AbsolutelyRandomPlugin has been enabled!");
        getLogger().info("Пусть на вашем сервере царит рандом!!");
        CommandAPI.onEnable();
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String commandName = command.getName().toLowerCase();

        // Проверка команды /sex, доступна всем игрокам
//        if (commandName.equals("sex")) {
//            if (!(sender instanceof Player player)) {
//                sender.sendMessage("This command can only be used by players.");
//                return true;
//            }
//
//            if (args.length != 1) {
//                sender.sendMessage("Используйте: /sex <ник игрока>");
//                return true;
//            }
//
//            String targetName = args[0];
//
//            // Запуск события SexEvent
//            new SexEvent(this).triggerSexEvent(player, targetName, this);
//            return true;
//        }
        if (!sender.hasPermission("absolutlyrandom.admin")) {
            sender.sendMessage("У вас нет прав для использования этой команды.");
            return true;
        }

        // Проверяем, является ли команда "debug"
        if (commandName.equals("debug") || commandName.equals("debugevent")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return true;
            }
            // Проверяем наличие аргументов
            if (args.length != 1) {
                sender.sendMessage("Usage: /debug <event>");
                return true;
            }

            // Перенаправляем на обработку события
            handleDebugEvent(sender, args);
            return true;
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
    private void handleDebugEvent(CommandSender sender, String[] args) {
        String eventName = args[0].toLowerCase(); // Получаем название события

        switch (eventName) {
            case "kick":
                processKickEvent(sender); // Вызов метода для обработки кика
                break;
            case "group":
                processGroupEvent(sender); // Вызов метода для обработки группового события
                break;
            case "crash":
                processCrashEvent(sender); // Вызов метода для обработки краша
                break;
            case "message":
                processMessageEvent(sender); // Вызов метода для обработки сообщения
                break;
            case "vova":
                processVovaEvent(sender); // Вызов метода для обработки события "вова"
                break;
            default:
                sender.sendMessage("Unknown event. Available events: kick, group, crash, message, vova.");
                break;
        }
    }
}