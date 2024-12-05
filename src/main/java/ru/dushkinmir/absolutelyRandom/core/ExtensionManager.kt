package ru.dushkinmir.absolutelyRandom.core

import org.bukkit.plugin.Plugin
import ru.dushkinmir.absolutelyRandom.features.sex.AnalFissureHandler
import ru.dushkinmir.absolutelyRandom.features.warp.WarpManager
import ru.dushkinmir.absolutelyRandom.utils.DatabaseManager
import java.sql.SQLException

class ExtensionManager(private val plugin: Plugin) {

    private var database: DatabaseManager? = null // Database manager
    private var fissureHandler: AnalFissureHandler? = null // Handler for anal fissure events
    private var warpManager: WarpManager? = null // Warp manager

    @Throws(SQLException::class)
    fun onEnable() {
        // Open database
        database = DatabaseManager(plugin)
        plugin.logger.info("Database are opened.")
        // Initialize managers
        warpManager = WarpManager(database!!, plugin)
        fissureHandler = AnalFissureHandler(database!!, plugin)
        plugin.logger.info("Managers are initialized.")
    }

    fun getFissureHandler(): AnalFissureHandler {
        return fissureHandler!!
    }

    fun getWarpManager(): WarpManager {
        return warpManager!!
    }

    fun onDisable() {
        database?.let {
            it.close()
            plugin.logger.info("Database are closed.")
        }
    }
}