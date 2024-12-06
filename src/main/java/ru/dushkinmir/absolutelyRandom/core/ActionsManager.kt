package ru.dushkinmir.absolutelyRandom.core

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.AbsRand
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.features.actions.types.*
import java.util.*


class ActionsManager(private val plugin: Plugin) {
    private val actions: MutableMap<String, Action> = HashMap()
    private val actionsChances: MutableMap<String, Int> = HashMap()
    private val debugActions: MutableMap<String, Runnable> = HashMap()

    // Автоматическая регистрация всех Action классов
    fun onEnable() {

        registerAction(Crash())
        registerAction(Group())
        registerAction(Kick())
        registerAction(Prank())
        registerAction(RandomMessage())
        registerAction(Stinky())
        registerAction(Storm())

        actions.forEach { actionName, _ ->
            {
                actionsChances[actionName] = plugin.config.getInt("chances.%s-chance".format(actionName), -1)
                debugActions[actionName] = Runnable { executeAction(actionName) }
            }
        }

        actionScheduler()
        registerCommands()
    }

    fun registerAction(action: Action) {
        actions[action.name] = action
        plugin.logger.info("Action registered: ${action.javaClass.simpleName}")
    }

    private fun actionScheduler() {
        // Schedule task to trigger random events at intervals
        object : BukkitRunnable() {
            override fun run() {
                // Get list of online players
                val players: List<Player> = ArrayList(plugin.server.onlinePlayers)
                if (players.isEmpty()) return // Return if no players are online

                actions.forEach { (actionName, _) -> checkAndTriggerAction(actionName) }
            }
        }.runTaskTimer(plugin, 0, SCHEDULE_PERIOD)
        plugin.logger.info("Action scheduler activated.")
    }

    private fun checkAndTriggerAction(actionName: String) {
        // Get the chance for the given event
        val actionChance = actionsChances[actionName]
        if (actionChance != null && actionChance > 0 && RANDOM_GENERATOR.nextInt(actionChance) == 0) {
            executeAction(actionName) // Trigger event if random chance is met
        }
    }

    private fun registerCommands() {
        // Register debugrandom command
        CommandAPICommand("debug")
            .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
            .withUsage("/debug <actionName>")
            .withArguments(
                StringArgument("actionName")
                    .replaceSuggestions(ArgumentSuggestions.strings(ArrayList(debugActions.keys)))
            )
            .executes(CommandExecutor { sender, args ->
                val actionName = args["actionName"] as String
                debugActions[actionName]?.run()
                sender?.sendMessage("[DEBUG] Событие $actionName выполнено.") // Send message to sender
            })
            .register()
    }

    private fun executeAction(actionName: String) {
        val action = actions[actionName.lowercase()]
        if (action != null) {
            action.execute(plugin)
        } else {
            plugin.logger.severe("Action not found: $actionName")
        }
    }

    fun onDisable() {
        AbsRand.getPlayerDataManager().clearAllData()
    }

    companion object {
        private const val SCHEDULE_PERIOD = 20L // Scheduling period for tasks
        private val RANDOM_GENERATOR = Random() // Random number generator
    }
}