package ru.dushkinmir.absolutelyRandom.core

import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.stringArgument
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.features.actions.actions.*
import kotlin.random.Random


class ActionsManager(private val plugin: Plugin) {
    private val actions: MutableMap<String, Action> = mutableMapOf<String, Action>()
    private val actionsChances: MutableMap<String, Int> = mutableMapOf<String, Int>()
    private val debugActions: MutableList<String> = mutableListOf<String>()

    fun onEnable() {
        registerAction(Crash())
        registerAction(Group())
        registerAction(Inventory())
        registerAction(Kick())
        registerAction(Prank())
        registerAction(RandomMessage())
        registerAction(Stinky(plugin))
        registerAction(Storm())

        actionScheduler()
        registerCommands()
    }

    fun registerAction(action: Action) {
        actions[action.name] = action
        actionsChances[action.name] = plugin.config.getInt("chances.${action.name}-chance", -1)
        debugActions.add(action.name)
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
        }.runTaskTimer(plugin, 0, 20L)
        plugin.logger.info("Action scheduler activated.")
    }

    private fun checkAndTriggerAction(actionName: String) {
        // Get the chance for the given event
        val actionChance = actionsChances[actionName]
        if (actionChance != null && actionChance > 0 && Random.nextInt(actionChance) == 0) {
            executeAction(actionName) // Trigger event if random chance is met
        }
    }

    private fun registerCommands() {
        // Register debugrandom command
        commandTree("debugaction") {
            withPermission("absolutlyrandom.admin")
            withUsage("/debugaction <actionName>")
            stringArgument("actionName") {
                replaceSuggestions(ArgumentSuggestions.strings(debugActions))
                anyExecutor { executor, args ->
                    val actionName = args["actionName"] as String
                    if (executeAction(actionName)) executor.sendMessage("[DEBUG] Событие $actionName выполнено.")
                    else executor.sendMessage("oops!")
                }
            }
        }
    }

    private fun executeAction(actionName: String): Boolean {
        val action = actions[actionName.lowercase()]
        if (action != null) {
            action.execute(plugin)
            return true
        } else {
            plugin.logger.severe("Action not found: $actionName")
            return false
        }
    }

    fun onDisable() {
        PlayerDataManager.clearAllData()
    }
}