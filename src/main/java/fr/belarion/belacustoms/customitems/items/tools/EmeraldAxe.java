package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.MiningTool;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * La hache emeraude casse le bois en colonne 1x3 (le bloc vise + les 2
 * au-dessus dans l'axe vertical de la buche), utile pour degager rapidement
 * un tronc sans abattre tout l'arbre (reserve a la version renforcee).
 */
public class EmeraldAxe implements CustomItem, MiningTool, ExtraDurability {

    public static final String ID = "EMERALD_AXE";
    private final int columnLength;
    private final ItemTextureRegistry textures;

    public EmeraldAxe(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.columnLength = stats.getAxeEmeraldLength();
        this.textures = textures;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getRadius() {
        return columnLength;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public double getDurabilityMultiplier() {
        return 2.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_AXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Hache", false)
                .progression(false)
                .loreLine("&7Coupe le bois en zone &a1x3&7.")
                .tier(ItemTier.EMERALD)
                .customId(ID)
                .build();
    }
}
