package fr.belarion.belacustoms.customitems.config;

import org.bukkit.Material;

/**
 * Association Material + Damage Value (durability) utilisee comme
 * identifiant de texture par un resource pack Minecraft 1.8 (technique
 * standard pre-1.13 : l'API Bukkit 1.8 ne propose pas de CustomModelData,
 * donc chaque modele 3D distinct est associe a une valeur de "damage"
 * unique sur un materiau vanilla existant).
 */
public class ItemTexture {

    private final Material material;
    private final short durability;

    public ItemTexture(Material material, short durability) {
        this.material = material;
        this.durability = durability;
    }

    public Material getMaterial() {
        return material;
    }

    public short getDurability() {
        return durability;
    }
}
