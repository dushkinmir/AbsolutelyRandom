package ru.dushkinmir.absolutelyRandom.core

import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.actions.types.Stinky
import ru.dushkinmir.absolutelyRandom.features.betters.HeadChat
import ru.dushkinmir.absolutelyRandom.features.betters.NameHider
import ru.dushkinmir.absolutelyRandom.features.events.ConsentEvent
import ru.dushkinmir.absolutelyRandom.features.events.DrugsEvent

class EventsManager(private val plugin: Plugin, private val extensionManager: ExtensionManager) {

    fun onEnable() {
        val events: MutableList<Listener> = ArrayList()
        events.add(DrugsEvent())
        events.add(Stinky())
        events.add(ConsentEvent(plugin))
        events.add(extensionManager.getFissureHandler()!!)

        if (plugin.config.getBoolean("betters.name-hider", false)) {
            events.add(NameHider(plugin))
        }
        if (plugin.config.getBoolean("betters.head-chat", false)) {
            events.add(HeadChat(plugin))
        }

        events.forEach { event -> plugin.server.pluginManager.registerEvents(event, plugin) }
        plugin.logger.info("Все события зарегистрированы.")
    }

    fun onDisable() {
        if (plugin.config.getBoolean("betters.head-chat", false)) {
            HeadChat.onDisable(plugin)
        }
    }
}