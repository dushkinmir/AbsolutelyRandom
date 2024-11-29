package ru.dushkinmir.absolutelyRandom.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomMessage extends JavaPlugin {
    private static final Random RANDOM = new Random();
    private static final int MAX_MESSAGE_COUNT = 3;
    private static final long TASK_INTERVAL_TICKS = 20 * 4;

    public static void triggerMessage(Plugin plugin, Set<String> MESSAGES) {
        List<Player> onlinePlayers = PlayerUtils.getOnlinePlayers();
        Player randomPlayer = PlayerUtils.getRandomPlayer(onlinePlayers);
        scheduleRandomMessagesTask(plugin, randomPlayer, MESSAGES);
    }

    private static void scheduleRandomMessagesTask(Plugin plugin, Player player, Set<String> MESSAGES) {
        new MessageTask(player, RANDOM.nextBoolean(), MESSAGES).runTaskTimer(plugin, 0L, TASK_INTERVAL_TICKS);
    }

    private static class MessageTask extends BukkitRunnable {
        private final Player player;
        private int messageCount;
        private final boolean isPlayerMessage;
        private final Set<String> MESSAGES;

        public MessageTask(Player player, boolean isPlayerMessage, Set<String> MESSAGES) {
            this.player = player;
            this.messageCount = 0;
            this.isPlayerMessage = isPlayerMessage;
            this.MESSAGES = MESSAGES;
        }

        @Override
        public void run() {
            if (messageCount < MAX_MESSAGE_COUNT) {
                showRandomMessageToPlayer();
                messageCount++;
            } else {
                this.cancel();
            }
        }

        private void showRandomMessageToPlayer() {
            List<String> messageList = new ArrayList<>(MESSAGES);
            String randomMessage = messageList.get(RANDOM.nextInt(MESSAGES.size()));
            if (isPlayerMessage) {
                sendPlayerMessage(player, randomMessage);
            } else {
                showTitleMessage(player, randomMessage);
            }
            MESSAGES.remove(randomMessage);
        }

        private Component createTitleText() {
            return Component.text("пениспиздасиськи", NamedTextColor.GRAY, TextDecoration.OBFUSCATED);
        }

        private Component createSubTitleText(String message) {
            return Component.text(message, NamedTextColor.GOLD);
        }

        private void showTitleMessage(Player player, String message) {
            Component titleText = createTitleText();
            Component subTitleText = createSubTitleText(message);
            player.showTitle(Title.title(titleText, subTitleText));
            PlayerUtils.sendMessageToPlayer(player, titleText, PlayerUtils.MessageType.ACTION_BAR);
        }

        private void sendPlayerMessage(Player player, String message) {
            Component messageFromPlayer = Component.text(String.format("<%s> %s", player.getName(), message));
            PlayerUtils.sendMessageToAllPlayers(messageFromPlayer, PlayerUtils.MessageType.CHAT);
        }
    }
}