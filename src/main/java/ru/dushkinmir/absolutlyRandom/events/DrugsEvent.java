package ru.dushkinmir.absolutlyRandom.events;

import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import de.tr7zw.nbtapi.NBT;

import java.util.Map;

public class DrugsEvent implements Listener {

    private void applyDrugEffects(ItemStack item, String drugName, Map<String, Integer> effects, int nutrition, float saturation) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(drugName));
        item.setItemMeta(meta);
        NBT.modify(item, nbt -> {
            nbt.setString("drug", "вова хорошенький");
        });

        NBT.modifyComponents(item, nbt -> {
            ReadWriteNBT foodTag = nbt.getOrCreateCompound("minecraft:food");
            foodTag.setInteger("nutrition", nutrition);
            foodTag.setFloat("saturation", saturation);
            foodTag.setBoolean("can_always_eat", true);

            ReadWriteNBTCompoundList effectsTag = foodTag.getCompoundList("effects");
            effects.forEach((effectId, duration) -> {
                ReadWriteNBT effectTag = effectsTag.addCompound().getOrCreateCompound("effect");
                effectTag.setString("id", effectId);
                effectTag.setInteger("duration", duration);
            });
        });
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND) {
                ItemStack item = event.getItem();
                if (item != null) {
                    switch (item.getType()) {
                        case SUGAR:
                            applyDrugEffects(item, "Мефедрон", Map.of(
                                    "minecraft:speed", 100,
                                    "minecraft:haste", 100,
                                    "minecraft:weakness", 100,
                                    "minecraft:hunger", 100
                            ), 0, 0.0f);
                            break;
                        case WHITE_DYE:
                            applyDrugEffects(item, "Кокаин", Map.of(
                                    "minecraft:speed", 100,
                                    "minecraft:strength", 100,
                                    "minecraft:nausea", 100,
                                    "minecraft:blindness", 100
                            ), 0, 0.0f);
                            break;
                        case FERN:
                            applyDrugEffects(item, "Марихуана", Map.of(
                                    "minecraft:regeneration", 100,
                                    "minecraft:hunger", 100,
                                    "minecraft:slowness", 100,
                                    "minecraft:weakness", 100
                            ), 0, 0.0f);
                            break;
                        case HONEY_BOTTLE:
                            applyDrugEffects(item, "Пиво", Map.of(
                                    "minecraft:nausea", 100,
                                    "minecraft:resistance", 100,
                                    "minecraft:mining_fatigue", 100,
                                    "minecraft:weakness", 100
                            ), 6, 1.0f);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND) {
                GameMode gameMode = event.getPlayer().getGameMode();
                if (!gameMode.equals(GameMode.CREATIVE) && !gameMode.equals(GameMode.SPECTATOR)) {
                    event.getPlayer().sendMessage(Component.text("дебил хули ты тычешь, совсем уже под солям объебан"));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerConsumeDrug(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        NBT.get(item, nbt -> {
            if ("вова хорошенький".equals(nbt.getString("drug"))) {
                String rawDruggedPlayer = "агаа!! %s у нас тут оказыватся балуется %s";
                String druggedPlayer = rawDruggedPlayer.formatted(event.getPlayer().getName(), "%s");
                rawDruggedPlayer = "уф бляя мощненько так закинулся %s";
                Map<Material, String> drugMessages = Map.of(
                        Material.WHITE_DYE, "мефедрончиком",
                        Material.SUGAR, "кокаинчиком",
                        Material.FERN, "марихуаной",
                        Material.HONEY_BOTTLE, "пивком"
                );

                if (drugMessages.containsKey(item.getType())) {
                    String drugType = drugMessages.get(item.getType());
                    event.getPlayer().getWorld().sendMessage(Component.text(druggedPlayer.formatted(drugType)));
                    druggedPlayer = rawDruggedPlayer.formatted(drugType);
                    event.getPlayer().sendActionBar(Component.text(druggedPlayer));
                }
            }
        });
    }
}