package ru.dushkinmir.absolutelyRandom.actions;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;

import java.util.*;

public class ActionsManager {
    private final Map<String, Action> actions = new HashMap<>();
    private final Map<String, Integer> actionsChances = new HashMap<>(); // Chances for actions
    private static final long SCHEDULE_PERIOD = 20L; // Scheduling period for tasks
    private static final Random RANDOM_GENERATOR = new Random(); // Random number generator
    private final Map<String, Runnable> debugActions = new HashMap<>();

    private final Plugin plugin;

    public ActionsManager(Plugin plugin) {
        this.plugin = plugin;
    }

    // Автоматическая регистрация всех Action классов
    public void registerAllActions() {
        Reflections reflections = new Reflections("ru.dushkinmir.absolutelyRandom.actions");
        // Ищем все классы, которые наследуют Action
        reflections.getSubTypesOf(Action.class).forEach(actionClass -> {
            try {
                Action action = actionClass.getDeclaredConstructor().newInstance();
                actionsChances.put(action.getName(), plugin.getConfig().getInt("chances.%s-chance".formatted(action.getName()), -1));
                actions.put(action.getName(), action);
                actions.forEach((aname, a) -> debugActions.put(aname, () -> executeAction(aname)));
            } catch (Exception e) {
                plugin.getLogger().severe("An error has been occurred:\n" + e.getMessage());
            }
        });
        scheduleActionTrigger();
    }

    private void scheduleActionTrigger() {
        // Schedule task to trigger random events at intervals
        new BukkitRunnable() {
            @Override
            public void run() {
                executeRandomActions(); // Execute random events
            }
        }.runTaskTimer(plugin, 0, SCHEDULE_PERIOD);
        plugin.getLogger().info("Запланированные действия активированы.");
    }

    private void executeRandomActions() {
        // Get list of online players
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (players.isEmpty()) return; // Return if no players are online

        actions.forEach((actionName, action) -> checkAndTriggerAction(actionName));
    }

    private void checkAndTriggerAction(String actionName) {
        // Get the chance for the given event
        Integer eventChance = actionsChances.get(actionName);
        if (eventChance != null && eventChance > 0 && RANDOM_GENERATOR.nextInt(eventChance) == 0) {
            executeAction(actionName); // Trigger event if random chance is met
        }
    }

    public void registerCommands() {
        // Register debugrandom command
        new CommandAPICommand("debugrandom")
                .withPermission(CommandPermission.fromString("absolutlyrandom.admin"))
                .withUsage("/debug <random>")
                .withArguments(new StringArgument("random")
                        .replaceSuggestions(ArgumentSuggestions.strings(
                                new ArrayList<>(debugActions.keySet())))
                )
                .executes((sender, args) -> {
                    String action = (String) args.get("random");
                    assert action != null;
                    handleDebugAction(sender, action); // Handle debug random event
                })
                .register();
    }

    public void handleDebugAction(CommandSender sender, String event) {
        Runnable eventAction = debugActions.get(event); // Get event action by key
        if (eventAction != null) {
            eventAction.run(); // Execute event action
            if (sender != null) {
                sender.sendMessage("[DEBUG] Событие " + event + " выполнено."); // Send message to sender
            }
        }
    }

    public void executeAction(String actionName) {
        Action action = actions.get(actionName.toLowerCase());
        if (action != null) {
            action.execute(plugin);
        } else {
            plugin.getLogger().severe("Action not found: " + actionName);
        }
    }
}
