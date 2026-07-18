package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.MiningTool;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmeraldShovel implements CustomItem, MiningTool, ExtraDurability {

    public static final String ID = "EMERALD_SHOVEL";
    private final int radius;
    private final ItemTextureRegistry textures;

    public EmeraldShovel(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.radius = stats.getShovelEmeraldRadius();
        this.textures = textures;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public double getDurabilityMultiplier() {
        return 1.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_SPADE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Pelle", false)
                .progression(false)
                .loreLine("&7Mine les blocs en zone &a3x3&7.")
                .customId(ID)
                .build();
    }
}
