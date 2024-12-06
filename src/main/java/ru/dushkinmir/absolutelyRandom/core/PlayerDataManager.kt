package ru.dushkinmir.absolutelyRandom.core

import java.util.*
import java.util.concurrent.ConcurrentHashMap

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
}

class PlayerDataManager {
    private val playerDataMap: ConcurrentHashMap<UUID, PlayerData> = ConcurrentHashMap()

    fun getPlayerData(playerUUID: UUID): PlayerData {
        return playerDataMap.computeIfAbsent(playerUUID) { PlayerData() }
    }

    fun removePlayerData(playerUUID: UUID) {
        playerDataMap.remove(playerUUID)
    }

    fun clearAllData() {
        playerDataMap.clear()
    }
}
