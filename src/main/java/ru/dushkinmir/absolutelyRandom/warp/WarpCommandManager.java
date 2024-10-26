package ru.dushkinmir.absolutelyRandom.warp;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WarpCommandManager {

    private final WarpManager warpManager;
    private final Plugin plugin;

    public WarpCommandManager(WarpManager warpManager, Plugin plugin) {
        this.warpManager = warpManager;
        this.plugin = plugin;
        registerWarpCommands();
    }

    private void registerWarpCommands() {
        // Подкоманда "create"
        CommandAPICommand warpCreate = new CommandAPICommand("create")
                .withArguments(new StringArgument("warpName"))
                .executesPlayer((player, args) -> {
                    String warpName = (String) args.get("warpName");
                    Location location = player.getLocation();
                    warpManager.createWarp(player, warpName, location);
                });

        // Подкоманда "teleport"
        CommandAPICommand warpTeleport = new CommandAPICommand("tp")
                .withArguments(new StringArgument("warpName")
                        .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                            Player player = (Player) info.sender();
                            return warpManager.getWarps(player).toArray(new String[0]);
                        })))
                .executesPlayer((player, args) -> {
                    String warpName = (String) args.get("warpName");
                    warpManager.teleportToWarp(player, warpName);
                });

        // Подкоманда "delete"
        CommandAPICommand warpDelete = new CommandAPICommand("del")
                .withArguments(new StringArgument("warpName")
                        .replaceSuggestions(ArgumentSuggestions.strings(info -> {
                            Player player = (Player) info.sender();
                            return warpManager.getWarps(player).toArray(new String[0]);
                        })))
                .executesPlayer((player, args) -> {
                    String warpName = (String) args.get("warpName");
                    warpManager.deleteWarp(player, warpName);
                });

        // Подкоманда "deleteall"
        CommandAPICommand warpDeleteAll = new CommandAPICommand("delall")
                .executesPlayer((player, args) -> {
                    warpManager.deleteAllWarps(player);
                });

        // Основная команда "warp" с подкомандами
        new CommandAPICommand("warp")
                .withPermission(CommandPermission.fromString("absolutlyrandom.warp"))
                .withSubcommand(warpCreate)
                .withSubcommand(warpTeleport)
                .withSubcommand(warpDelete)
                .withSubcommand(warpDeleteAll)
                .register();
    }
}
