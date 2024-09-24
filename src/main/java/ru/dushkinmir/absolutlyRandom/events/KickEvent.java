package ru.dushkinmir.absolutlyRandom.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KickEvent {

    private static final Random RANDOM = new Random();
    private static final String KICK_MESSAGE = "хахаха лошара";
    private static final NamedTextColor KICK_MESSAGE_COLOR = NamedTextColor.RED;

    public static void triggerKickEvent() {
        List<Player> players = getOnlinePlayers();
        if (!players.isEmpty()) {
            Player randomPlayer = getRandomPlayer(players);
            kickPlayer(randomPlayer);
        }
    }

    private static List<Player> getOnlinePlayers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
    }

    private static Player getRandomPlayer(List<Player> players) {
        return players.get(RANDOM.nextInt(players.size()));
    }

    private static void kickPlayer(Player player) {
        player.kick(Component.text(KICK_MESSAGE, KICK_MESSAGE_COLOR));
    }
}