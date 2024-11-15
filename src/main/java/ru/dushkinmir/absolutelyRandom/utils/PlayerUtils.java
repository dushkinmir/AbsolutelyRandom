package ru.dushkinmir.absolutelyRandom.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerUtils {

    private static final Random RANDOM = new Random();

    public static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    public static Player getRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    public static void sendMessageToPlayer(Player player, Component message, MessageType type) {
        switch (type) {
            case ACTION_BAR:
                player.sendActionBar(message);
                break;
            case CHAT:
                player.sendMessage(message);
                break;
            default:
                throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }

    public static void sendMessageToPlayer(Player player, Component message, Component message1) {
        player.showTitle(Title.title(message, message1));
    }

    public static void sendMessageToAllPlayers(Component message, MessageType type) {
        for (Player player : getOnlinePlayers()) {
            sendMessageToPlayer(player, message, type);
        }
    }

    public enum MessageType {
        ACTION_BAR,
        CHAT
    }

    public static void kickPlayer(Player player, Component reason) {
        player.kick(reason);
    }

    public static void kickAllPlayers(Component reason) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(reason);
        }
    }
}