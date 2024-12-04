package ru.dushkinmir.absolutelyRandom.features.events

import de.tr7zw.changeme.nbtapi.NBT
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils

class DrugsEvent : Listener {

    private val drugs = mapOf(
        Material.SUGAR to Drug(
            name = "Кокаин",
            effects = mapOf(
                "minecraft:speed" to 100,
                "minecraft:haste" to 100,
                "minecraft:weakness" to 100,
                "minecraft:hunger" to 100
            ),
            nutrition = 0,
            saturation = 0.0f,
            particleColor = Color.GRAY,
            sound = Sound.ENTITY_CREEPER_PRIMED
        ),
        Material.WHITE_DYE to Drug(
            name = "Мефедрон",
            effects = mapOf(
                "minecraft:speed" to 100,
                "minecraft:strength" to 100,
                "minecraft:nausea" to 100,
                "minecraft:blindness" to 100
            ),
            nutrition = 0,
            saturation = 0.0f,
            particleColor = Color.WHITE,
            sound = Sound.ENTITY_CREEPER_PRIMED
        ),
        Material.FERN to Drug(
            name = "Марихуана",
            effects = mapOf(
                "minecraft:regeneration" to 100,
                "minecraft:hunger" to 100,
                "minecraft:slowness" to 100,
                "minecraft:weakness" to 100
            ),
            nutrition = 0,
            saturation = 0.0f,
            particleColor = Color.GREEN,
            sound = Sound.ENTITY_CREEPER_PRIMED
        ),
        Material.HONEY_BOTTLE to Drug(
            name = "Пиво",
            effects = mapOf(
                "minecraft:nausea" to 100,
                "minecraft:resistance" to 100,
                "minecraft:mining_fatigue" to 100,
                "minecraft:weakness" to 100
            ),
            nutrition = 6,
            saturation = 1.0f,
            particleColor = Color.YELLOW,
            sound = Sound.ENTITY_CREEPER_PRIMED
        )
    )

    @EventHandler
    fun onPlayerRightClick(event: PlayerInteractEvent) {
        val player = event.player
        val clickedBlock = event.clickedBlock
        val item = event.item

        if (event.action.isRightClick && player.isSneaking && clickedBlock?.type == Material.BREWING_STAND && item != null) {
            NBT.get(item) { nbt ->
                if (!nbt.hasTag("drug")) {
                    drugs[item.type]?.apply(item, clickedBlock, player)
                }
            }
        }
    }

    @EventHandler
    fun onPlayerLeftClick(event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock
        if (event.action.isLeftClick && clickedBlock?.type == Material.BREWING_STAND) {
            val player = event.player
            if (player.gameMode != GameMode.CREATIVE && player.gameMode != GameMode.SPECTATOR) {
                PlayerUtils.sendMessageToPlayer(
                    player,
                    Component.text("дебил хули ты тычешь, совсем уже под солями объебан"),
                    PlayerUtils.MessageType.CHAT
                )
            }
        }
    }

    @EventHandler
    fun onPlayerConsumeDrug(event: PlayerItemConsumeEvent) {
        val item = event.item
        NBT.get(item) { nbt ->
            if ("вова хорошенький" == nbt.getString("drug")) {
                val player = event.player
                val drugMessages = mapOf(
                    Material.WHITE_DYE to "мефедрончиком",
                    Material.SUGAR to "кокаинчиком",
                    Material.FERN to "марихуаной",
                    Material.HONEY_BOTTLE to "пивком"
                )
                drugMessages[item.type]?.let { drugType ->
                    val chatMessage = "агаа!! ${player.name} у нас тут оказывается балуется $drugType"
                    val actionBarMessage = "уф бляя мощненько так закинулся $drugType"
                    PlayerUtils.sendMessageToPlayer(player, Component.text(chatMessage), PlayerUtils.MessageType.CHAT)
                    PlayerUtils.sendMessageToPlayer(
                        player,
                        Component.text(actionBarMessage),
                        PlayerUtils.MessageType.ACTION_BAR
                    )
                }
            }
        }
    }
}
