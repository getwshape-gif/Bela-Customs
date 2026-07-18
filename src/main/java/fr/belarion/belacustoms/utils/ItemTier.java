package fr.belarion.belacustoms.utils;

import org.bukkit.ChatColor;

/**
 * Tiers d'items compatibles avec les Custom Enchants.
 * Un custom enchant ne fonctionne JAMAIS sur du diamant, fer ou netherite :
 * uniquement sur du stuff marque avec un de ces tiers (voir ItemTierUtil).
 */
public enum ItemTier {

    EMERALD("emerald", "Émeraude", ChatColor.GREEN),
    EMERALD_RENFORCE("emerald_renforce", "Émeraude Renforcé", ChatColor.DARK_GREEN);

    private final String id;
    private final String label;
    private final ChatColor color;

    ItemTier(String id, String label, ChatColor color) {
        this.id = id;
        this.label = label;
        this.color = color;
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public ChatColor getColor() { return color; }

    public static ItemTier fromId(String id) {
        if (id == null) return null;
        for (ItemTier t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
