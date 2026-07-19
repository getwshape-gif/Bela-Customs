package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
* Version CustomItem de la Table d'Enchantement Emeraude : distribuable via
    * /citem (registre + NBT CustomItemId + texture), au meme titre que
    * n'importe quel autre custom item du plugin.
    *
    * Le declenchement (clic-droit sur un bloc Material.EMERALD_BLOCK) et le
    * GUI restent geres tels quels par emeraldenchanttable.EnchantTableListener
    * / gui.emeraldenchanttable.EnchantTableGUI (non modifies) : Bukkit 1.8 ne
    * conserve aucun NBT sur un bloc plein une fois pose, donc poser cet item
    * produit un EMERALD_BLOCK identique a n'importe quel autre, et le
    * comportement (GUI, slots, boutons, messages, couts) reste strictement
    * inchange - voir ReinforcedEmeraldBlock pour la meme remarque.
    */
public class EmeraldEnchantTableItem implements CustomItem {

    public static final String ID = "EMERALD_ENCHANT_TABLE";
    private final ItemTextureRegistry textures;

public EmeraldEnchantTableItem(ItemTextureRegistry textures) {
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
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.EMERALD_BLOCK, (short) 0));
        return new ItemBuilder(tex.getMaterial())
            .durability(tex.getDurability())
            .rawName("&2&lTable d'Enchantement Émeraude")
            .customId(ID)
            .noCustomEnchantsTag()
            .build();
    }
}
