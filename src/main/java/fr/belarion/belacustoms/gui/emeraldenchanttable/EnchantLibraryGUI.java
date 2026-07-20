package fr.belarion.belacustoms.gui.emeraldenchanttable;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.gui.GuiUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Bibliothèque d'Enchants : liste absolument tous les Custom Enchants
 * disponibles avec nom / description / compatibilité / effet.
 *
 * Disposition fixe et volontairement centrée : chaque page peut contenir
 * jusqu'à 14 custom enchants, placés exclusivement dans les slots
 * 10,11,12,13,14,15,16,19,20,21,22,23,24,25. Les boutons de navigation
 * (9 = page précédente, 26 = page suivante, en bas à droite) restent en
 * dehors de cette zone d'affichage afin qu'elle ne soit jamais amputée.
 * Cette disposition
 * est identique sur TOUTES les pages, y compris les futures : ajouter un
 * 15e enchant dans CustomEnchant remplit simplement la page courante puis,
 * une fois pleine, crée automatiquement une page suivante qui reprend
 * exactement le même gabarit, sans aucun changement de code nécessaire ici.
 */
public final class EnchantLibraryGUI {

    public static final String TITLE = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "Bibliothèque d'Enchants";

    public static final int SLOT_BACK = 4;
    public static final int SLOT_PREV = 9;
    public static final int SLOT_NEXT = 26;

    /**
     * Disposition fixe et définitive des enchants, identique sur toutes les
     * pages : 14 slots exactement, jamais plus, jamais moins. Décalée d'une
     * colonne vers la gauche pour laisser la colonne de droite entièrement
     * libre : la flèche "page suivante" (SLOT_NEXT) y est ancrée en bas à
     * droite, symétrique de la flèche "page précédente" en bas à gauche.
     */
    private static final int[] DISPLAY_SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
    };

    private EnchantLibraryGUI() {}

    public static Inventory build(int page) {
        CustomEnchant[] all = CustomEnchant.values();
        int pageSize = DISPLAY_SLOTS.length;
        int maxPage = Math.max(0, (all.length - 1) / pageSize);
        if (page < 0) page = 0;
        if (page > maxPage) page = maxPage;

        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        // Slot 17 : vitre situee juste au-dessus de la fleche "page suivante"
        // (slot 26) ; ajoutee ici en accent vert pour correspondre exactement
        // aux autres vitres vertes du GUI (elle restait auparavant grise,
        // seule vitre non accentuee de tout le fond premium).
        GuiUtil.fillPremiumBackground(inv, new int[]{0, 8, 9, 17, 26});

        ItemStack back = GuiUtil.button(Material.ARROW, ChatColor.WHITE, "Retour",
                GuiUtil.SEPARATOR, ChatColor.GRAY + "Retour à la table.", GuiUtil.SEPARATOR);
        // La quantité de ce bouton encode discrètement la page actuelle (page + 1)
        // pour permettre au listener de calculer suivant/précédent sans état externe.
        back.setAmount(page + 1);
        inv.setItem(SLOT_BACK, back);

        int start = page * pageSize;
        int count = Math.min(pageSize, all.length - start);
        for (int i = 0; i < count; i++) {
            inv.setItem(DISPLAY_SLOTS[i], buildDisplayItem(all[start + i]));
        }

        // Flèche "Page suivante" : toujours présente, même s'il n'existe
        // qu'une seule page (système prêt pour les futurs enchants).
        // Grisée/inactive tant qu'il n'y a rien après, verte et
        // fonctionnelle dès qu'une page suivante existe.
        boolean hasNext = start + pageSize < all.length;
        inv.setItem(SLOT_NEXT, buildNextButton(hasNext));

        // Flèche "Page précédente" : n'apparaît que lorsqu'il existe
        // effectivement une page antérieure.
        if (page > 0) {
            inv.setItem(SLOT_PREV, GuiUtil.button(Material.ARROW, ChatColor.GREEN, "« Page précédente",
                    GuiUtil.SEPARATOR));
        }

        return inv;
    }

    private static ItemStack buildNextButton(boolean enabled) {
        if (enabled) {
            return GuiUtil.button(Material.ARROW, ChatColor.GREEN, "Page suivante »",
                    GuiUtil.SEPARATOR);
        }
        return GuiUtil.button(Material.ARROW, ChatColor.GRAY, "Page suivante",
                GuiUtil.SEPARATOR, ChatColor.DARK_GRAY + "Aucune page suivante.", GuiUtil.SEPARATOR);
    }

    private static ItemStack buildDisplayItem(CustomEnchant enchant) {
        ItemStack item = new ItemStack(enchant.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(enchant.getColor().toString() + ChatColor.BOLD + enchant.getDisplayName());
        meta.setLore(GuiUtil.buildEnchantLore(enchant));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isDisplaySlot(int slot) {
        for (int s : DISPLAY_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }
}
