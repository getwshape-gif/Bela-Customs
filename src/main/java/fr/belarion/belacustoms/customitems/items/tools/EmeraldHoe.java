package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.ReplantingHoe;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmeraldHoe implements CustomItem, ReplantingHoe, ExtraDurability {

    public static final String ID = "EMERALD_HOE";
    private final int radius;
    private final ItemTextureRegistry textures;

    public EmeraldHoe(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.radius = stats.getHoeEmeraldRadius();
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
        return 2.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_HOE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Houe", false)
                .progression(false)
                .loreLine("&7Récolte et replante automatiquement.")
                .tier(ItemTier.EMERALD)
                .customId(ID)
                .build();
    }
}
