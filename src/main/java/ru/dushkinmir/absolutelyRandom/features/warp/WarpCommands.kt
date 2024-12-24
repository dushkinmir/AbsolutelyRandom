package ru.dushkinmir.absolutelyRandom.features.warp

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
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
        this.inventoryConfirmation = InventoryConfirmation()
    }

    fun registerWarpCommands() {
        commandTree("warp") {
            subcommand("create") {
                stringArgument("warp") {
                    playerExecutor { player, args ->
                        val warpName = args["warpName"] as String
                        val location: Location = player.location
                        warpManager.createWarp(player, warpName, location)
                    }
                }
            }
            subcommand("tp") {
                stringArgument("warpName") {
                    replaceSuggestions { info, builder ->
                        val player = info.sender() as Player
                        warpManager.getWarps(player, false).toTypedArray()
                        builder.buildFuture()
                    }
                    playerExecutor { player, args ->
                        val warpName = args["warpName"] as String
                        warpManager.teleportToWarp(player, warpName)
                    }
                }
            }
            subcommand("del") {
                stringArgument("warpName") {
                    replaceSuggestions { info, builder ->
                        val player = info.sender() as Player
                        warpManager.getWarps(player, false).toTypedArray()
                        builder.buildFuture()
                    }
                    playerExecutor { player, args ->
                        val warpName = args["warpName"] as String
                        warpManager.deleteWarp(player, warpName)
                    }
                }
            }
            subcommand("delall") {
                playerExecutor { player, _ ->
                    PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.RED),
                        PlayerUtils.MessageType.CHAT
                    )
                    inventoryConfirmation.showConfirmation(
                        Component.text("Подтверждение").color(NamedTextColor.YELLOW),
                        player,
                        Component.text("Внимание: валюта не будет возвращена!").color(NamedTextColor.GOLD),
                        listOf("Подтвердите удаление всех варпов.").map { it ->
                            Component.text(it).color(NamedTextColor.DARK_PURPLE)
                        },
                        Component.text("Подтвердить").color(NamedTextColor.GREEN),
                        Component.text("Отменить").color(NamedTextColor.RED),
                        { onConfirm(player) }
                    ) { onCancel(player) }
                }
            }
            subcommand("list") {
                playerExecutor { player, _ ->
                    val warps = warpManager.getWarps(player, true)

                    if (warps.isEmpty()) {
                        PlayerUtils.sendMessageToPlayer(
                            player,
                            Component.text("У вас нет созданных варпов.").color(NamedTextColor.RED),
                            PlayerUtils.MessageType.CHAT
                        )
                    } else {
                        val formattedWarps = warps.joinToString("\n ")
                        PlayerUtils.sendMessageToPlayer(
                            player,
                            Component.text("Ваши варпы: \n $formattedWarps").color(NamedTextColor.GREEN),
                            PlayerUtils.MessageType.CHAT
                        )
                    }
                }
            }
        }
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