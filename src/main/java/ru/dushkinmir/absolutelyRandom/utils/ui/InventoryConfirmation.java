package ru.dushkinmir.absolutelyRandom.utils.ui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryConfirmation implements Listener {

    private final Component title;

    private Runnable onConfirm;
    private Runnable onCancel;

    public InventoryConfirmation(Component title) {
        this.title = title;
    }

    public void showConfirmation(Player player, Component infoTitle, List<Component> infoLore, Component acceptTitle, Component declineTitle, Runnable onConfirm, Runnable onCancel) {
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;

        Inventory confirmationMenu = Bukkit.createInventory(null, 9, this.title);

        // Создаём и устанавливаем предметы
        confirmationMenu.setItem(3, createItem(Material.GREEN_WOOL, acceptTitle));
        confirmationMenu.setItem(4, createInfoItem(infoTitle, infoLore));
        confirmationMenu.setItem(5, createItem(Material.RED_WOOL, declineTitle));

        // Открываем меню игроку
        player.openInventory(confirmationMenu);
    }

    private ItemStack createItem(Material material, Component displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(Component infoTitle, List<Component> infoLore) {
        ItemStack item = new ItemStack(Material.OAK_LOG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(infoTitle);
        meta.lore(infoLore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (!event.getView().title().equals(this.title)) return;

        event.setCancelled(true);

        switch (event.getCurrentItem().getType()) {
            case Material.GREEN_WOOL -> {
                if (this.onConfirm != null) this.onConfirm.run();
            }
            case Material.RED_WOOL -> {
                if (this.onCancel != null) this.onCancel.run();
            }
        }
    }
}
