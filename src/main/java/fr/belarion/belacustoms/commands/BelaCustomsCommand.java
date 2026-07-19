package fr.belarion.belacustoms.commands;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldAnvilItem;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldEnchantTableItem;
import fr.belarion.belacustoms.gui.emeraldenchanttable.EnchantLibraryGUI;
import fr.belarion.belacustoms.managers.MessagesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
* Executeur unique pour les commandes historiquement portees par le plugin
    * Belarion-Enchants : /belacustoms (admin, reload), /enchanttable,
* /enchantanvil et /enchants. Toutes routees ici depuis plugin.yml.
    */
public class BelaCustomsCommand implements CommandExecutor {

    private final BelaCustoms plugin;

public BelaCustomsCommand(BelaCustoms plugin) {
    this.plugin = plugin;
}

@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String name = command.getName();
        MessagesManager messages = plugin.getMessagesManager();

    if (name.equalsIgnoreCase("belacustoms")) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            if (sender instanceof Player) {
                messages.send((Player) sender, "reload");
            } else {
                sender.sendMessage("Configuration de Bela-Customs rechargée.");
            }
            return true;
        }
        sender.sendMessage(ChatColor.YELLOW + "Utilisation : /belacustoms reload");
        return true;
    }

    if (!(sender instanceof Player)) {
        sender.sendMessage("Commande joueur uniquement.");
        return true;
    }
        Player player = (Player) sender;

    if (name.equalsIgnoreCase("enchants")) {
        if (!player.hasPermission("belacustoms.enchants")) {
            messages.send(player, "no-permission");
            return true;
        }
        // Ouvre exactement le meme GUI que le bouton Bibliotheque de la
        // table d'enchantement : une seule version du GUI a maintenir.
        player.openInventory(EnchantLibraryGUI.build(0));
        return true;
    }

    if (name.equalsIgnoreCase("enchanttable")) {
        // Distribue desormais le vrai CustomItem EMERALD_ENCHANT_TABLE
        // (NBT CustomItemId + texture), au lieu d'un simple ItemStack sans
        // identite : le nom affiche et le comportement (pose, clic-droit,
        // GUI) restent strictement identiques.
        plugin.getCustomItemManager().give(player, EmeraldEnchantTableItem.ID, 1);
        messages.send(player, "enchants.table-block-received");
        return true;
    }

    if (name.equalsIgnoreCase("enchantanvil")) {
        // Idem pour EMERALD_ANVIL.
        plugin.getCustomItemManager().give(player, EmeraldAnvilItem.ID, 1);
        messages.send(player, "enchants.anvil-block-received");
        return true;
    }

    return false;
    }
}
