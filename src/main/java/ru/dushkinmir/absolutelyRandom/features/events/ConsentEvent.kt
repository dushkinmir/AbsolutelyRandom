package ru.dushkinmir.absolutelyRandom.features.events

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import ru.dushkinmir.absolutelyRandom.utils.ui.ChatConfirmation
import ru.dushkinmir.absolutelyRandom.utils.ui.InventoryConfirmation
import java.util.*

class ConsentEvent(
    private val plugin: Plugin,
    private val chatConfirmation: ChatConfirmation = ChatConfirmation(plugin),
    private val inventoryConfirmation: InventoryConfirmation = InventoryConfirmation()
) : Listener {
    private val playerBlockMap: MutableMap<Player, Block> = HashMap()
    private val random = Random()

    private companion object {
        private val CONSENT_TITLE = Component.text("Согласие на обработку данных").color(NamedTextColor.YELLOW)
        private val infoLore: List<String> = listOf(
            "Соглашаясь, вы добровольно передаете",
            "себя в вечное рабство этому серверу",
            "Отказ приведет к немедленному наказанию!",
            "Соглашаясь, вы теряете свою свободу",
            "и становитесь собственностью владельца сервера."
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
                inventoryConfirmation.showConfirmation(
                    CONSENT_TITLE,
                    player,
                    Component.text("Договор о согласии").color(NamedTextColor.GOLD),
                    infoLore.map { it -> Component.text(it).color(NamedTextColor.DARK_PURPLE) },
                    Component.text("Принять").color(NamedTextColor.GREEN),
                    Component.text("Отказаться").color(NamedTextColor.RED),
                    { onConfirm(player) }
                ) { onCancel(player) }
            } else {
                chatConfirmation.showConfirmation(
                    player,
                    CONSENT_TITLE
                        .append(Component.text("\n"))
                        .append(
                            Component.text(
                                infoLore.joinToString("\n")
                            ).color(NamedTextColor.DARK_PURPLE)
                        ),
                    { onConfirm(player) },
                    { onCancel(player) }
                )
            }
            event.isCancelled = true
        }
    }

    private fun onConfirm(player: Player) {
        val block = playerBlockMap.remove(player)
        if (block != null) {
            player.closeInventory()
            object : BukkitRunnable() {
                override fun run() {
                    player.openInventory((block.state as Container).inventory)
                }
            }.runTaskLater(plugin, 1L)
        }
    }

    private fun onCancel(player: Player) {
        PlayerUtils.kickPlayer(
            player,
            Component.text("Китай партия вами не доволен! 😡", NamedTextColor.RED)
        )
    }
}