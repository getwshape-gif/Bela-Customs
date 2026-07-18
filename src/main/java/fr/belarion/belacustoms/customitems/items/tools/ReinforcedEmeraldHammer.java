package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.MiningTool;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReinforcedEmeraldHammer implements CustomItem, MiningTool {

    public static final String ID = "REINFORCED_EMERALD_HAMMER";
    private final int radius;
    private final ItemTextureRegistry textures;

    public ReinforcedEmeraldHammer(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.radius = stats.getHammerReinforcedRadius();
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
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_PICKAXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Hammer", true)
                .progression(true)
                .loreLine("&7Mine les blocs en zone &a5x5&7.")
                .unbreakableTag()
                .unbreakable(true)
                .customId(ID)
                .build();
    }
}
