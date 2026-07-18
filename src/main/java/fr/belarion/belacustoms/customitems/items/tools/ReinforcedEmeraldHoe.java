package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.ReplantingHoe;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReinforcedEmeraldHoe implements CustomItem, ReplantingHoe {

    public static final String ID = "REINFORCED_EMERALD_HOE";
    private final int radius;
    private final ItemTextureRegistry textures;

    public ReinforcedEmeraldHoe(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.radius = stats.getHoeReinforcedRadius();
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
        return true;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_HOE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Houe", true)
                .progression(true)
                .loreLine("&7Recolte en zone &a3x3&7.")
                .unbreakableTag()
                .unbreakable(true)
                .tier(ItemTier.EMERALD_RENFORCE)
                .customId(ID)
                .build();
    }
}
