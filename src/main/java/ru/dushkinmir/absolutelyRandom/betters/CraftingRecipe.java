package ru.dushkinmir.absolutelyRandom.betters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;


public class CraftingRecipe {
    private final Plugin plugin;

    public CraftingRecipe(Plugin plugin) {
        this.plugin = plugin;
        createRadioRecipe();
    }

    private void createRadioRecipe() {
        ItemStack radio = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = radio.getItemMeta();
        meta.displayName(Component.text("Radio", NamedTextColor.GOLD));
        radio.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(plugin, "unique_key");
        ShapedRecipe recipe = new ShapedRecipe(key, radio);
        recipe.shape("III", "IRI", "III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(recipe);
    }
}