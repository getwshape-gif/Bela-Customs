package fr.belarion.belacustoms.gui.emeraldanvil;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.gui.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI de l'Enclume Émeraude : 27 slots, même style premium que la Table
 * d'Enchantement Émeraude (gris foncé dominant, bordures gris clair,
 * accents émeraude autour des slots fonctionnels), pour une identité
 * visuelle cohérente entre les deux interfaces.
 * Item à réparer/enchanter : slot 11. Forger : slot 13.
 * Slot 15 (droite) : DOUBLE USAGE, determine par ce qui y est place :
 *   - Livre (vanilla ou custom) -> applique un enchant (prix fixe,
 *     costs.emerald-anvil, 30 niveaux par defaut).
 *   - Blocs d'Émeraude normaux -> repare l'item de gauche (prix variable
 *     en blocs selon les degats + prix fixe en niveaux,
 *     costs.emerald-anvil-repair, 30 niveaux par defaut). Voir
 *     EmeraldAnvilListener pour la logique complete.
 */
public final class EmeraldAnvilGUI {

    public static final String TITLE = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "✦ Enclume Émeraude ✦";
    public static final int SLOT_ITEM = 11;
    public static final int SLOT_CONFIRM = 13;
    public static final int SLOT_BOOK = 15;

    /** Vitres émeraude décoratives encadrant les trois slots fonctionnels. */
    private static final int[] ACCENT_SLOTS = new int[]{10, 12, 14, 16};

    private EmeraldAnvilGUI() {}

    public static Inventory build() {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        GuiUtil.fillPremiumBackground(inv, ACCENT_SLOTS);

        inv.setItem(SLOT_ITEM, null);
        inv.setItem(SLOT_BOOK, null);
        inv.setItem(SLOT_CONFIRM, buildConfirmButton());

        return inv;
    }

    public static ItemStack buildConfirmButton() {
        int enchantCost = BelaCustoms.get().getEnchantSettings().getEmeraldAnvilCost();
        int repairCost = BelaCustoms.get().getEnchantSettings().getRepairCostLevels();
        return GuiUtil.button(Material.ANVIL, ChatColor.GREEN, "✦ Forger ✦",
                GuiUtil.SEPARATOR,
                ChatColor.GRAY + "Enchanter : item Émeraude à gauche,",
                ChatColor.GRAY + "livre (custom ou vanilla) à droite.",
                ChatColor.YELLOW + "Coût" + ChatColor.WHITE + " " + enchantCost + " niveaux",
                "",
                ChatColor.GRAY + "Réparer : item à gauche,",
                ChatColor.GRAY + "Blocs d'Émeraude normaux à droite.",
                ChatColor.YELLOW + "Coût" + ChatColor.WHITE + " " + repairCost + " niveaux + blocs",
                GuiUtil.SEPARATOR);
    }
}
