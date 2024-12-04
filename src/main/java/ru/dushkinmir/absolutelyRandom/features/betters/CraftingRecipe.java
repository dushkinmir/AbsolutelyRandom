package ru.dushkinmir.absolutelyRandom.features.betters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


public class CraftingRecipe {
    private final Plugin plugin;

    public CraftingRecipe(Plugin plugin) {
        this.plugin = plugin;
        createRadioRecipe();
        createRadioRepairRecipe();
    }

    private void createRadioRecipe() {
        ItemStack radio = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = radio.getItemMeta();
        meta.displayName(Component.text("Radio", NamedTextColor.GOLD));

        // Внимание: здесь радио получает начальную прочность в 30 единиц
        NamespacedKey durabilityKey = new NamespacedKey(plugin, "durability");
        meta.getPersistentDataContainer().set(durabilityKey, PersistentDataType.INTEGER, 30);

        radio.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(plugin, "radio");
        ShapedRecipe recipe = new ShapedRecipe(key, radio);
        recipe.shape("III", "IRI", "III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(recipe);
    }

    private void createRadioRepairRecipe() {
        ItemStack brokenRadio = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta meta = brokenRadio.getItemMeta();
        meta.displayName(Component.text("Radio", NamedTextColor.GOLD));

        // Внимание: изначальная прочность радио равна 0, поскольку оно сломано
        NamespacedKey durabilityKey = new NamespacedKey(plugin, "durability");
        meta.getPersistentDataContainer().set(durabilityKey, PersistentDataType.INTEGER, 0);

        brokenRadio.setItemMeta(meta);

        ItemStack fixedRadio = new ItemStack(Material.NOTE_BLOCK);
        ItemMeta fixedMeta = fixedRadio.getItemMeta();
        fixedMeta.displayName(Component.text("Radio", NamedTextColor.GOLD));

        // Внимание: прочность радио восстанавливается до 30, поскольку оно ремонтируется
        fixedMeta.getPersistentDataContainer().set(durabilityKey, PersistentDataType.INTEGER, 30);

        fixedRadio.setItemMeta(fixedMeta);

        NamespacedKey key = new NamespacedKey(plugin, "radioRepaired");
        // Создаем рецепт без определенной формы
        ShapelessRecipe recipe = new ShapelessRecipe(key, fixedRadio);

        // Добавляем ингредиенты в любом порядке
        recipe.addIngredient(Material.IRON_INGOT);
        recipe.addIngredient(Material.IRON_INGOT);
        recipe.addIngredient(brokenRadio.getType());

        Bukkit.addRecipe(recipe);
    }
}