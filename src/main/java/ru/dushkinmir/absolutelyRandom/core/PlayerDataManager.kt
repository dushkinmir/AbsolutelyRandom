package ru.dushkinmir.absolutelyRandom.core

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class PlayerDataManager {
    private val actionDataMap: MutableMap<String, MutableMap<UUID, PlayerData>> = mutableMapOf()

    fun getPlayerDataForAction(actionName: String, playerUUID: UUID): PlayerData {
        return actionDataMap
            .computeIfAbsent(actionName) { mutableMapOf() }
            .computeIfAbsent(playerUUID) { PlayerData() }
    }

    fun removePlayerDataForAction(actionName: String, playerUUID: UUID) {
        actionDataMap[actionName]?.remove(playerUUID)
    }

    // Очистить все данные
    fun clearAllData() {
        actionDataMap.clear()
    }
}

class PlayerData {

    private val data: MutableMap<String, Any> = ConcurrentHashMap()

    fun <T> set(key: String, value: T) {
        data[key] = value as Any
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return data[key] as? T
    }

    fun remove(key: String) {
        data.remove(key)
    }

    fun clear() {
        data.clear()
    }

    fun hasData(): Boolean {
        return data.isNotEmpty()
    }
}

