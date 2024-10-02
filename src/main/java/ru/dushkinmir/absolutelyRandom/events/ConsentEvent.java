package ru.dushkinmir.absolutelyRandom.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ConsentEvent implements Listener {
    private final Map<Player, Block> playerBlockMap = new HashMap<>();
    private final Plugin plugin;

    public ConsentEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && event.getAction().isRightClick() && block.getState() instanceof Container) {
            if (player.isSneaking()) return;
            playerBlockMap.put(player, block);
            openConsentMenu(player);
            event.setCancelled(true);
        }
    }

    private void openConsentMenu(Player player) {
        Inventory consentMenu = Bukkit.createInventory(null, 9, Component.text("Согласие на обработку данных", NamedTextColor.YELLOW));
        consentMenu.setItem(3, createItemStack(Material.GREEN_WOOL, "Принять", NamedTextColor.GREEN));
        consentMenu.setItem(4, createInfoItem());
        consentMenu.setItem(5, createItemStack(Material.RED_WOOL, "Отказаться", NamedTextColor.RED));
        player.openInventory(consentMenu);
    }

    private ItemStack createItemStack(Material material, String displayName, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName, color, TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.OAK_LOG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Договор о согласии", NamedTextColor.GOLD, TextDecoration.BOLD));
        meta.lore(Arrays.asList(
                Component.text("Соглашаясь, вы добровольно передаете", Style.style(TextDecoration.ITALIC)),
                Component.text("себя в вечное рабство этому серверу", Style.style(TextDecoration.ITALIC)),
                Component.text("Отказ приведет к немедленному накаказанию!", Style.style(TextDecoration.ITALIC)),
                Component.text("Соглашаясь, вы теряете свою свободу", Style.style(TextDecoration.ITALIC)),
                Component.text("и становитесь собственностью владельца сервера.", Style.style(TextDecoration.ITALIC))
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text(
                "Согласие на обработку данных",
                NamedTextColor.YELLOW,
                TextDecoration.BOLD))) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
            handleItemClick(player, clickedItem);
        }
    }


    private void handleItemClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.GREEN_WOOL) {
            Block block = playerBlockMap.get(player);
            if (block != null) {
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(((org.bukkit.block.Container) block.getState()).getInventory());
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            PlayerUtils.sendMessageToPlayer(player, Component.text("Китай партия вами не доволен! \uD83D\uDE21", NamedTextColor.RED), false);
            player.closeInventory();
            player.getWorld().strikeLightning(player.getLocation());
            player.getWorld().createExplosion(player.getLocation(), 2F, false, false);
        }
    }
}
