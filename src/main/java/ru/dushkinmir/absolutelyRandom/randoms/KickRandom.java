package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KickRandom {

    private static final Random RANDOM = new Random();
    private static final String KICK_MESSAGE = "хахаха лошара";
    private static final NamedTextColor KICK_MESSAGE_COLOR = NamedTextColor.RED;

    public static void triggerKick() {
        List<Player> onlinePlayers = getOnlinePlayers();
        if (!onlinePlayers.isEmpty()) {
            kickRandomPlayer(onlinePlayers);
        }
    }

    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static void kickRandomPlayer(List<Player> players) {
        Player randomPlayer = pickRandomPlayer(players);
        kickPlayer(randomPlayer);
    }

    private static Player pickRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void kickPlayer(Player player) {
        player.kick(Component.text(KICK_MESSAGE, KICK_MESSAGE_COLOR));
    }
}