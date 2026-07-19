package fr.belarion.belacustoms.commands;

import fr.belarion.belacustoms.utils.NBTEditor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

/**
* /beladebug (alias /customdebug) : commande de debug TEMPORAIRE, strictement
* en lecture seule. Affiche dans le chat toutes les informations pertinentes
* de l'item tenu en main (Material, nom, lore, enchantements, durabilite,
* tag NBT CustomItemId, NBT brut complet) afin de verifier la valeur exacte
* a utiliser dans les regles OptiFine CIT du Resource Pack.
*
* Ne modifie jamais l'item ni son NBT : aucun appel a setString/setBoolean,
* uniquement des lectures (NBTEditor.getCustomId, getRawTagString, etc.).
* Ne change rien au fonctionnement existant des custom items.
*/
public class DebugCommand implements CommandExecutor {

@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
if (!(sender instanceof Player)) {
sender.sendMessage(ChatColor.RED + "Cette commande doit etre executee par un joueur tenant un item en main.");
return true;
}

Player player = (Player) sender;
ItemStack item = player.getItemInHand();

if (item == null || item.getType() == org.bukkit.Material.AIR) {
sender.sendMessage(ChatColor.RED + "Tu ne tiens aucun item en main.");
return true;
}

sender.sendMessage(ChatColor.GOLD + "========== CUSTOM ITEM DEBUG ==========");
sender.sendMessage(ChatColor.YELLOW + "Material: " + ChatColor.WHITE + item.getType().name());
sender.sendMessage(ChatColor.YELLOW + "Durability: " + ChatColor.WHITE + item.getDurability());

ItemMeta meta = item.getItemMeta();
if (meta != null && meta.hasDisplayName()) {
sender.sendMessage(ChatColor.YELLOW + "Display Name: " + ChatColor.WHITE + meta.getDisplayName());
} else {
sender.sendMessage(ChatColor.YELLOW + "Display Name: " + ChatColor.GRAY + "(aucun)");
}

if (meta != null && meta.hasLore()) {
sender.sendMessage(ChatColor.YELLOW + "Lore:");
List<String> lore = meta.getLore();
for (String line : lore) {
sender.sendMessage(ChatColor.WHITE + " - " + line);
}
} else {
sender.sendMessage(ChatColor.YELLOW + "Lore: " + ChatColor.GRAY + "(aucun)");
}

Map<Enchantment, Integer> enchants = item.getEnchantments();
if (!enchants.isEmpty()) {
sender.sendMessage(ChatColor.YELLOW + "Enchantements:");
for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
sender.sendMessage(ChatColor.WHITE + " - " + entry.getKey().getName() + " " + entry.getValue());
}
} else {
sender.sendMessage(ChatColor.YELLOW + "Enchantements: " + ChatColor.GRAY + "(aucun)");
}

String customId = NBTEditor.getCustomId(item);
sender.sendMessage(ChatColor.YELLOW + "CustomItemId: " + ChatColor.WHITE
+ (customId != null ? customId : ChatColor.GRAY + "(aucun - item non custom)"));

sender.sendMessage(ChatColor.YELLOW + "Unbreakable (NBT): " + ChatColor.WHITE + NBTEditor.isUnbreakable(item));

String rawTag = NBTEditor.getRawTagString(item);
sender.sendMessage(ChatColor.YELLOW + "NBT brut complet:");
sender.sendMessage(ChatColor.WHITE + (rawTag != null ? rawTag : "(aucun tag NBT)"));

sender.sendMessage(ChatColor.GOLD + "========================================");
return true;
}
}
