package ru.dushkinmir.absolutelyRandom.features.actions

import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.AbsRand
import ru.dushkinmir.absolutelyRandom.core.PlayerData
import java.util.*

abstract class Action(internal val name: String) {
    abstract fun execute(plugin: Plugin)

    // Возвращает данные для игрока
    fun getPlayerData(playerUUID: UUID): PlayerData {
        return AbsRand.getPlayerDataManager().getPlayerDataForAction(name, playerUUID)
    }
}
