package ru.dushkinmir.absolutelyRandom.features.actions

import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.core.PlayerData
import ru.dushkinmir.absolutelyRandom.core.PlayerDataManager
import java.util.*

abstract class Action(internal val name: String) {
    abstract fun execute(plugin: Plugin)

    protected fun getPlayerData(playerUUID: UUID): PlayerData {
        val actionName = this::class.simpleName!!
        return PlayerDataManager.getPlayerDataForAction(actionName, playerUUID)
    }

    protected fun removePlayerData(playerUUID: UUID) {
        val actionName = this::class.simpleName!!
        PlayerDataManager.removePlayerDataForAction(actionName, playerUUID)
    }
}
