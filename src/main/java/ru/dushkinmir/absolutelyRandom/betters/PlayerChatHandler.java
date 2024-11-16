package ru.dushkinmir.absolutelyRandom.betters;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerChatHandler implements Listener {
    private final Plugin plugin;
    private static final TextComponent RADIO_NAME = Component.text("Радио", NamedTextColor.GOLD);

    public PlayerChatHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private void decreaseRadioDurability(Player player) {
        ItemStack radio = player.getInventory().getItemInMainHand();
        if (radio.getItemMeta() instanceof Damageable damageable) {
            if (radio.getType().getMaxDurability() - damageable.getDamage() > 1) {
                damageable.setDamage(damageable.getDamage() + 1);
                radio.setItemMeta(damageable);
            } else {
                player.getInventory().setItemInMainHand(null); // Remove item if it's out of durability
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());

        if (isInvalidMessage(event, message)) {
            return;
        }

        if (playerHasRadio(player)) {
            decreaseRadioDurability(player);
        }

        event.setCancelled(true);

        // Переносим создание ArmorStand в основной поток
        new BukkitRunnable() {
            @Override
            public void run() {
                ArmorStand armorStand = createArmorStand(player, message);
                if (message.length() > 30) {
                    new MessageScroller(plugin).scrollMessageAboveHead(armorStand, message);
                } else {
                    removeArmorStandLater(armorStand, 80L);
                }
                new ArmorStandFollower(plugin).followPlayer(player, armorStand, 1L);
            }
        }.runTask(plugin);
    }

    private boolean isInvalidMessage(AsyncChatEvent event, String message) {
        return message.startsWith("/") || event.isCancelled();
    }

    private boolean playerHasRadio(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        return heldItem.hasItemMeta()
                && heldItem.getItemMeta().hasDisplayName()
                && RADIO_NAME.equals(heldItem.getItemMeta().displayName());
    }

    private ArmorStand createArmorStand(Player player, String message) {
        Location location = player.getLocation().add(0, 2.25, 0);
        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setInvisible(true);
        armorStand.customName(Component.text(message));
        armorStand.setCustomNameVisible(true);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        return armorStand;
    }

    private void removeArmorStandLater(ArmorStand armorStand, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.remove();
            }
        }.runTaskLater(plugin, delay);
    }

    private record ArmorStandFollower(Plugin plugin) {
        void followPlayer(Player player, ArmorStand armorStand, long interval) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!armorStand.isValid() || !player.isOnline()) {
                        armorStand.remove();
                        this.cancel();
                        return;
                    }
                    Location location = player.getLocation().add(0, 2.25, 0);
                    armorStand.teleport(location);
                }
            }.runTaskTimer(plugin, 0L, interval);
        }
    }

    private record MessageScroller(Plugin plugin) {
        void scrollMessageAboveHead(ArmorStand armorStand, String message) {
            int messageLength = message.length();
            new BukkitRunnable() {
                int offset = 0;

                @Override
                public void run() {
                    if (offset + 64 >= messageLength) {
                        this.cancel();
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                armorStand.remove();
                            }
                        }.runTaskLater(plugin, 20L);
                    } else {
                        armorStand.customName(Component.text(message.substring(offset, Math.min(offset + 64, messageLength))));
                        offset++;
                    }
                }
            }.runTaskTimer(plugin, 0L, 7L);
        }
    }
}