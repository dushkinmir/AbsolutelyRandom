package ru.dushkinmir.absolutelyRandom.utils.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryConfirmation {

    private final Component title;
    private final Component infoTitle;
    private final List<Component> infoLore;
    private final Component acceptTitle;
    private final Component declineTitle;

    public InventoryConfirmation(String titleText, NamedTextColor titleColor, String infoTitleText, NamedTextColor infoTitleColor, List<String> infoLoreTexts, NamedTextColor infoLoreColor, String acceptText, NamedTextColor acceptColor, String declineText, NamedTextColor declineColor) {
        this.title = Component.text(titleText, titleColor);
        this.infoTitle = Component.text(infoTitleText, infoTitleColor, TextDecoration.BOLD);
        this.infoLore = infoLoreTexts.stream().map(text -> InventoryConfirmation.createStyledText(text, infoLoreColor, TextDecoration.ITALIC)).toList();
        this.acceptTitle = Component.text(acceptText, acceptColor, TextDecoration.BOLD);
        this.declineTitle = Component.text(declineText, declineColor, TextDecoration.BOLD);
    }

    public void openConsentMenu(Player player) {
        Inventory consentMenu = Bukkit.createInventory(null, 9, this.title);
        consentMenu.setItem(3, createItemStack(Material.GREEN_WOOL, this.acceptTitle));
        consentMenu.setItem(4, createInfoItem());
        consentMenu.setItem(5, createItemStack(Material.RED_WOOL, this.declineTitle));
        player.openInventory(consentMenu);
    }

    private ItemStack createItemStack(Material material, Component displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem() {
        ItemStack item = new ItemStack(Material.OAK_LOG);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(this.infoTitle);
        meta.lore(this.infoLore);
        item.setItemMeta(meta);
        return item;
    }

    private static Component createStyledText(String text, NamedTextColor color, TextDecoration... decorations) {
        return Component.text(text, color, decorations);
    }
}