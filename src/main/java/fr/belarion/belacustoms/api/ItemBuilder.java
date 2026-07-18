package fr.belarion.belacustoms.api;

import fr.belarion.belacustoms.utils.NBTEditor;
import fr.belarion.belacustoms.utils.ColorUtil;
import fr.belarion.belacustoms.utils.ItemTier;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Construit les ItemStack des custom items en respectant le format visuel
 * impose par le cahier des charges :
 *
 *   Nom   : &e[ETOILE] &7Nom de l'item en &a/&2 (Émeraude / Émeraude Renforcé)
 *   Lore  : description + ligne de progression + "&d✔ Custom enchants"
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
    private ItemTier tier;
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

    /**
     * NOTE (correction durabilite) : cette valeur n'est PLUS appliquee au tag
     * "Damage" reel de l'ItemStack (voir build()). Elle etait auparavant
     * utilisee comme identifiant de texture pour un resource pack, mais
     * partageait par erreur le meme slot NBT que la durabilite reelle de
     * l'item, ce qui provoquait des items generes avec une durabilite
     * negative (ex: -600) des lors que la valeur depassait la durabilite
     * maximale reelle du Material (particulierement les armures). Conservee
     * ici uniquement pour compatibilite avec les appels existants
     * (.durability(tex.getDurability())) ; n'a plus d'effet sur build().
     */
    public ItemBuilder durability(short durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Definit le nom affiche a partir du "nom simple" de l'item (ex: "Hammer").
     * Format impose : &e[ETOILE] &7<nom> en &aÉmeraude [&2Renforcé]
     */
    public ItemBuilder emeraldName(String itemName, boolean reinforced) {
        StringBuilder name = new StringBuilder("&e").append(STAR).append(" &7").append(itemName).append(" en &aÉmeraude");
        if (reinforced) {
            name.append(" &2Renforcé");
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
     *   Émeraude    : ◆ &aÉmeraude &7> &bDiamant
     *   Renforcé    : ◆ &aÉmeraude &2Renforcé &7> &aÉmeraude
     */
    public ItemBuilder progression(boolean reinforced) {
        if (reinforced) {
            loreLine("◆ &aÉmeraude &2Renforcé &7> &aÉmeraude");
        } else {
            loreLine("◆ &aÉmeraude &7> &bDiamant");
        }
        return this;
    }

    /**
     * Ajoute la ligne "✓ Unbreakable" imposee par le cahier des charges pour
     * les items renforces. L'affichage automatique vanilla (en bleu) est
     * masque via ItemFlag.HIDE_UNBREAKABLE dans build(), afin que cette ligne
     * personnalisee soit la seule visible.
     */
    public ItemBuilder unbreakableTag() {
        loreLine("&a✓ Unbreakable");
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

    /**
     * Marque l'item avec un tier Émeraude (voir ItemTier / ItemTierUtil).
     * C'est ce tag, caché en NBT/lore, que lit l'Enclume Émeraude pour
     * accepter l'item et que le système de Custom Enchants (EffectManager,
     * MiningListener, CombatListener, etc.) lit pour savoir si un item est
     * éligible aux mécaniques d'enchants. Sans cet appel, un custom item
     * n'est jamais reconnu comme "tier Émeraude" par ces systèmes.
     */
    public ItemBuilder tier(ItemTier tier) {
        this.tier = tier;
        return this;
    }

    public ItemStack build() {
        // CORRECTION DURABILITE : on ne touche plus jamais au tag "Damage"
        // reel de l'item ici. Bukkit 1.8 ne connait qu'un seul et meme slot
        // NBT pour a la fois (a) l'identifiant de texture resource pack et
        // (b) la durabilite reelle consommee par le joueur : les reutiliser
        // pour la texture provoquait des items obtenus avec une durabilite
        // deja negative. Tout custom item doit desormais toujours naitre a
        // Damage=0, c'est-a-dire durabilite maximale/complete.
        ItemStack item = new ItemStack(material, amount);

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
        finalLore.add(ColorUtil.c("&d✔ Custom enchants"));
        meta.setLore(finalLore);

        for (EnchantEntry entry : enchants) {
            meta.addEnchant(entry.enchantment, entry.level, true);
        }

        if (unbreakable) {
            // Masque l'affichage automatique vanilla ("Unbreakable" en bleu) :
            // seule la ligne personnalisee "&a✓ Unbreakable" du lore doit
            // apparaitre. La fonctionnalite (item reellement incassable)
            // reste intacte, ce flag ne touche que le rendu du tooltip.
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        item.setItemMeta(meta);

        if (tier != null) {
            ItemTierUtil.setTier(item, tier);
        }

        if (unbreakable) {
            item = NBTEditor.setUnbreakable(item);
        }
        if (customId != null) {
            item = NBTEditor.setCustomId(item, customId);
        }

        return item;
    }
}
