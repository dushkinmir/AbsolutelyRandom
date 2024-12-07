package ru.dushkinmir.absolutelyRandom.utils.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class ChatConfirmation(
    private val plugin: Plugin
) {
    // Показать окно подтверждения
    fun showConfirmation(player: Player, message: Component, onConfirm: () -> Unit, onCancel: () -> Unit) {
        // Создаём сообщение с кнопками
        val confirmationMessage = Component.text()
            .append(message)
            .append(Component.text("\n"))
            .append(
                Component.text("§r[§aДа§r]")
                    .clickEvent(ClickEvent.runCommand("/confirm_yes_${player.uniqueId}"))
            )
            .append(Component.text(" §7или "))
            .append(
                Component.text("§r[§cНет§r]")
                    .clickEvent(ClickEvent.runCommand("/confirm_no_${player.uniqueId}"))
            )
            .build()

        // Отправляем сообщение игроку
        player.sendMessage(confirmationMessage)

        // Регистрируем временные команды
        registerTemporaryCommands(player, onConfirm, onCancel)
    }

    // Регистрация временных команд
    private fun registerTemporaryCommands(player: Player, onConfirm: () -> Unit, onCancel: () -> Unit) {
        val yesCommand = "confirm_yes_${player.uniqueId}"
        val noCommand = "confirm_no_${player.uniqueId}"

        // Добавляем временные команды в командный процессор
        plugin.server.commandMap.register(plugin.name, object : org.bukkit.command.Command(yesCommand) {
            override fun execute(
                sender: org.bukkit.command.CommandSender,
                commandLabel: String,
                args: Array<out String>
            ): Boolean {
                if (sender is Player && sender.uniqueId == player.uniqueId) {
                    onConfirm()
                    unregisterCommands(yesCommand, noCommand)
                    return true
                }
                return false
            }
        })

        plugin.server.commandMap.register(plugin.name, object : org.bukkit.command.Command(noCommand) {
            override fun execute(
                sender: org.bukkit.command.CommandSender,
                commandLabel: String,
                args: Array<out String>
            ): Boolean {
                if (sender is Player && sender.uniqueId == player.uniqueId) {
                    onCancel()
                    unregisterCommands(yesCommand, noCommand)
                    return true
                }
                return false
            }
        })
    }

    // Удаление временных команд
    private fun unregisterCommands(vararg commands: String) {
        commands.forEach { command ->
            plugin.server.commandMap.knownCommands.remove(command)
        }
    }
}
