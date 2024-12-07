package ru.dushkinmir.absolutelyRandom.features.warp

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import ru.dushkinmir.absolutelyRandom.utils.ui.ConsentMenu

class WarpCommands(private val warpManager: WarpManager, plugin: Plugin) : Listener {

    private val consentMenu: ConsentMenu

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, plugin)
        this.consentMenu = ConsentMenu(
            "Подтверждение", NamedTextColor.YELLOW,
            "Внимание: валюта не будет возвращена!", NamedTextColor.GOLD,
            listOf("Подтвердите удаление всех варпов."), NamedTextColor.DARK_PURPLE,
            "Подтвердить", NamedTextColor.GREEN,
            "Отменить", NamedTextColor.RED
        )
    }

    fun registerWarpCommands() {
        // Подкоманда "create"
        val warpCreate = CommandAPICommand("create")
            .withArguments(StringArgument("warpName"))
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val warpName = args["warpName"] as String
                val location: Location = player.location
                warpManager.createWarp(player, warpName, location)
            })

        // Подкоманда "teleport"
        val warpTeleport = CommandAPICommand("tp")
            .withArguments(
                StringArgument("warpName")
                    .replaceSuggestions(ArgumentSuggestions.strings { info ->
                        val player = info.sender() as Player
                        warpManager.getWarps(player, false).toTypedArray()
                    })
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val warpName = args["warpName"] as String
                warpManager.teleportToWarp(player, warpName)
            })

        // Подкоманда "delete"
        val warpDelete = CommandAPICommand("del")
            .withArguments(
                StringArgument("warpName")
                    .replaceSuggestions(ArgumentSuggestions.strings { info ->
                        val player = info.sender() as Player
                        warpManager.getWarps(player, false).toTypedArray()
                    })
            )
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val warpName = args["warpName"] as String
                warpManager.deleteWarp(player, warpName)
            })

// Подкоманда "deleteall"
        val warpDeleteAll = CommandAPICommand("delall")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.RED),
                    PlayerUtils.MessageType.CHAT
                )
                consentMenu.openConsentMenu(player)
            })

        val warpList = CommandAPICommand("list")
            .executesPlayer(PlayerCommandExecutor { player, args ->
                val warps = warpManager.getWarps(player, true)

                if (warps.isEmpty()) {
                    PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text("У вас нет созданных варпов.")
                            .color(NamedTextColor.RED),
                        PlayerUtils.MessageType.CHAT
                    )
                } else {
                    val formattedWarps = warps.joinToString("\n ")
                    PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text("Ваши варпы: \n $formattedWarps")
                            .color(NamedTextColor.GREEN),
                        PlayerUtils.MessageType.CHAT
                    )
                }
            })

        // Основная команда "warp" с подкомандами
        CommandAPICommand("warp")
            .withSubcommand(warpCreate)
            .withSubcommand(warpTeleport)
            .withSubcommand(warpDelete)
            .withSubcommand(warpDeleteAll)
            .withSubcommand(warpList)
            .register()
    }

    // Обработка кликов в меню подтверждения удаления всех варпов
    @EventHandler
    fun handleInventoryClick(event: InventoryClickEvent) {
        if (event.view.title() == Component.text(
                "Подтверждение",
                NamedTextColor.YELLOW
            )
        ) {
            val player = event.whoClicked as Player
            event.isCancelled = true
            val clickedItem = event.currentItem
            if (clickedItem == null || clickedItem.type == Material.AIR) return

            handleItemClick(player, clickedItem)
        }
    }

    private fun handleItemClick(player: Player, clickedItem: ItemStack) {
        // Обработка нажатия на кнопки
        when (clickedItem.type) {
            Material.GREEN_WOOL -> {
                warpManager.deleteAllWarps(player)
                player.closeInventory()
            }

            Material.RED_WOOL -> {
                player.closeInventory()
                PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("Удаление всех варпов отменено.").color(NamedTextColor.GREEN),
                    PlayerUtils.MessageType.CHAT
                )
            }

            else -> {}
        }
    }
}