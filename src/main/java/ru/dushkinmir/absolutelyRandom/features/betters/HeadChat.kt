package ru.dushkinmir.absolutelyRandom.features.betters

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable

class HeadChat(private val plugin: Plugin) : Listener {
    init {
        CraftingRecipe(plugin)
    }

    private fun decreaseRadioDurability(player: Player) {
        val radio = player.inventory.itemInMainHand
        val meta = radio.itemMeta
        if (radio.hasItemMeta()) {
            if (meta != null) {
                val durabilityKey = NamespacedKey(plugin, "durability")
                val durability = meta.persistentDataContainer.get(durabilityKey, PersistentDataType.INTEGER)
                if (durability != null && durability > 1) {
                    val newDurability = durability - 1
                    meta.persistentDataContainer.set(durabilityKey, PersistentDataType.INTEGER, newDurability)
                    val updatedName = Component.text()
                        .append(RADIO_NAME)
                        .append(Component.text("[$newDurability]", NamedTextColor.DARK_AQUA))
                        .build()

                    meta.displayName(updatedName)
                    radio.itemMeta = meta
                } else if (durability != null) {
                    player.inventory.setItemInMainHand(null)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val message = LegacyComponentSerializer.legacySection().serialize(event.message())

        if (isInvalidMessage(event, message)) {
            return
        }

        if (playerHasRadio(player)) {
            decreaseRadioDurability(player)
            return
        }

        event.isCancelled = true

        object : BukkitRunnable() {
            override fun run() {
                val armorStand = createTextDisplay(player, message)
                if (message.length > 20) {
                    MessageScroller(plugin).scrollMessageAboveHead(armorStand, message)
                } else {
                    var delay = 80L
                    removeTextDisplayLater(armorStand, delay)
                }
                TextDisplayFollower(plugin).followPlayer(player, armorStand, 1L)
            }
        }.runTask(plugin)
    }

    private fun isInvalidMessage(event: AsyncChatEvent, message: String): Boolean {
        return message.startsWith("/") || event.isCancelled
    }

    private fun playerHasRadio(player: Player): Boolean {
        val heldItem = player.inventory.itemInMainHand
        return heldItem.hasItemMeta() &&
                heldItem.itemMeta!!.hasDisplayName() &&
                heldItem.itemMeta!!.displayName().toString().contains(RADIO_NAME.toString())
    }

    private fun createTextDisplay(player: Player, message: String): TextDisplay {
        val location = player.location.add(0.0, 2.25, 0.0)
        val textDisplay = player.world.spawnEntity(location, EntityType.TEXT_DISPLAY) as TextDisplay
        textDisplay.isInvisible = true
        textDisplay.customName(Component.text(message))
        textDisplay.billboard = Billboard.CENTER
        textDisplay.isCustomNameVisible = true
        textDisplay.setGravity(false)
        return textDisplay
    }

    private fun removeTextDisplayLater(textDisplay: TextDisplay, delay: Long) {
        object : BukkitRunnable() {
            override fun run() {
                textDisplay.remove()
            }
        }.runTaskLater(plugin, delay)
    }

    private data class TextDisplayFollower(val plugin: Plugin) {
        fun followPlayer(player: Player, textDisplay: TextDisplay, interval: Long) {
            object : BukkitRunnable() {
                override fun run() {
                    if (!textDisplay.isValid || !player.isOnline) {
                        textDisplay.remove()
                        this.cancel()
                        return
                    }
                    val location = player.location.add(0.0, 2.25, 0.0)
                    textDisplay.teleport(location)
                }
            }.runTaskTimer(plugin, 0L, interval)
        }
    }

    private data class MessageScroller(val plugin: Plugin) {
        fun scrollMessageAboveHead(textDisplay: TextDisplay, message: String) {
            val messageLength = message.length
            val visibleLength = 30
            if (messageLength <= visibleLength) {
                textDisplay.customName(Component.text(message))
                object : BukkitRunnable() {
                    override fun run() {
                        textDisplay.remove()
                    }
                }.runTaskLater(plugin, 80L)
            } else {
                object : BukkitRunnable() {
                    var offset = 0

                    override fun run() {
                        if (offset >= messageLength) {
                            this.cancel()
                            object : BukkitRunnable() {
                                override fun run() {
                                    textDisplay.remove()
                                }
                            }.runTaskLater(plugin, 20L)
                        } else {
                            val endIndex = offset + visibleLength
                            var visibleText = message.substring(offset, endIndex)

                            if (endIndex < messageLength) {
                                visibleText += "..."
                            }

                            textDisplay.customName(Component.text(visibleText))
                            offset++
                        }
                    }
                }.runTaskTimer(plugin, 0L, 7L)
            }
        }
    }

    companion object {
        private val RADIO_NAME: TextComponent = Component.text("Radio", NamedTextColor.GOLD)

        fun onDisable(plugin: Plugin) {
            plugin.server.removeRecipe(NamespacedKey(plugin, "radio"))
            plugin.server.removeRecipe(NamespacedKey(plugin, "radioRepaired"))
        }
    }

    class CraftingRecipe(private val plugin: Plugin) {
        init {
            createRadioRecipe()
            createRadioRepairRecipe()
        }

        private fun createRadioRecipe() {
            val radio = ItemStack(Material.NOTE_BLOCK)
            val meta = radio.itemMeta
            meta!!.displayName(Component.text("Radio", NamedTextColor.GOLD))

            val durabilityKey = NamespacedKey(plugin, "durability")
            meta.persistentDataContainer.set(durabilityKey, PersistentDataType.INTEGER, 30)

            radio.itemMeta = meta

            val key = NamespacedKey(plugin, "radio")
            val recipe = ShapedRecipe(key, radio)
            recipe.shape("III", "IRI", "III")
            recipe.setIngredient('I', Material.IRON_INGOT)
            recipe.setIngredient('R', Material.REDSTONE)

            Bukkit.addRecipe(recipe)
        }

        private fun createRadioRepairRecipe() {
            val brokenRadio = ItemStack(Material.NOTE_BLOCK)
            val meta = brokenRadio.itemMeta
            meta!!.displayName(Component.text("Radio", NamedTextColor.GOLD))

            val durabilityKey = NamespacedKey(plugin, "durability")
            meta.persistentDataContainer.set(durabilityKey, PersistentDataType.INTEGER, 0)

            brokenRadio.itemMeta = meta

            val fixedRadio = ItemStack(Material.NOTE_BLOCK)
            val fixedMeta = fixedRadio.itemMeta
            fixedMeta!!.displayName(Component.text("Radio", NamedTextColor.GOLD))

            fixedMeta.persistentDataContainer.set(durabilityKey, PersistentDataType.INTEGER, 30)

            fixedRadio.itemMeta = fixedMeta

            val key = NamespacedKey(plugin, "radioRepaired")
            val recipe = ShapelessRecipe(key, fixedRadio)

            recipe.addIngredient(Material.IRON_INGOT)
            recipe.addIngredient(Material.IRON_INGOT)
            recipe.addIngredient(brokenRadio.type)

            Bukkit.addRecipe(recipe)
        }
    }
}