package ru.dushkinmir.absolutelyRandom.betters;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerChatHandler implements Listener {
    private final Plugin plugin;
    private static final TextComponent RADIO_NAME = Component.text("Radio", NamedTextColor.GOLD);

    public PlayerChatHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());

        // Игнорировать сообщения от плагинов, сервера и команды
        if (message.startsWith("/") || event.isCancelled()) {
            return;
        }

        // Проверка есть ли у игрока радио
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.hasItemMeta() && heldItem.getItemMeta().hasDisplayName()
                && RADIO_NAME.equals(heldItem.getItemMeta().displayName())) {
            return;
        }

        // Отменить отправку сообщения в чат
        event.setCancelled(true);

        // Отобразить сообщение над головой игрока
        sendActionBar(player, message);

        // Прокручивание длинных сообщений
        if (message.length() > 64) {
            scrollLongMessage(player, message);
        }
    }

    // Метод для отображения сообщения над головой игрока
    private void sendActionBar(Player player, String message) {
        player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    // Метод для прокрутки длинных сообщений
    private void scrollLongMessage(Player player, String message) {
        int messageLength = message.length();
        new BukkitRunnable() {
            int offset = 0;

            @Override
            public void run() {
                if (offset + 64 >= messageLength) {
                    this.cancel();
                } else {
                    sendActionBar(player, message.substring(offset, offset + 64));
                    offset++;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 1 сообщение в секунду
    }
}