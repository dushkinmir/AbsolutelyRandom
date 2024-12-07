package ru.dushkinmir.absolutelyRandom.core

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerDataManager {
    private val playerDataMap: MutableMap<String, MutableMap<UUID, PlayerData>> = ConcurrentHashMap()

    fun getPlayerDataForAction(actionName: String, playerUUID: UUID): PlayerData {
        val actionData = playerDataMap.computeIfAbsent(actionName) { ConcurrentHashMap() }
        return actionData.computeIfAbsent(playerUUID) { PlayerData() }
    }

    fun removePlayerDataForAction(actionName: String, playerUUID: UUID) {
        playerDataMap[actionName]?.remove(playerUUID)
    }

    fun clearActionData(actionName: String) {
        playerDataMap.remove(actionName)
    }

    fun clearAllData() {
        playerDataMap.clear()
    }
}

class PlayerData : MutableMap<String, Any> by ConcurrentHashMap()


