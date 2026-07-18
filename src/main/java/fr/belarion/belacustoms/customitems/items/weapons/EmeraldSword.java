package fr.belarion.belacustoms.customitems.items.weapons;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.WeaponBonus;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EmeraldSword implements CustomItem, WeaponBonus, ExtraDurability {

    public static final String ID = "EMERALD_SWORD";
    private final double bonusDamage;
    private final ItemTextureRegistry textures;

    public EmeraldSword(ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.bonusDamage = stats.getSwordEmeraldBonusDamage();
        this.textures = textures;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public double getBonusDamage() {
        return bonusDamage;
    }

    @Override
    public double getDurabilityMultiplier() {
        return 1.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_SWORD, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Épée", false)
                .progression(false)
                .loreLine("&7Dégâts améliorés.")
                .tier(ItemTier.EMERALD)
                .customId(ID)
                .build();
    }
}
