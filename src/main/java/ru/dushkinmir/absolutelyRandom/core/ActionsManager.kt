package ru.dushkinmir.absolutelyRandom.core

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.reflections.Reflections
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import java.util.*

class ActionsManager(private val plugin: Plugin) {
    private val actions: MutableMap<String, Action> = HashMap()
    private val actionsChances: MutableMap<String, Int> = HashMap() // Chances for actions
    private val debugActions: MutableMap<String, Runnable> = HashMap()

    // Автоматическая регистрация всех Action классов
    fun registerAllActions() {
        val reflections = Reflections("ru.dushkinmir.absolutelyRandom.features.actions.types")
        // Ищем все классы, которые наследуют Action
        reflections.getSubTypesOf(Action::class.java).forEach { actionClass ->
            try {
                val action = actionClass.getDeclaredConstructor().newInstance()
                actionsChances[action.name] = plugin.config.getInt("chances.%s-chance".format(action.name), -1)
                actions[action.name] = action
                actions.forEach { (aname, _) -> debugActions[aname] = Runnable { executeAction(aname) } }
            } catch (e: Exception) {
                plugin.logger.severe("An error has been occurred:\n" + e.message)
            }
        }
        scheduleActionTrigger()
        registerCommands()
    }

    private fun scheduleActionTrigger() {
        // Schedule task to trigger random events at intervals
        object : BukkitRunnable() {
            override fun run() {
                executeRandomActions() // Execute random events
            }
        }.runTaskTimer(plugin, 0, SCHEDULE_PERIOD)
        plugin.logger.info("Запланированные действия активированы.")
    }

    private fun executeRandomActions() {
        // Get list of online players
        val players: List<Player> = ArrayList(plugin.server.onlinePlayers)
        if (players.isEmpty()) return // Return if no players are online

        actions.forEach { (actionName, _) -> checkAndTriggerAction(actionName) }
    }

    private fun checkAndTriggerAction(actionName: String) {
        // Get the chance for the given event
        val eventChance = actionsChances[actionName]
        if (eventChance != null && eventChance > 0 && RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            executeAction(actionName) // Trigger event if random chance is met
        }
    }

    fun registerCommands() {
        // Register debugrandom command
        CommandAPICommand("debugrandom")
            .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
            .withUsage("/debug <random>")
            .withArguments(
                StringArgument("random")
                    .replaceSuggestions(ArgumentSuggestions.strings(ArrayList(debugActions.keys)))
            )
            .executes(CommandExecutor { sender, args ->
                val action = args["random"] as String
                handleDebugAction(sender, action) // Handle debug random event
            })
            .register()
    }

    fun handleDebugAction(sender: CommandSender?, event: String) {
        val eventAction = debugActions[event] // Get event action by key
        if (eventAction != null) {
            eventAction.run() // Execute event action
            sender?.sendMessage("[DEBUG] Событие $event выполнено.") // Send message to sender
        }
    }

    fun executeAction(actionName: String) {
        val action = actions[actionName.lowercase(Locale.getDefault())]
        if (action != null) {
            action.execute(plugin)
        } else {
            plugin.logger.severe("Action not found: $actionName")
        }
    }

    companion object {
        private const val SCHEDULE_PERIOD = 20L // Scheduling period for tasks
        private val RANDOM_GENERATOR = Random() // Random number generator
    }
}