package ru.dushkinmir.absolutelyRandom.core;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.dushkinmir.absolutelyRandom.features.actions.types.Stinky;
import ru.dushkinmir.absolutelyRandom.features.betters.HeadChat;
import ru.dushkinmir.absolutelyRandom.features.betters.NameHider;
import ru.dushkinmir.absolutelyRandom.features.events.ConsentEvent;
import ru.dushkinmir.absolutelyRandom.features.events.DrugsEvent;

import java.util.ArrayList;
import java.util.List;

public class EventsManager {
    private final Plugin plugin;
    private final ExtensionManager extensionManager;

    public EventsManager(Plugin plugin, ExtensionManager extensionManager) {
        this.plugin = plugin;
        this.extensionManager = extensionManager;
    }

    public void registerEvents() {
        List<Listener> events = new ArrayList<>();
        events.add(new DrugsEvent());
        events.add(new Stinky());
        events.add(new ConsentEvent(plugin));
        events.add(extensionManager.getFissureHandler());

        if (plugin.getConfig().getBoolean("betters.name-hider", false)) {
            events.add(new NameHider(plugin));
        }
        if (plugin.getConfig().getBoolean("betters.head-chat", false)) {
            events.add(new HeadChat(plugin));
        }

        events.forEach(event -> plugin.getServer().getPluginManager().registerEvents(event, plugin));
        plugin.getLogger().info("Все события зарегистрированы.");
    }

    public void onDisable() {
        if (plugin.getConfig().getBoolean("betters.head-chat", false)) {
            HeadChat.onDisable(plugin);
        }
    }
}
