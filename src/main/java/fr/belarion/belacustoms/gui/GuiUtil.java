package fr.belarion.belacustoms.gui;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helpers partages par tous les GUI du plugin pour garder une identite
 * visuelle unique : vitres premium (gris fonce / gris clair, accents
 * emeraude), separateurs, boutons, et le format standard des descriptions
 * de Custom Enchants.
 */
public final class GuiUtil {

    public static final String SEPARATOR = ChatColor.DARK_GREEN + "" + ChatColor.STRIKETHROUGH
            + "                    ";

    /** Vitre teintee verte (donnee 13 = vert emeraude), utilisee en accent premium. */
    public static final short GLASS_GREEN = 13;
    /** Vitre vert clair, utilisee pour accentuer certains bords. */
    public static final short GLASS_LIME = 5;
    public static final short GLASS_BLACK = 15;
    /** Vitre gris fonce : couleur de fond dominante du style premium. */
    public static final short GLASS_DARK_GRAY = 7;
    /** Vitre gris clair / argente : bordures du style premium. */
    public static final short GLASS_LIGHT_GRAY = 8;
    /** Vitre blanche. */
    public static final short GLASS_WHITE = 0;

    private GuiUtil() {}

    public static ItemStack pane(short data) {
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        return pane;
    }

    /** Remplit tout l'inventaire de vitres vert emeraude (fond simple, encore utilise par l'Enclume). */
    public static void fillBackground(Inventory inv) {
        ItemStack filler = pane(GLASS_GREEN);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
    }

    /**
     * Fond premium partage par la Table d'Enchantement et la Bibliotheque :
     * gris fonce dominant, bordures (premiere et derniere rangee) en gris
     * clair, et quelques vitres emeraude en accent sur les slots fournis.
     * Les slots fonctionnels places apres cet appel ecrasent naturellement
     * les vitres.
     */
    public static void fillPremiumBackground(Inventory inv, int[] accentSlots) {
        int size = inv.getSize();
        ItemStack dark = pane(GLASS_DARK_GRAY);
        ItemStack light = pane(GLASS_LIGHT_GRAY);
        ItemStack accent = pane(GLASS_GREEN);

        for (int i = 0; i < size; i++) {
            inv.setItem(i, dark);
        }
        for (int i = 0; i < 9 && i < size; i++) {
            inv.setItem(i, light);
        }
        for (int i = Math.max(0, size - 9); i < size; i++) {
            inv.setItem(i, light);
        }
        if (accentSlots != null) {
            for (int slot : accentSlots) {
                if (slot >= 0 && slot < size) {
                    inv.setItem(slot, accent);
                }
            }
        }
    }

    public static ItemStack button(Material material, ChatColor titleColor, String title, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(titleColor.toString() + ChatColor.BOLD + title);
        meta.setLore(Arrays.asList(loreLines));
        item.setItemMeta(meta);
        return item;
    }

    public static void appendLore(ItemStack item, List<String> extra) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(extra);
        item.setItemMeta(meta);
    }

    /**
     * Format standard et unique de la description d'un Custom Enchant,
     * utilise a la fois par les livres (EnchantBookUtil) et la Bibliotheque
     * d'Enchants (EnchantLibraryGUI) : description, compatibilite, puis
     * le tag "Custom enchant" en violet. Aucune rarete, aucune ligne
     * inutile.
     */
    public static List<String> buildEnchantLore(CustomEnchant enchant) {
        List<String> lore = new ArrayList<String>();
        for (String line : enchant.getDescription()) {
            lore.add(ChatColor.GRAY + line);
        }
        lore.add("");
        lore.add(ChatColor.GRAY + "Compatible :");
        lore.add(ChatColor.GREEN + "✔ " + enchant.getTarget().getLabel());
        lore.add("");
        lore.add(ChatColor.LIGHT_PURPLE + "Custom enchant");
        return lore;
    }
}
