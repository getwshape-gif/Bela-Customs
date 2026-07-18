package fr.belarion.belacustoms.commands;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.gui.emeraldenchanttable.EnchantLibraryGUI;
import fr.belarion.belacustoms.managers.MessagesManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
                    sender.sendMessage("Configuration de Bela-Customs rechargee.");
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
            ItemStack block = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta meta = block.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Table d'Enchantement Emeraude");
            block.setItemMeta(meta);
            player.getInventory().addItem(block);
            messages.send(player, "enchants.table-block-received");
            return true;
        }

        if (name.equalsIgnoreCase("enchantanvil")) {
            ItemStack block = new ItemStack(Material.SEA_LANTERN);
            ItemMeta meta = block.getItemMeta();
            meta.setDisplayName(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Enclume Emeraude");
            block.setItemMeta(meta);
            player.getInventory().addItem(block);
            messages.send(player, "enchants.anvil-block-received");
            return true;
        }

        return false;
    }
}
