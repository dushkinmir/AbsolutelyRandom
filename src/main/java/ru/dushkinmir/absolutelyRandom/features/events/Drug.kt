package ru.dushkinmir.absolutelyRandom.features.events

import de.tr7zw.changeme.nbtapi.NBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class Drug(
    private val name: String,
    private val effects: Map<String, Int>,
    private val nutrition: Int,
    private val saturation: Float,
    private val particleColor: Color,
    private val sound: Sound
) {

    fun apply(item: ItemStack, clickedBlock: Block, player: Player) {
        spawnVisualEffects(clickedBlock, player.world)
        updateItemMeta(item)
        applyNBTData(item)
    }

    private fun spawnVisualEffects(block: Block, world: World) {
        world.spawnParticle(
            Particle.DUST,
            block.location,
            75, 1.0, 1.0, 1.0,
            Particle.DustOptions(particleColor, 1.7f)
        )
        world.playSound(block.location, sound, 1.0f, 1.0f)
    }

    private fun updateItemMeta(item: ItemStack) {
        val meta: ItemMeta? = item.itemMeta
        meta?.displayName(Component.text(name))
        item.itemMeta = meta
    }

    private fun applyNBTData(item: ItemStack) {
        NBT.modify(item) { nbt ->
            nbt.setString("drug", "вова хорошенький")
        }

        NBT.modifyComponents(item) { nbt ->
            val foodTag: ReadWriteNBT = nbt.getOrCreateCompound("minecraft:food")
            foodTag.setInteger("nutrition", nutrition)
            foodTag.setFloat("saturation", saturation)
            foodTag.setBoolean("can_always_eat", true)

            val effectsTag: ReadWriteNBTCompoundList = foodTag.getCompoundList("effects")
            effects.forEach { (effectId, duration) ->
                val effectTag = effectsTag.addCompound().getOrCreateCompound("effect")
                effectTag.setString("id", effectId)
                effectTag.setInteger("duration", duration)
            }
        }
    }
}
