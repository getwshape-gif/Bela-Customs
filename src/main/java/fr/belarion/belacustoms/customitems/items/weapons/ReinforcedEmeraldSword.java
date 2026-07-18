package fr.belarion.belacustoms.customitems.items.weapons;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.WeaponBonus;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReinforcedEmeraldSword implements CustomItem, WeaponBonus {

    public static final String ID = "REINFORCED_EMERALD_SWORD";
    private final double bonusDamage;
    private final ItemTextureRegistry textures;

    public ReinforcedEmeraldSword(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.bonusDamage = stats.getSwordReinforcedBonusDamage();
        this.textures = textures;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isUnbreakable() {
        return true;
    }

    @Override
    public double getBonusDamage() {
        return bonusDamage;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_SWORD, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Epee", true)
                .progression(true)
                .loreLine("&7Degats superieurs.")
                .unbreakableTag()
                .unbreakable(true)
                .customId(ID)
                .build();
    }
}
