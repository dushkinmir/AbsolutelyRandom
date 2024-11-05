package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.Random;

public class PrankRandom {
    private static final Random random = new Random();
    private static final Sound[] sounds = {
            Sound.ENTITY_BLAZE_AMBIENT,
            Sound.ENTITY_GHAST_SCREAM,
            Sound.ENTITY_ENDERMAN_TELEPORT,
            Sound.ENTITY_WITHER_SPAWN,
            Sound.ENTITY_WITCH_AMBIENT
    };

    public static void triggerPrank() {
        Player loshara = PlayerUtils.getRandomPlayer(PlayerUtils.getOnlinePlayers());
        int prankType = random.nextInt(3);
        switch (prankType) {
            case 0:
                eschkere(loshara);
                break;
            case 1:
                playRandomSound(loshara);
                spawnParticles(loshara.getLocation());
                break;
            case 2:
                sendRickrollMessage();
        }
    }

    public static void eschkere(Player target) {
        target.getWorld().strikeLightning(target.getLocation());
        target.getWorld().createExplosion(target, 2F, false, false);

        Component message = Component.text("ЕЩКЕРЕЕЕ!!! 1488", NamedTextColor.DARK_GREEN);
        PlayerUtils.sendMessageToAllPlayers(message, PlayerUtils.MessageType.CHAT);
        target.showTitle(Title.title(message, message));
        PlayerUtils.sendMessageToAllPlayers(message, PlayerUtils.MessageType.ACTION_BAR);
    }

    private static void playRandomSound(Player target) {
        Location location = target.getLocation();
        Sound sound = sounds[random.nextInt(sounds.length)];

        target.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    private static void spawnParticles(Location location) {
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 20);
    }

    private static void sendRickrollMessage() {
        Component rickrollMessage = Component.text("RICKROLL!!!", NamedTextColor.GOLD);
        PlayerUtils.sendMessageToAllPlayers(rickrollMessage, PlayerUtils.MessageType.CHAT);
    }
}
