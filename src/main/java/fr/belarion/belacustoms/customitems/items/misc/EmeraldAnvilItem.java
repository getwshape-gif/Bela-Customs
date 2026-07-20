package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
* Version CustomItem de l'Enclume Emeraude : distribuable via /citem
* (registre + NBT CustomItemId + texture), au meme titre que n'importe
* quel autre custom item du plugin.
*
* Le declenchement (clic-droit sur un bloc Material.SEA_LANTERN) et le GUI
* restent geres tels quels par emeraldanvil.EmeraldAnvilListener /
* gui.emeraldanvil.EmeraldAnvilGUI (non modifies) : Bukkit 1.8 ne conserve
* aucun NBT sur un bloc plein une fois pose, donc poser cet item produit
* un SEA_LANTERN identique a n'importe quel autre, et le comportement
* (GUI, slots, boutons, messages, couts, reparation) reste strictement
* inchange (meme principe que pour la Table d'Enchantement Emeraude, voir
* EmeraldEnchantTableItem).
*/
public class EmeraldAnvilItem implements CustomItem {

    public static final String ID = "EMERALD_ANVIL";
    private final ItemTextureRegistry textures;

    public EmeraldAnvilItem(ItemTextureRegistry textures) {
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
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.SEA_LANTERN, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .rawName("&2&lEnclume Émeraude")
                .customId(ID)
                .noCustomEnchantsTag()
                .build();
    }
}
