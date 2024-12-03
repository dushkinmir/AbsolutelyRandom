package ru.dushkinmir.absolutelyRandom.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.List;

public class Group extends Action {

    private static final Component EVENT_STARTING_MESSAGE =
            Component.text("ГРУППОВАЯ МАСТУРБАЦИЯ НАЧНЕТСЯ ЧЕРЕЗ...", NamedTextColor.RED);
    private static final Component EVENT_END_MESSAGE =
            Component.text("ГРУППОВАЯ МАСТУРБАЦИЯ ОКОНЧЕНА, СПАСИБО ЗА УЧАСТИЕ", NamedTextColor.GREEN);
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int EVENT_DURATION_TICKS = 60;
    private static boolean eventActive = false;

    public Group() {
        super("group");
    }

    @Override
    public void execute(Plugin plugin) {
        if (eventActive) {
            return;
        }
        eventActive = true;

        PlayerUtils.sendMessageToAllPlayers(EVENT_STARTING_MESSAGE, PlayerUtils.MessageType.CHAT);

        new EventCountdownTask(plugin, PlayerUtils.getOnlinePlayers()).runTaskTimer(plugin, 0L, 20L);
    }

    private static class EventCountdownTask extends BukkitRunnable {
        private final Plugin plugin;
        private final List<Player> players;
        private int countdown = COUNTDOWN_SECONDS;

        public EventCountdownTask(Plugin plugin, List<Player> players) {
            this.plugin = plugin;
            this.players = players;
        }

        @Override
        public void run() {
            if (countdown > 0) {
                PlayerUtils.sendMessageToAllPlayers(Component.text(countdown + "...", NamedTextColor.RED), PlayerUtils.MessageType.CHAT);
                countdown--;
            } else {
                new FallingBlocksTask(plugin, players).runTaskTimer(plugin, 0L, 5L);
                this.cancel();
            }
        }
    }

    public static class FallingBlocksTask extends BukkitRunnable {
        private final Plugin plugin;
        private final List<Player> players;
        private int remainingTicks = EVENT_DURATION_TICKS;

        public FallingBlocksTask(Plugin plugin, List<Player> players) {
            this.plugin = plugin;
            this.players = players;
        }

        @Override
        public void run() {
            if (remainingTicks > 0) {
                for (Player player : players) {
                    dropItemNearPlayer(player);
                }
                remainingTicks--;
            } else {
                PlayerUtils.sendMessageToAllPlayers(EVENT_END_MESSAGE, PlayerUtils.MessageType.CHAT);
                eventActive = false;
                this.cancel();
            }
        }

        private void dropItemNearPlayer(Player player) {
            ItemStack item = new ItemStack(Material.WHITE_WOOL, 1);
            Vector direction = player.getEyeLocation().getDirection().normalize();
            var droppedItem = player.getWorld().dropItem(player.getLocation().add(direction.multiply(1.5)), item);

            new BukkitRunnable() {
                @Override
                public void run() {
                    droppedItem.remove();
                }
            }.runTaskLater(plugin, 9L);
        }
    }
}