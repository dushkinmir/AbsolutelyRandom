package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MessageRandom extends JavaPlugin {
    private static final List<String> MESSAGES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final int MAX_MESSAGE_COUNT = 3;
    private static final long TASK_INTERVAL_TICKS = 20 * 4;

    public static void triggerMessage(Plugin plugin) {
        List<Player> onlinePlayers = PlayerUtils.getOnlinePlayers();
        if (!onlinePlayers.isEmpty()) {
            reloadMessages();
            Player randomPlayer = PlayerUtils.getRandomPlayer(onlinePlayers);
            scheduleRandomMessagesTask(plugin, randomPlayer);
        }
    }

    private static void reloadMessages() {
        Plugin plugin = Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("AbsolutelyRandom"));
        FileConfiguration config = plugin.getConfig();
        List<String> configMessages = config.getStringList("random-messages");

        if (!configMessages.isEmpty()) {
            MESSAGES.clear();
            MESSAGES.addAll(configMessages);
        }
    }

    private static void scheduleRandomMessagesTask(Plugin plugin, Player player) {
        new MessageTask(player, RANDOM.nextBoolean()).runTaskTimer(plugin, 0L, TASK_INTERVAL_TICKS);
    }

    private static class MessageTask extends BukkitRunnable {
        private final Player player;
        private int messageCount;
        private final boolean isPlayerMessage;

        public MessageTask(Player player, boolean isPlayerMessage) {
            this.player = player;
            this.messageCount = 0;
            this.isPlayerMessage = isPlayerMessage;
        }

        @Override
        public void run() {
            if (messageCount < MAX_MESSAGE_COUNT) {
                showRandomMessageToPlayer();
                messageCount++;
            } else {
                reloadMessages();
                this.cancel();
            }
        }

        private void showRandomMessageToPlayer() {
            String randomMessage = MESSAGES.get(RANDOM.nextInt(MESSAGES.size()));
            if (isPlayerMessage) {
                sendPlayerMessage(player, randomMessage);
            } else {
                showTitleMessage(player, randomMessage);
            }
            MESSAGES.remove(randomMessage);
        }

        private void showTitleMessage(Player player, String message) {
            Component titleText = createTitleText();
            Component subTitleText = createSubTitleText(message);
            PlayerUtils.sendMessageToPlayer(player, titleText, PlayerUtils.MessageType.TITLE);
            PlayerUtils.sendMessageToPlayer(player, subTitleText, PlayerUtils.MessageType.SUBTITLE);
            PlayerUtils.sendMessageToPlayer(player, titleText, PlayerUtils.MessageType.ACTION_BAR);
        }

        private Component createTitleText() {
            return Component.text("вова лашок", NamedTextColor.GRAY, TextDecoration.OBFUSCATED);
        }

        private Component createSubTitleText(String message) {
            return Component.text(message, NamedTextColor.GOLD);
        }

        private void sendPlayerMessage(Player player, String message) {
            Component messageFromPlayer = Component.text(String.format("<%s> %s", player.getName(), message));
            PlayerUtils.sendMessageToAllPlayers(messageFromPlayer, PlayerUtils.MessageType.CHAT);
        }
    }
}