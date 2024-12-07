package ru.dushkinmir.absolutelyRandom.features.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import ru.dushkinmir.absolutelyRandom.utils.ui.ChatConfirmation
import ru.dushkinmir.absolutelyRandom.utils.ui.InventoryConfirmation
import java.util.*

class ConsentEvent(
    private val plugin: Plugin,
    private val chatConfirmation: ChatConfirmation = ChatConfirmation(plugin)
) : Listener {
    private val playerBlockMap: MutableMap<Player, Block> = HashMap()
    private val random = Random()

    companion object {
        private val CONSENT_TITLE = Component.text("Согласие на обработку данных", NamedTextColor.YELLOW)
    }

    private val inventoryConfirmation: InventoryConfirmation

    init {
        val infoLore = listOf(
            "Соглашаясь, вы добровольно передаете",
            "себя в вечное рабство этому серверу",
            "Отказ приведет к немедленному наказанию!",
            "Соглашаясь, вы теряете свою свободу",
            "и становитесь собственностью владельца сервера."
        )
        inventoryConfirmation = InventoryConfirmation(
            PlainTextComponentSerializer.plainText().serialize(CONSENT_TITLE),
            NamedTextColor.YELLOW,
            "Договор о согласии", NamedTextColor.GOLD,
            infoLore, NamedTextColor.DARK_PURPLE,
            "Принять", NamedTextColor.GREEN,
            "Отказаться", NamedTextColor.RED
        )
    }

    @EventHandler
    fun handlePlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val block = event.clickedBlock
        if (block != null && event.action.isRightClick && block.state is Container) {
            if (player.isSneaking) return
            playerBlockMap[player] = block
            if (random.nextBoolean()) {
                inventoryConfirmation.openConsentMenu(player)
            } else {
                chatConfirmation.showConfirmation(
                    player,
                    CONSENT_TITLE
                        .append(Component.text("\n"))
                        .append(
                            Component.text(
                                listOf(
                                    "Соглашаясь, вы добровольно передаете",
                                    "себя в вечное рабство этому серверу",
                                    "Отказ приведет к немедленному наказанию!",
                                    "Соглашаясь, вы теряете свою свободу",
                                    "и становитесь собственностью владельца сервера."
                                ).joinToString("\n")
                            ).color(NamedTextColor.DARK_PURPLE)
                        ),
                    onConfirm = {
                        val block = playerBlockMap.remove(player)
                        if (block != null) {
                            player.closeInventory()
                            object : BukkitRunnable() {
                                override fun run() {
                                    player.openInventory((block.state as Container).inventory)
                                }
                            }.runTaskLater(plugin, 1L)
                        }
                    },
                    onCancel = {
                        PlayerUtils.kickPlayer(
                            player,
                            Component.text("Китай партия вами не доволен! \uD83D\uDE21", NamedTextColor.RED)
                        )
                    }
                )
            }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun handleInventoryClick(event: InventoryClickEvent) {
        if (event.view.title() == CONSENT_TITLE) {
            val player = event.whoClicked as Player
            event.isCancelled = true
            val clickedItem = event.currentItem
            if (clickedItem == null || clickedItem.type == Material.AIR) return

            handleItemClick(player, clickedItem)
        }
    }

    private fun handleItemClick(player: Player, clickedItem: ItemStack) {
        if (clickedItem.type == Material.GREEN_WOOL) {
            val block = playerBlockMap.remove(player)
            if (block != null) {
                player.closeInventory()
                object : BukkitRunnable() {
                    override fun run() {
                        player.openInventory((block.state as Container).inventory)
                    }
                }.runTaskLater(plugin, 1L)
            }
        } else if (clickedItem.type == Material.RED_WOOL) {
            PlayerUtils.kickPlayer(
                player,
                Component.text("Китай партия вами не доволен! \uD83D\uDE21", NamedTextColor.RED)
            )
        }
    }
}