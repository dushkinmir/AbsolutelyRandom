package ru.dushkinmir.absolutelyRandom.features.warp

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils
import ru.dushkinmir.absolutelyRandom.utils.ui.InventoryConfirmation

class WarpCommands(private val warpManager: WarpManager, plugin: Plugin) : Listener {

    private val inventoryConfirmation: InventoryConfirmation

    init {
        Bukkit.getServer().pluginManager.registerEvents(this, plugin)
        this.inventoryConfirmation = InventoryConfirmation(
            Component.text("Подтверждение").color(NamedTextColor.YELLOW)
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
                inventoryConfirmation.showConfirmation(
                    player,
                    Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.GOLD),
                    listOf("Подтвердите удаление всех варпов.").map { it ->
                        Component.text(it).color(NamedTextColor.DARK_PURPLE)
                    },
                    Component.text("Подтвердить").color(NamedTextColor.GREEN),
                    Component.text("Отменить").color(NamedTextColor.RED),
                    { onConfirm(player) },
                    { onCancel(player) }
                )
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

    private fun onConfirm(player: Player) {
        warpManager.deleteAllWarps(player)
        player.closeInventory()
    }

    private fun onCancel(player: Player) {
        player.closeInventory()
        PlayerUtils.sendMessageToPlayer(
            player,
            Component.text("Удаление всех варпов отменено.").color(NamedTextColor.GREEN),
            PlayerUtils.MessageType.CHAT
        )
    }
}