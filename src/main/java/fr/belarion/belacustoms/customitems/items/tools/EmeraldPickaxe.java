package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Pioche emeraude : plus rapide qu'une pioche diamant et plus grande duree
 * de vie effective.
 *
 * Donnee sans aucun enchantement de base (voir demande du 20/07/2026) :
 * l'item reste normalement enchantable ensuite via table d'enchantement,
 * enclume ou livres, jusqu'au niveau V vanilla comme n'importe quel autre
 * outil.
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
        return 2.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_PICKAXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Pioche", false)
                .progression(false)
                .loreLine("&7Minage amélioré.")
                .tier(ItemTier.EMERALD)
                .customId(ID)
                .build();
    }
}
