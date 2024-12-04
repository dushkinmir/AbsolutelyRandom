package ru.dushkinmir.absolutelyRandom.features.betters

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NameHider(private val plugin: Plugin) : Listener {
    private val visibilityMap: MutableMap<UUID, MutableMap<UUID, Boolean>> = ConcurrentHashMap()

    init {
        startVisibilityTask()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerId = event.player.uniqueId
        visibilityMap.putIfAbsent(playerId, ConcurrentHashMap())
    }

    private fun startVisibilityTask() {
        object : BukkitRunnable() {
            override fun run() {
                try {
                    updateVisibility()
                } catch (e: Exception) {
                    plugin.logger.severe(e.message)
                }
            }
        }.runTaskTimer(plugin, 0L, 3L) // Обновление каждые 20 тиков (1 секунда)
    }

    fun updateVisibility() {
        for (viewer in plugin.server.onlinePlayers) {
            val viewerId = viewer.uniqueId
            visibilityMap.putIfAbsent(viewerId, ConcurrentHashMap())
            val viewerVisibilityMap = visibilityMap[viewerId]!!

            for (target in plugin.server.onlinePlayers) {
                if (viewer == target) continue

                val targetId = target.uniqueId
                val canSee = viewer.hasLineOfSight(target)

                if (canSee && (!viewerVisibilityMap.containsKey(targetId) || viewerVisibilityMap[targetId] == false)) {
                    viewer.showPlayer(plugin, target)
                    viewerVisibilityMap[targetId] = true
                } else if (!canSee && (!viewerVisibilityMap.containsKey(targetId) || viewerVisibilityMap[targetId] == true)) {
                    viewer.hidePlayer(plugin, target)
                    viewerVisibilityMap[targetId] = false
                }
            }
        }
    }
}