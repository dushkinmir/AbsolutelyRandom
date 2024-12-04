package ru.dushkinmir.absolutelyRandom.features.events;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.Map;

public class DrugsEvent implements Listener {

    private void applyDrugEffects(ItemStack item, String drugName, Map<String, Integer> effects, int nutrition,
                                  float saturation, Block clickedBlock, Player player) {
        player.getWorld().spawnParticle(
                Particle.DUST,
                clickedBlock.getLocation(),
                75, 1, 1, 1,
                new Particle.DustOptions(Color.GRAY, 1.7f));
        player.getWorld().playSound(clickedBlock.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 1.0f);
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
        if (event.getAction().isRightClick() && event.getPlayer().isSneaking()) {
            Block clickedBlock = event.getClickedBlock();
            ItemStack item = event.getItem();
            if (clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND && item != null) {
                NBT.get(item, nbt -> {
                    if (!nbt.hasTag("drug")) {
                        switch (item.getType()) {
                            case SUGAR:
                                applyDrugEffects(item, "Кокаин", Map.of(
                                        "minecraft:speed", 100,
                                        "minecraft:haste", 100,
                                        "minecraft:weakness", 100,
                                        "minecraft:hunger", 100
                                ), 0, 0.0f, clickedBlock, event.getPlayer());
                                break;
                            case WHITE_DYE:
                                applyDrugEffects(item, "Мефедрон", Map.of(
                                        "minecraft:speed", 100,
                                        "minecraft:strength", 100,
                                        "minecraft:nausea", 100,
                                        "minecraft:blindness", 100
                                ), 0, 0.0f, clickedBlock, event.getPlayer());
                                break;
                            case FERN:
                                applyDrugEffects(item, "Марихуана", Map.of(
                                        "minecraft:regeneration", 100,
                                        "minecraft:hunger", 100,
                                        "minecraft:slowness", 100,
                                        "minecraft:weakness", 100
                                ), 0, 0.0f, clickedBlock, event.getPlayer());
                                break;
                            case HONEY_BOTTLE:
                                applyDrugEffects(item, "Пиво", Map.of(
                                        "minecraft:nausea", 100,
                                        "minecraft:resistance", 100,
                                        "minecraft:mining_fatigue", 100,
                                        "minecraft:weakness", 100
                                ), 6, 1.0f, clickedBlock, event.getPlayer());
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND) {
                GameMode gameMode = event.getPlayer().getGameMode();
                if (!gameMode.equals(GameMode.CREATIVE) && !gameMode.equals(GameMode.SPECTATOR)) {
                    PlayerUtils.sendMessageToPlayer(event.getPlayer(), Component.text("дебил хули ты тычешь, совсем уже под солями объебан"), PlayerUtils.MessageType.CHAT);
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
                    PlayerUtils.sendMessageToPlayer(event.getPlayer(), Component.text(druggedPlayer.formatted(drugType)), PlayerUtils.MessageType.CHAT);
                    druggedPlayer = rawDruggedPlayer.formatted(drugType);
                    PlayerUtils.sendMessageToPlayer(event.getPlayer(), Component.text(druggedPlayer), PlayerUtils.MessageType.ACTION_BAR);
                }
            }
        });
    }
}