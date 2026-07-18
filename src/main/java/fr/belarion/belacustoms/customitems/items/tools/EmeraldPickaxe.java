package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Pioche emeraude : plus rapide qu'une pioche diamant (Efficacite I offerte
 * de base, cumulable ensuite normalement via table d'enchantement/enclume
 * jusqu'au niveau V vanilla) et plus grande duree de vie effective.
 */
public class EmeraldPickaxe implements CustomItem, ExtraDurability {

    public static final String ID = "EMERALD_PICKAXE";
    private final ItemTextureRegistry textures;

    public EmeraldPickaxe(ItemTextureRegistry textures) {
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
    public double getDurabilityMultiplier() {
        return 1.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_PICKAXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Pioche", false)
                .progression(false)
                .loreLine("&7Minage ameliore.")
                .enchant(Enchantment.DIG_SPEED, 1)
                .customId(ID)
                .build();
    }
}
