package fr.belarion.belacustoms.customenchants;

import org.bukkit.inventory.ItemStack;

/**
 * Catégories de compatibilité utilisées par tous les Custom Enchants.
 * Centralise la logique "cet item peut-il recevoir cet enchant" par type.
 */
public enum EnchantTarget {

    BOOTS("Bottes"),
    ARMOR("Toute armure"),
    SWORD("Épée"),
    PICKAXE("Pioche"),
    TOOLS("Tous les outils"),
    TOOLS_AND_WEAPONS("Tous les outils et armes"),
    ALL_EQUIPMENT("Tout équipement");

    private final String label;

    EnchantTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(ItemStack item) {
        if (item == null) return false;
        String m = item.getType().name();
        switch (this) {
            case BOOTS:
                return m.endsWith("_BOOTS");
            case ARMOR:
                return m.endsWith("_BOOTS") || m.endsWith("_HELMET")
                        || m.endsWith("_CHESTPLATE") || m.endsWith("_LEGGINGS");
            case SWORD:
                return m.endsWith("_SWORD");
            case PICKAXE:
                return m.endsWith("_PICKAXE");
            case TOOLS:
                return m.endsWith("_PICKAXE") || m.endsWith("_SPADE")
                        || m.endsWith("_HOE") || m.endsWith("_AXE");
            case TOOLS_AND_WEAPONS:
                return m.endsWith("_PICKAXE") || m.endsWith("_SPADE") || m.endsWith("_HOE")
                        || m.endsWith("_AXE") || m.endsWith("_SWORD");
            case ALL_EQUIPMENT:
                return m.endsWith("_BOOTS") || m.endsWith("_HELMET") || m.endsWith("_CHESTPLATE")
                        || m.endsWith("_LEGGINGS") || m.endsWith("_SWORD") || m.endsWith("_PICKAXE")
                        || m.endsWith("_SPADE") || m.endsWith("_HOE") || m.endsWith("_AXE");
            default:
                return false;
        }
    }
}
