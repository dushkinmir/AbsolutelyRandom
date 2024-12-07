package ru.dushkinmir.absolutelyRandom.features.actions.actions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import java.util.*

class Prank : Action("prank") {
    private val random = Random()
    private val sounds = arrayOf(
        Sound.ENTITY_BLAZE_AMBIENT,
        Sound.ENTITY_GHAST_SCREAM,
        Sound.ENTITY_ENDERMAN_TELEPORT,
        Sound.ENTITY_WITHER_SPAWN,
        Sound.ENTITY_WITCH_AMBIENT
    )

    override fun execute(plugin: Plugin) {
        val loshara = PlayerUtils.getRandomPlayer()
        if (random.nextBoolean()) {
            eschkere(loshara)
        } else {
            spookyScarySkeleton(loshara)
        }
    }

    private fun eschkere(target: Player) {
        target.world.strikeLightning(target.location)
        target.world.createExplosion(target, 2f, false, false)

        val message = Component.text("ЕЩКЕРЕЕЕ!!! 1488", NamedTextColor.DARK_GREEN)

        PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.CHAT)
        PlayerUtils.sendMessageToPlayer(target, message, message)
        PlayerUtils.sendMessageToPlayer(target, message, PlayerUtils.MessageType.ACTION_BAR)
    }

    private fun spookyScarySkeleton(target: Player) {
        val location: Location = target.location
        val sound: Sound = sounds[random.nextInt(sounds.size)]

        target.world.playSound(location, sound, 1.0f, 1.0f)
        location.world.spawnParticle(Particle.EXPLOSION, location, 20)
    }
}