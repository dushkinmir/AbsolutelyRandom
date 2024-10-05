package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.List;

public class EschkereRandom implements Listener {
    public static void triggerEschkere() {
        List<Player> players = PlayerUtils.getOnlinePlayers();
        Player target = PlayerUtils.getRandomPlayer(players);

        if (target != null) {
            target.getWorld().strikeLightning(target.getLocation());
            target.getWorld().createExplosion(target.getLocation(), 4F, true, false);

            Component message = Component.text("ЕЩКЕРЕЕЕ!!! 1488");
            PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.CHAT);
            PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.TITLE);
            PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.SUBTITLE);
            PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.ACTION_BAR);
        }
    }
}