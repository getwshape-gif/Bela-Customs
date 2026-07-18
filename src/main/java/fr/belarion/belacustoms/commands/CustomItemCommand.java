package fr.belarion.belacustoms.commands;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import fr.belarion.belacustoms.managers.MessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /citem give <joueur> <id> [quantite]
 * /citem list
 * /citem id            (affiche l'id du custom item tenu en main)
 */
public class CustomItemCommand implements CommandExecutor, TabCompleter {

    private final BelaCustoms plugin;
    private final CustomItemManager manager;

    public CustomItemCommand(BelaCustoms plugin, CustomItemManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private MessagesManager messages() {
        return plugin.getMessagesManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messages().get("items.usage"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "give":
                return handleGive(sender, args);
            case "list":
                return handleList(sender);
            case "id":
                return handleId(sender);
            default:
                sender.sendMessage(messages().get("items.unknown-subcommand"));
                return true;
        }
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messages().get("items.give-usage"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(messages().get("items.player-not-found", "player", args[1]));
            return true;
        }
        String id = args[2];
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException ex) {
                sender.sendMessage(messages().get("items.invalid-amount", "amount", args[3]));
                return true;
            }
        }

        boolean success = manager.give(target, id, amount);
        if (!success) {
            sender.sendMessage(messages().get("items.unknown-id", "id", id));
            return true;
        }

        sender.sendMessage(messages().get("items.give-success-sender", "amount", String.valueOf(amount), "id", id, "player", target.getName()));
        if (!sender.equals(target)) {
            target.sendMessage(messages().get("items.give-success-target", "amount", String.valueOf(amount), "id", id));
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        String ids = manager.getRegistry().getIds().stream().collect(Collectors.joining(", "));
        sender.sendMessage(messages().get("items.list", "ids", ids));
        return true;
    }

    private boolean handleId(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messages().get("items.player-only-subcommand"));
            return true;
        }
        Player player = (Player) sender;
        String id = manager.getId(player.getItemInHand());
        if (id == null) {
            sender.sendMessage(messages().get("items.id-not-custom"));
        } else {
            sender.sendMessage(messages().get("items.id-result", "id", id));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(java.util.Arrays.asList("give", "list", "id"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(manager.getRegistry().getIds());
        }

        String current = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";
        return completions.stream()
                .filter(s -> s.toLowerCase(Locale.ROOT).startsWith(current))
                .collect(Collectors.toList());
    }
}
