package ru.dushkinmir.absolutelyRandom.warp;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.List;

public class WarpCommandManager implements Listener {

    private final WarpManager warpManager;

    public WarpCommandManager(WarpManager warpManager) {
        this.warpManager = warpManager;
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
                    // Открыть меню подтверждения удаления всех варпов
                    Inventory deleteMenu = Bukkit.createInventory(null, 9, Component.text("Подтверждение", NamedTextColor.YELLOW));
                    deleteMenu.setItem(3, createItemStack(Material.GREEN_WOOL, "Подтвердить", NamedTextColor.GREEN));
                    deleteMenu.setItem(5, createItemStack(Material.RED_WOOL, "Отменить", NamedTextColor.RED));
                    // Создание блока дерева с информацией
                    deleteMenu.setItem(4, createInfoItem());
                    // Предупреждение для игрока
                    PlayerUtils.sendMessageToPlayer(player, Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
                    player.openInventory(deleteMenu);
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
                .withPermission(CommandPermission.fromString("absolutlyrandom.warp"))
                .withSubcommand(warpCreate)
                .withSubcommand(warpTeleport)
                .withSubcommand(warpDelete)
                .withSubcommand(warpDeleteAll)
                .withSubcommand(warpList)
                .register();
    }

    // Метод для создания ItemStack
    private ItemStack createItemStack(Material material, String displayName, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName, color, TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    // Метод для создания информационного предмета
    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.OAK_LOG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Внимание: валюта не будет возвращена!", NamedTextColor.RED, TextDecoration.BOLD));
        meta.lore(List.of(
                Component.text("Подтвердите удаление всех варпов.", NamedTextColor.YELLOW)
        ));
        item.setItemMeta(meta);
        return item;
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
}
