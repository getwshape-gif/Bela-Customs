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
 * Le declenchement (clic-droit sur un bloc Prismarine avec data 2, c'est-a-dire
 * Dark Prismarine) et le GUI restent geres tels quels par
 * emeraldenchanttable.EnchantTableListener / gui.emeraldenchanttable.EnchantTableGUI
 * (non modifies).
 *
 * IMPORTANT : ItemBuilder.durability() ne touche plus la vraie valeur NBT
 * "Damage" de l'ItemStack (voir ItemBuilder, correction anti-durabilite
 * negative) : l'appeler ici n'aurait donc aucun effet. Pour ce bloc
 * precisement, la valeur de data (2 = Dark Prismarine) DOIT etre reellement
 * appliquee pour que le bloc pose corresponde au declencheur attendu par
 * EnchantTableListener -- on l'applique donc explicitement via
 * setDurability() sur l'ItemStack final, apres la construction complete
 * (NBT/tier/etc compris).
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
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.PRISMARINE, (short) 2));
        ItemStack item = new ItemBuilder(tex.getMaterial())
                .rawName("&2&lTable d'Enchantement Émeraude")
                .customId(ID)
                .noCustomEnchantsTag()
                .build();
        item.setDurability(tex.getDurability());
        return item;
    }
}
