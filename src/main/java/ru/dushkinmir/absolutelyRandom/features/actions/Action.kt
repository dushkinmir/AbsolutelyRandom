package ru.dushkinmir.absolutelyRandom.features.actions

import org.bukkit.plugin.Plugin

abstract class Action(internal val name: String) {
    abstract fun execute(plugin: Plugin)
}