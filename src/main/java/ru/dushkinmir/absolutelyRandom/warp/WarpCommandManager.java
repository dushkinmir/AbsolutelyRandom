package ru.dushkinmir.absolutelyRandom.warp;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.utils.ConsentMenu;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.List;

public class WarpCommandManager implements Listener {

    private final WarpManager warpManager;
    private final ConsentMenu consentMenu;

    public WarpCommandManager(WarpManager warpManager, Plugin plugin) {
        this.warpManager = warpManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.consentMenu = new ConsentMenu(
                "Подтверждение", NamedTextColor.YELLOW,
                "Внимание: валюта не будет возвращена!", NamedTextColor.GOLD,
                List.of("Подтвердите удаление всех варпов."), NamedTextColor.DARK_PURPLE,
                "Подтвердить", NamedTextColor.GREEN,
                "Отменить", NamedTextColor.RED
        );
    }

    public void registerWarpCommands() {
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
                            return warpManager.getWarps(player, false).toArray(new String[0]);
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
                            return warpManager.getWarps(player, false).toArray(new String[0]);
                        })))
                .executesPlayer((player, args) -> {
                    String warpName = (String) args.get("warpName");
                    warpManager.deleteWarp(player, warpName);
                });

// Подкоманда "deleteall"
        CommandAPICommand warpDeleteAll = new CommandAPICommand("delall")
                .executesPlayer((player, args) -> {
                    PlayerUtils.sendMessageToPlayer(player, Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
                    consentMenu.openConsentMenu(player);
                });

        CommandAPICommand warpList = new CommandAPICommand("list")
                .executesPlayer((player, args) -> {
                    List<String> warps = warpManager.getWarps(player, true);

                    if (warps.isEmpty()) {
                        PlayerUtils.sendMessageToPlayer(
                                player,
                                Component.text("У вас нет созданных варпов.")
                                        .color(NamedTextColor.RED),
                                PlayerUtils.MessageType.CHAT);
                    } else {
                        String formattedWarps = String.join("\n ", warps);
                        PlayerUtils.sendMessageToPlayer(
                                player,
                                Component.text("Ваши варпы: \n " + formattedWarps)
                                        .color(NamedTextColor.GREEN),
                                PlayerUtils.MessageType.CHAT);
                    }
                });


        // Основная команда "warp" с подкомандами
        new CommandAPICommand("warp")
                .withSubcommand(warpCreate)
                .withSubcommand(warpTeleport)
                .withSubcommand(warpDelete)
                .withSubcommand(warpDeleteAll)
                .withSubcommand(warpList)
                .register();
    }

    // Обработка кликов в меню подтверждения удаления всех варпов
    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text(
                "Подтверждение",
                NamedTextColor.YELLOW))) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            handleItemClick(player, clickedItem);
        }
    }

    private void handleItemClick(Player player, ItemStack clickedItem) {
        // Обработка нажатия на кнопки
        if (clickedItem.getType() == Material.GREEN_WOOL) {
            warpManager.deleteAllWarps(player);
            player.closeInventory();
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            player.closeInventory();
            PlayerUtils.sendMessageToPlayer(player, Component.text("Удаление всех варпов отменено.").color(NamedTextColor.GREEN), PlayerUtils.MessageType.CHAT);
        }
    }
}
