package ru.dushkinmir.absolutelyRandom.randoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        if (random.nextBoolean()) {
            eschkere(loshara);
        } else {
            spookyScarySkeleton(loshara);
        }
    }

    public static void eschkere(Player target) {
        target.getWorld().strikeLightning(target.getLocation());
        target.getWorld().createExplosion(target, 2F, false, false);

        Component message = Component.text("ЕЩКЕРЕЕЕ!!! 1488", NamedTextColor.DARK_GREEN);

        PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.CHAT);
        PlayerUtils.sendMessageToPlayer(target, message, message);
        PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.ACTION_BAR);
    }

    private static void spookyScarySkeleton(Player target) {
        Location location = target.getLocation();
        Sound sound = sounds[random.nextInt(sounds.length)];

        target.getWorld().playSound(location, sound, 1.0f, 1.0f);
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 20);
    }
}
