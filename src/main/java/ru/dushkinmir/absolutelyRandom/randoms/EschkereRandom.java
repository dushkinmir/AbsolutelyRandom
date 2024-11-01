package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
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
            target.getWorld().createExplosion(target, 2F, false, false);

            Component message = Component.text("ЕЩКЕРЕЕЕ!!! 1488", NamedTextColor.DARK_GREEN);
            PlayerUtils.sendMessageToAllPlayers(message, PlayerUtils.MessageType.CHAT);
            target.showTitle(Title.title(message, message));
            PlayerUtils.sendMessageToAllPlayers(message, PlayerUtils.MessageType.ACTION_BAR);
        }
    }
}