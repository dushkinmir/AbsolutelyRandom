package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

public class KickRandom {

    private static final Component PLAYER_KICK_MESSAGE = Component.text("хахаха лошара", NamedTextColor.RED);
    private static final String BROADCAST_KICK_MESSAGE_TEMPLATE = "вот же %s лох!";
    private static final NamedTextColor BROADCAST_KICK_MESSAGE_COLOR = NamedTextColor.YELLOW;

    public static void triggerKick() {
        if (!PlayerUtils.getOnlinePlayers().isEmpty()) {
            kickPlayer(PlayerUtils.getRandomPlayer(PlayerUtils.getOnlinePlayers()));
        }
    }

    private static void kickPlayer(Player player) {
        Component broadcastMessage = generateKickMessage(player.getName());
        PlayerUtils.sendMessageToAllPlayers(broadcastMessage, false);
        PlayerUtils.kickPlayer(player, PLAYER_KICK_MESSAGE);
    }

    private static Component generateKickMessage(String playerName) {
        return Component.text(String.format(BROADCAST_KICK_MESSAGE_TEMPLATE, playerName), BROADCAST_KICK_MESSAGE_COLOR);
    }
}