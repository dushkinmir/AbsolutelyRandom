package ru.dushkinmir.absolutelyRandom.actions;

import org.bukkit.plugin.Plugin;

public abstract class Action {
    private final String name;

    protected Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name.toLowerCase();
    }

    public abstract void execute(Plugin plugin);
}
