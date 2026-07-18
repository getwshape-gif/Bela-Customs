package fr.belarion.belacustoms.customenchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * Registre de tous les Custom Enchants du plugin.
 *
 * Pour ajouter un nouvel enchant : il suffit d'ajouter une entrée ici avec
 * ses métadonnées (nom, description, cible, effet). Le stockage multi-enchant
 * (EnchantStorage), les GUI (table/bibliothèque) et l'enclume émeraude lisent
 * automatiquement CustomEnchant.values() : aucune autre classe n'a besoin
 * d'être modifiée pour un enchant "passif" (potion effect géré par
 * EffectManager) ou "vanilla-equivalent" (Fortune/Looting/Unbreaking). Un
 * enchant avec une mécanique très spécifique (Vein Miner, AutoSmelt,
 * Aimantation, Anti Rod, No Fall) nécessite en plus un petit bout de logique
 * dans le listener concerné, identifié via son id().
 */
public enum CustomEnchant {

    SPEED(
            "speed", "Speed", ChatColor.AQUA, Material.DIAMOND_BOOTS,
            new String[]{"Permet au porteur", "de courir beaucoup", "plus rapidement."},
            EnchantTarget.BOOTS,
            new String[]{"Speed II permanent"}
    ),
    STRENGTH(
            "strength", "Strength", ChatColor.RED, Material.DIAMOND_CHESTPLATE,
            new String[]{"Augmente la force", "d'attaque du porteur", "en permanence."},
            EnchantTarget.ARMOR,
            new String[]{"Strength I permanent"}
    ),
    FIRE_RESISTANCE(
            "fire_resistance", "Fire Resistance", ChatColor.GOLD, Material.BLAZE_POWDER,
            new String[]{"Rend le porteur", "totalement immunisé", "contre le feu et la lave."},
            EnchantTarget.ARMOR,
            new String[]{"Fire Resistance permanent"}
    ),
    NO_FALL(
            "no_fall", "No Fall", ChatColor.GRAY, Material.FEATHER,
            new String[]{"Absorbe la totalité", "des dégâts de chute,", "quelle que soit la hauteur."},
            EnchantTarget.BOOTS,
            new String[]{"Aucun dégât de chute"}
    ),
    ANTI_ROD(
            "anti_rod", "Anti Rod", ChatColor.DARK_AQUA, Material.FISHING_ROD,
            new String[]{"Empêche le joueur d'être", "attiré ou déséquilibré", "par une canne à pêche."},
            EnchantTarget.ARMOR,
            new String[]{"Immunité totale au knockback", "provoqué par une canne à pêche"}
    ),
    ANTI_DEBUFF(
            "anti_debuff", "Anti Debuff", ChatColor.LIGHT_PURPLE, Material.MILK_BUCKET,
            new String[]{"Le porteur devient", "immunisé contre tous", "les effets négatifs."},
            EnchantTarget.ARMOR,
            new String[]{"Immunité : Poison, Slowness,", "Weakness, Blindness, Wither,", "Mining Fatigue, Hunger"}
    ),
    HASTE_II(
            "haste2", "Haste II", ChatColor.YELLOW, Material.GOLD_PICKAXE,
            new String[]{"Augmente la vitesse", "de minage tant que", "l'outil est en main."},
            EnchantTarget.TOOLS,
            new String[]{"Haste II en main"}
    ),
    MAGNET(
            "magnet", "Aimantation", ChatColor.DARK_PURPLE, Material.NETHER_STAR,
            new String[]{"Les ressources et les", "butins sont envoyés", "directement dans l'inventaire."},
            EnchantTarget.TOOLS_AND_WEAPONS,
            new String[]{"Envoi automatique des drops", "en inventaire (sinon au sol)"}
    ),
    AUTOSMELT(
            "autosmelt", "AutoSmelt", ChatColor.RED, Material.FURNACE,
            new String[]{"Cuit automatiquement", "les minerais minés."},
            EnchantTarget.PICKAXE,
            new String[]{"Fer et Or minés sont", "directement cuits en lingots"}
    ),
    VEIN_MINER(
            "vein_miner", "Vein Miner", ChatColor.DARK_GREEN, Material.DIAMOND_PICKAXE,
            new String[]{"Casse instantanément", "tout le filon de minerai", "connecté."},
            EnchantTarget.PICKAXE,
            new String[]{"Casse le filon de minerai", "complet (jamais les autres blocs)"}
    ),
    GEM_HUNTER(
            "gem_hunter", "Gem Hunter", ChatColor.GREEN, Material.EMERALD,
            new String[]{"Multiplie grandement", "les gains de minerais."},
            EnchantTarget.PICKAXE,
            new String[]{"Équivalent Fortune IV"},
            Enchantment.LOOT_BONUS_BLOCKS, 4
    ),
    SOUL_COLLECTOR(
            "soul_collector", "Soul Collector", ChatColor.DARK_RED, Material.SKULL_ITEM,
            new String[]{"Les âmes vaincues", "abandonnent davantage", "de butin."},
            EnchantTarget.SWORD,
            new String[]{"Équivalent Looting IV"},
            Enchantment.LOOT_BONUS_MOBS, 4
    ),
    ETERNAL(
            "eternal", "Eternal", ChatColor.WHITE, Material.NETHER_STAR,
            new String[]{"Renforce la durabilité", "de l'équipement de", "façon quasi infinie."},
            EnchantTarget.ALL_EQUIPMENT,
            new String[]{"Équivalent Unbreaking IV"},
            Enchantment.DURABILITY, 4
    ),
    XP_BOOST(
            "xp_boost", "XP Boost", ChatColor.GREEN, Material.EXP_BOTTLE,
            new String[]{"Augmente l'expérience", "obtenue en tuant", "les créatures."},
            EnchantTarget.SWORD,
            new String[]{"+100% d'expérience obtenue", "(XP vanilla × 2)"}
    );

    private final String id;
    private final String displayName;
    private final ChatColor color;
    private final Material icon;
    private final String[] description;
    private final EnchantTarget target;
    private final String[] effectLines;
    private final Enchantment vanillaEquivalent;
    private final int vanillaLevel;

    CustomEnchant(String id, String displayName, ChatColor color, Material icon, String[] description,
                  EnchantTarget target, String[] effectLines) {
        this(id, displayName, color, icon, description, target, effectLines, null, 0);
    }

    CustomEnchant(String id, String displayName, ChatColor color, Material icon, String[] description,
                  EnchantTarget target, String[] effectLines, Enchantment vanillaEquivalent, int vanillaLevel) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.description = description;
        this.target = target;
        this.effectLines = effectLines;
        this.vanillaEquivalent = vanillaEquivalent;
        this.vanillaLevel = vanillaLevel;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public ChatColor getColor() { return color; }
    public Material getIcon() { return icon; }
    public String[] getDescription() { return description; }
    public EnchantTarget getTarget() { return target; }
    public String[] getEffectLines() { return effectLines; }

    /** Non-null si cet enchant est un simple alias d'un enchant vanilla (Fortune/Looting/Unbreaking). */
    public Enchantment getVanillaEquivalent() { return vanillaEquivalent; }
    public int getVanillaLevel() { return vanillaLevel; }
    public boolean isVanillaEquivalent() { return vanillaEquivalent != null; }

    /** Ligne de lore visible représentant cet enchant appliqué sur un item. */
    public String getTagLine() {
        return color.toString() + ChatColor.BOLD + "✦ " + displayName;
    }

    public static CustomEnchant fromId(String id) {
        if (id == null) return null;
        for (CustomEnchant e : values()) {
            if (e.id.equalsIgnoreCase(id)) return e;
        }
        return null;
    }
}
