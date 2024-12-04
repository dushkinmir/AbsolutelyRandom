package ru.dushkinmir.absolutelyRandom.features.sex;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SexCommands {
    AnalFissureHandler fissureHandler;
    Plugin plugin;

    public SexCommands(AnalFissureHandler fissureHandler, Plugin plugin) {
        this.fissureHandler = fissureHandler;
        this.plugin = plugin;
    }

    public void registerSexCommand() {
        Argument<?> noSelectorSuggestions = new PlayerArgument("target")
                .replaceSafeSuggestions(SafeSuggestions.suggest(info -> {
                    // Получаем игрока, который вводит команду
                    Player senderPlayer = (Player) info.sender();
                    // Получаем всех онлайн игроков, кроме отправителя
                    return plugin.getServer().getOnlinePlayers().stream()
                            .filter(player -> !player.equals(senderPlayer)) // исключаем отправителя
                            .toArray(Player[]::new);
                }));
        new CommandAPICommand("sex")
                .withArguments(noSelectorSuggestions)
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        Player target = (Player) args.get("target");
                        SexManager.triggerSexEvent(player, target, plugin, fissureHandler);
                    }
                })
                .register();
    }
}
