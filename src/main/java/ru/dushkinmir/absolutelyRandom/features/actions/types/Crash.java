package ru.dushkinmir.absolutelyRandom.features.actions.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dushkinmir.absolutelyRandom.features.actions.Action;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.util.Random;

public class Crash extends Action {
    private static final Random RANDOM = new Random();
    private static final int GLITCH_DURATION_TICKS = 200; // Длительность хаоса
    private static final Component DISCONNECT_MESSAGE = Component.text(
            "Critical error: Server crashed. Please try again later.", NamedTextColor.RED);
    private static final Component FINAL_MOTD = Component.text("System Failure: Rebooting...", NamedTextColor.RED);
    private static final Material GLITCH_BLOCK = Material.BARRIER; // Используем барьер как эффект замены

    public Crash() {
        super("crash");
    }

    @Override
    public void execute(Plugin plugin) {
        // Шаг 1: Стартуем визуальный хаос
        startGlitches(plugin);

        // Шаг 2: Через некоторое время кикаем всех игроков
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            kickAllPlayersWithStyle();
            setMotd(FINAL_MOTD);
        }, GLITCH_DURATION_TICKS);
    }

    private static void startGlitches(Plugin plugin) {
        new BukkitRunnable() {
            int iterations = 0;

            @Override
            public void run() {
                if (iterations++ > 10) {
                    cancel(); // Останавливаем хаос через 10 итераций
                    return;
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    createGlitchEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Повторяем каждую секунду
    }

    private static void createGlitchEffects(Player player) {
        // Телепортируем в случайную точку неподалёку
        Location randomLocation = getRandomNearbyLocation(player.getLocation(), 20);
        player.teleport(randomLocation);

        // Отправляем "глючные" сообщения в чат
        String glitchMessage = generateRandomGlitchMessage(player.getName());
        PlayerUtils.sendMessageToPlayer(player, Component.text(glitchMessage, NamedTextColor.DARK_RED), PlayerUtils.MessageType.ACTION_BAR);

        // Добавляем ослепление или другие эффекты
        player.addPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS.createEffect(40, 1));

        // Спавним молнии рядом
        World world = player.getWorld();
        world.strikeLightning(randomLocation);

        // Заменяем блоки вокруг игрока на блоки барьеров (только для него)
        replaceNearbyBlocksWithGlitch(player);
    }

    private static void replaceNearbyBlocksWithGlitch(Player player) {
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();

        if (world == null) return;

        // Проверяем блоки вокруг игрока (в радиусе 5 блоков)
        for (int x = -5; x <= 5; x++) {
            for (int y = -2; y <= 2; y++) { // Небольшая высота для эффекта
                for (int z = -5; z <= 5; z++) {
                    Location loc = playerLocation.clone().add(x, y, z);

                    // Заменяем блок только для этого игрока, на барьер (или другой блок)
                    player.sendBlockChange(loc, GLITCH_BLOCK.createBlockData());
                }
            }
        }
    }

    private static Location getRandomNearbyLocation(Location base, int range) {
        World world = base.getWorld();
        if (world == null) return base;

        double offsetX = RANDOM.nextInt(range * 2) - range;
        double offsetZ = RANDOM.nextInt(range * 2) - range;
        return base.clone().add(offsetX, 0, offsetZ);
    }

    private static String generateRandomGlitchMessage(String playerName) {
        return "ERROR_" + RANDOM.nextInt(9999) + " Player " + playerName + " disconnected";
    }

    private static void kickAllPlayersWithStyle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerUtils.kickPlayer(player, DISCONNECT_MESSAGE);
        }
    }

    private static void setMotd(Component motd) {
        Bukkit.getServer().motd(motd);
    }


}
