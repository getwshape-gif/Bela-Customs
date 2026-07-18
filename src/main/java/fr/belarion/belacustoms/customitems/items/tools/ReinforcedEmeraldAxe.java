package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.api.TreeFeller;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Hache emeraude renforcee : "tree capitator". Casser une seule buche coupe
 * l'integralite de l'arbre (toutes les buches connectees), voir ItemMiningListener.
 */
public class ReinforcedEmeraldAxe implements CustomItem, TreeFeller {

    public static final String ID = "REINFORCED_EMERALD_AXE";
    private final ItemTextureRegistry textures;

    public ReinforcedEmeraldAxe(ItemTextureRegistry textures) {
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
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_AXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Hache", true)
                .progression(true)
                .loreLine("&7Coupe les arbres entièrement.")
                .unbreakableTag()
                .unbreakable(true)
                .tier(ItemTier.EMERALD_RENFORCE)
                .customId(ID)
                .build();
    }
}
