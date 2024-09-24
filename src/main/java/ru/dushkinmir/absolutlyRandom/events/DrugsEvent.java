package ru.dushkinmir.absolutlyRandom.events;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import de.tr7zw.nbtapi.NBT;

public class DrugsEvent implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        // Проверяем, что событие вызвано правым кликом
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND) {
                ItemStack item = event.getItem();
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    switch (item.getType()) {
                        case SUGAR:
                            // Сахар - Мефедрон
                            meta.displayName(Component.text("Мефедрон"));
                            item.setItemMeta(meta);
                            NBT.modifyComponents(item, nbt -> {
                                ReadWriteNBT foodTag = nbt.getOrCreateCompound("minecraft:food");
                                foodTag.setInteger("nutrition", 0);
                                foodTag.setFloat("saturation", 0.0f);
                                foodTag.setBoolean("can_always_eat", true);

                                // Effects
                                ReadWriteNBTCompoundList effectsTag = foodTag.getCompoundList("effects");
                                ReadWriteNBT effectData;
                                // Speed
                                ReadWriteNBT speedEffectTag = effectsTag.addCompound();
                                effectData = speedEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:speed");
                                effectData.setInteger("duration", 100);
                                // Haste
                                ReadWriteNBT hasteEffectTag = effectsTag.addCompound();
                                effectData = hasteEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:speed");
                                effectData.setInteger("duration", 100);
                                // Weakness
                                ReadWriteNBT weaknessEffectTag = effectsTag.addCompound();
                                effectData = weaknessEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:weakness");
                                effectData.setInteger("duration", 100);
                                // Hunger
                                ReadWriteNBT hungerEffectTag = effectsTag.addCompound();
                                effectData = hungerEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:hunger");
                                effectData.setInteger("duration", 100);
                            });
                            break;

                        case WHITE_DYE:
                            // Белый краситель - Кокаин
                            meta.displayName(Component.text("Кокаин"));
                            item.setItemMeta(meta);
                            NBT.modifyComponents(item, nbt -> {
                                ReadWriteNBT foodTag = nbt.getOrCreateCompound("minecraft:food");
                                foodTag.setInteger("nutrition", 0);
                                foodTag.setFloat("saturation", 0.0f);
                                foodTag.setBoolean("can_always_eat", true);
                                // Effects
                                ReadWriteNBTCompoundList effectsTag = foodTag.getCompoundList("effects");
                                ReadWriteNBT effectData;
                                // Speed
                                ReadWriteNBT speedEffectTag = effectsTag.addCompound();
                                effectData = speedEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:speed");
                                effectData.setInteger("duration", 100);
                                // Haste
                                ReadWriteNBT strengthEffectTag = effectsTag.addCompound();
                                effectData = strengthEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:strength");
                                effectData.setInteger("duration", 100);
                                // Weakness
                                ReadWriteNBT nauseaEffectTag = effectsTag.addCompound();
                                effectData = nauseaEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:nausea");
                                effectData.setInteger("duration", 100);
                                // Hunger
                                ReadWriteNBT blindnessEffectTag = effectsTag.addCompound();
                                effectData = blindnessEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:blindness");
                                effectData.setInteger("duration", 100);
                            });
                            break;

                        case FERN:
                            // Папоротник - Марихуана
                            meta.displayName(Component.text("Марихуана"));
                            item.setItemMeta(meta);
                            NBT.modifyComponents(item, nbt -> {
                                ReadWriteNBT foodTag = nbt.getOrCreateCompound("minecraft:food");
                                foodTag.setInteger("nutrition", 0);
                                foodTag.setFloat("saturation", 0.0f);
                                foodTag.setBoolean("can_always_eat", true);
                                // Effects
                                ReadWriteNBTCompoundList effectsTag = foodTag.getCompoundList("effects");
                                ReadWriteNBT effectData;
                                // Regeneration
                                ReadWriteNBT regenerationEffectTag = effectsTag.addCompound();
                                effectData = regenerationEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:regeneration");
                                effectData.setInteger("duration", 100);
                                // Hunger
                                ReadWriteNBT hungerEffectTag = effectsTag.addCompound();
                                effectData = hungerEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:hunger");
                                effectData.setInteger("duration", 100);
                                // Slowness
                                ReadWriteNBT slownessEffectTag = effectsTag.addCompound();
                                effectData = slownessEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:slowness");
                                effectData.setInteger("duration", 100);
                                // Weakness
                                ReadWriteNBT weaknessEffectTag = effectsTag.addCompound();
                                effectData = weaknessEffectTag.getOrCreateCompound("effect");
                                effectData.setString("id", "minecraft:weakness");
                                effectData.setInteger("duration", 100);
                            });
                            break;

                        case POTION:
                            // Бутылочка воды - Водка
                            NBT.modifyComponents(item, nbt -> {
                                ReadWriteNBT potionTag = nbt.getOrCreateCompound("minecraft:potion_contents");
                                String waterPotion = potionTag.getKeys().toString();
                                event.getPlayer().sendMessage(waterPotion);
                            });
                            break;

                        case HONEY_BOTTLE:
                            // Бутылочка меда - Пиво
                            meta.displayName(Component.text("Пиво"));
                            item.setItemMeta(meta);
                            NBT.modifyComponents(item, nbt -> {
                                ReadWriteNBT foodTag = nbt.getOrCreateCompound("minecraft:food");
                                foodTag.setInteger("nutrition", 6);
                                foodTag.setFloat("saturation", 1.0f);
                                foodTag.setBoolean("can_always_eat", true);
                                // Effects
                                ReadWriteNBTCompoundList effectsTag = foodTag.getCompoundList("effects");
                                ReadWriteNBT effectTag;
                                // Nausea
                                ReadWriteNBT nauseaEffectTag = effectsTag.addCompound();
                                effectTag = nauseaEffectTag.getOrCreateCompound("effect");
                                effectTag.setString("id", "minecraft:nausea");
                                effectTag.setInteger("duration", 100);
                                // Resistance
                                ReadWriteNBT resistanceEffectTag = effectsTag.addCompound();
                                effectTag = resistanceEffectTag.getOrCreateCompound("effect");
                                effectTag.setString("id", "minecraft:resistance");
                                effectTag.setInteger("duration", 100);
                                // Mining fatigue
                                ReadWriteNBT miningFatigueEffectTag = effectsTag.addCompound();
                                effectTag = miningFatigueEffectTag.getOrCreateCompound("effect");
                                effectTag.setString("id", "minecraft:mining_fatigue");
                                effectTag.setInteger("duration", 100);
                                // Weakness
                                ReadWriteNBT weaknessEffectTag = effectsTag.addCompound();
                                effectTag = weaknessEffectTag.getOrCreateCompound("effect");
                                effectTag.setString("id", "minecraft:weakness");
                                effectTag.setInteger("duration", 100);
                            });
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }
}