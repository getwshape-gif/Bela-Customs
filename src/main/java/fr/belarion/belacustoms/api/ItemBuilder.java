package fr.belarion.belacustoms.api;

import fr.belarion.belacustoms.utils.NBTEditor;
import fr.belarion.belacustoms.utils.ColorUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Construit les ItemStack des custom items en respectant le format visuel
 * impose par le cahier des charges :
 *
 *   Nom   : &e[ETOILE] &7Nom de l'item en &a/&2 (emeraude / emeraude renforcee)
 *   Lore  : description + ligne de progression + "&aCustom enchants autorises"
 *
 * Regle des items renforces : Unbreakable via NBT (pas de perte de durabilite),
 * appliquee automatiquement si unbreakable(true) est appele.
 */
public class ItemBuilder {

    public static final String STAR = "⭐"; // etoile jaune palissante (Unicode, pas de couleur fixe imposee au glyphe)

    private final Material material;
    private int amount = 1;
    private short durability = 0;
    private String displayName;
    /**
     * Chaque "section" devient un groupe de 1+ lignes de lore. Le build()
     * separe automatiquement chaque section par une ligne vide, pour un
     * rendu premium "espace" plutot qu'un bloc de texte compact.
     */
    private final List<List<String>> loreSections = new ArrayList<>();
    private boolean unbreakable = false;
    private String customId;
    private final List<EnchantEntry> enchants = new ArrayList<>();

    private static class EnchantEntry {
        Enchantment enchantment;
        int level;
        EnchantEntry(Enchantment e, int l) { this.enchantment = e; this.level = l; }
    }

    public ItemBuilder(Material material) {
        this.material = material;
    }

    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Definit le nom affiche a partir du "nom simple" de l'item (ex: "Hammer").
     * Format impose : &e[ETOILE] &7<nom> en &aEmeraude [&2Renforce]
     */
    public ItemBuilder emeraldName(String itemName, boolean reinforced) {
        StringBuilder name = new StringBuilder("&e").append(STAR).append(" &7").append(itemName).append(" en &aEmeraude");
        if (reinforced) {
            name.append(" &2Renforce");
        }
        this.displayName = ColorUtil.c(name.toString());
        return this;
    }

    public ItemBuilder rawName(String name) {
        this.displayName = ColorUtil.c(name);
        return this;
    }

    private List<String> newSection() {
        List<String> section = new ArrayList<>();
        loreSections.add(section);
        return section;
    }

    /** Ajoute une ligne isolee (sa propre section, entouree de lignes vides). */
    public ItemBuilder loreLine(String line) {
        newSection().add(ColorUtil.c(line));
        return this;
    }

    /** Ajoute plusieurs lignes regroupees dans une meme section (pas de ligne vide entre elles). */
    public ItemBuilder loreLines(String... lines) {
        List<String> section = newSection();
        for (String line : lines) {
            section.add(ColorUtil.c(line));
        }
        return this;
    }

    /**
     * Ajoute la ligne de progression standard :
     *   Emeraude    : &aEmeraude &7> &bDiamant
     *   Renforce    : &aEmeraude &2Renforce &7> &aEmeraude
     */
    public ItemBuilder progression(boolean reinforced) {
        if (reinforced) {
            loreLine("&aEmeraude &2Renforce &7> &aEmeraude");
        } else {
            loreLine("&aEmeraude &7> &bDiamant");
        }
        return this;
    }

    /** Ajoute la ligne "&aUnbreakable" imposee par le cahier des charges pour les items renforces. */
    public ItemBuilder unbreakableTag() {
        loreLine("&aUnbreakable");
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.enchants.add(new EnchantEntry(enchantment, level));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder customId(String id) {
        this.customId = id;
        return this;
    }

    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        if (durability != 0) {
            item.setDurability(durability);
        }

        ItemMeta meta = item.getItemMeta();
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        List<String> finalLore = new ArrayList<>();
        for (List<String> section : loreSections) {
            if (!finalLore.isEmpty()) {
                finalLore.add("");
            }
            finalLore.addAll(section);
        }
        if (!finalLore.isEmpty()) {
            finalLore.add("");
        }
        // Ligne finale obligatoire imposee par le cahier des charges
        finalLore.add(ColorUtil.c("&aCustom enchants autorises"));
        meta.setLore(finalLore);

        for (EnchantEntry entry : enchants) {
            meta.addEnchant(entry.enchantment, entry.level, true);
        }

        item.setItemMeta(meta);

        if (unbreakable) {
            item = NBTEditor.setUnbreakable(item);
        }
        if (customId != null) {
            item = NBTEditor.setCustomId(item, customId);
        }

        return item;
    }
}
