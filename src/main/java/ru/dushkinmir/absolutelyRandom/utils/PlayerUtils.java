package ru.dushkinmir.absolutelyRandom.utils;

import net.kyori.adventure.text.Component;
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

    public static void sendMessageToPlayer(Player player, Component message, boolean isActionBar) {
        if (isActionBar) {
            player.sendActionBar(message);
        } else {
            player.sendMessage(message);
        }
    }

    public static void sendMessageToAllPlayers(Component message, boolean isActionBarMessage) {
        for (Player player : getOnlinePlayers()) {
            sendMessageToPlayer(player, message, isActionBarMessage);
        }
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