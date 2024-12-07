package ru.dushkinmir.absolutelyRandom.features.actions.actions

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.dushkinmir.absolutelyRandom.features.actions.Action
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils

class Inventory : Action("inventory") {
    override fun execute(plugin: Plugin) {
        val player = PlayerUtils.getRandomPlayer()
        val savedInventory = stealInventory(player)
        val savedGameMode = changeGameMode(player)
        addPotionEffect(player)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            returnEverythingBack(player, savedInventory, savedGameMode)
            player.sendMessage("ладно не плачь щовэл")
        }, 20L * 60)
    }

    private fun stealInventory(player: Player): Array<ItemStack?> {
        val inventoryContents = player.inventory.contents.clone()
        player.inventory.clear()
        return inventoryContents
    }

    private fun changeGameMode(player: Player): GameMode {
        val currentGameMode = player.gameMode
        player.gameMode = GameMode.ADVENTURE
        player.sendMessage("ты тупой нигер")
        return currentGameMode
    }

    private fun addPotionEffect(player: Player): List<PotionEffectType> {
        val potionEffects = listOf<PotionEffectType>(PotionEffectType.SLOWNESS, PotionEffectType.MINING_FATIGUE)
        potionEffects.forEach { type ->
            {
                val effect = PotionEffect(type, 20 * 60, 1)
                player.addPotionEffect(effect)
            }
        }
        return potionEffects
    }

    private fun returnEverythingBack(
        player: Player,
        savedInventory: Array<ItemStack?>,
        savedGameMode: GameMode
    ) {
        player.inventory.contents = savedInventory
        player.gameMode = savedGameMode
    }
}
