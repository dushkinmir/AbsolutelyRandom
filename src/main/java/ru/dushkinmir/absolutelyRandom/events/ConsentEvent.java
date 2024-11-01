package ru.dushkinmir.absolutelyRandom.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConsentEvent implements Listener {
    private final Map<Player, Block> playerBlockMap = new HashMap<>();
    private final Plugin plugin;
    private final Random random = new Random();
    private static final Component CONSENT_TITLE = Component.text("Согласие на обработку данных", NamedTextColor.YELLOW);

    public ConsentEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void handlePlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (random.nextBoolean() && block != null && event.getAction().isRightClick() && block.getState() instanceof Container) {
            if (player.isSneaking()) return;
            playerBlockMap.put(player, block);
            openConsentMenu(player);
            event.setCancelled(true);
        }
    }

    private void openConsentMenu(Player player) {
        Inventory consentMenu = Bukkit.createInventory(null, 9, CONSENT_TITLE);
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
        meta.lore(List.of(
                createStyledText("Соглашаясь, вы добровольно передаете", TextDecoration.ITALIC),
                createStyledText("себя в вечное рабство этому серверу", TextDecoration.ITALIC),
                createStyledText("Отказ приведет к немедленному наказанию!", TextDecoration.ITALIC),
                createStyledText("Соглашаясь, вы теряете свою свободу", TextDecoration.ITALIC),
                createStyledText("и становитесь собственностью владельца сервера.", TextDecoration.ITALIC)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private Component createStyledText(String text, TextDecoration... decorations) {
        return Component.text(text, NamedTextColor.DARK_PURPLE, decorations);
    }

    @EventHandler
    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(CONSENT_TITLE)) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            handleItemClick(player, clickedItem);
        }
    }

    private void handleItemClick(Player player, ItemStack clickedItem) {
        if (clickedItem.getType() == Material.GREEN_WOOL) {
            Block block = playerBlockMap.remove(player);
            if (block != null) {
                player.closeInventory();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.openInventory(((Container) block.getState()).getInventory());
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else if (clickedItem.getType() == Material.RED_WOOL) {
            PlayerUtils.kickPlayer(player, Component.text("Китай партия вами не доволен! \uD83D\uDE21", NamedTextColor.RED));
        }
    }
}