package fr.belarion.belacustoms.customenchants;

import fr.belarion.belacustoms.utils.HiddenTag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gere le stockage multi-enchants sur un item : un item peut porter
 * plusieurs Custom Enchants en meme temps (ex : Speed + Strength + No Fall).
 *
 * Une seule source de verite : le tag cache "belenchs" (liste d'ids separes
 * par ";"). Les lignes de lore visibles sont entierement regenerees a
 * chaque modification a partir de cette liste : jamais de duplication
 * possible, jamais de derive entre lore visible et donnees reelles.
 */
public final class EnchantStorage {

    public static final String TAG_KEY = "belenchs";
    private static final String SEPARATOR = ";";

    private EnchantStorage() {}

    public static List<CustomEnchant> getEnchants(ItemStack item) {
        String raw = HiddenTag.read(item, TAG_KEY);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<CustomEnchant> list = new ArrayList<CustomEnchant>();
        for (String id : raw.split(SEPARATOR)) {
            CustomEnchant enchant = CustomEnchant.fromId(id);
            if (enchant != null && !list.contains(enchant)) {
                list.add(enchant);
            }
        }
        return list;
    }

    public static boolean hasEnchant(ItemStack item, CustomEnchant enchant) {
        return getEnchants(item).contains(enchant);
    }

    /**
     * Ajoute un Custom Enchant a l'item. Retourne false sans rien modifier
     * si l'item possede deja cet enchant (validation stricte anti-doublon).
     */
    public static boolean addEnchant(ItemStack item, CustomEnchant enchant) {
        List<CustomEnchant> current = new ArrayList<CustomEnchant>(getEnchants(item));
        if (current.contains(enchant)) return false;
        current.add(enchant);
        writeAll(item, current);
        return true;
    }

    /** Reecrit entierement le tag + les lignes de lore visibles a partir de la liste donnee. */
    private static void writeAll(ItemStack item, List<CustomEnchant> enchants) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList<String>();

        // Retire uniquement l'ancien tag cache "belenchs" et les anciennes lignes visibles
        // de custom enchants. Les AUTRES tags caches (ex: beltier) sont preserves intacts.
        List<String> cleaned = new ArrayList<String>();
        for (String line : lore) {
            if (HiddenTag.matchesKey(line, TAG_KEY)) continue;
            if (isEnchantTagLine(line)) continue;
            cleaned.add(line);
        }

        for (CustomEnchant enchant : enchants) {
            cleaned.add(enchant.getTagLine());
        }

        meta.setLore(cleaned);
        item.setItemMeta(meta);

        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < enchants.size(); i++) {
            if (i > 0) ids.append(SEPARATOR);
            ids.append(enchants.get(i).getId());
        }
        HiddenTag.write(item, TAG_KEY, ids.toString());
    }

    private static boolean isEnchantTagLine(String line) {
        for (CustomEnchant enchant : CustomEnchant.values()) {
            if (line.equals(enchant.getTagLine())) return true;
        }
        return false;
    }
}
