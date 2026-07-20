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
* Bloc de reference : Prismarine:2 (Dark Prismarine), configure dans
* custom-items.yml (textures.EMERALD_ENCHANT_TABLE) — remplace l'ancien
* Bloc d'Emeraude.
*
* Le declenchement (clic-droit) reste gere par
* emeraldenchanttable.EnchantTableListener / gui.emeraldenchanttable.
* EnchantTableGUI (mis a jour pour reconnaitre Material.PRISMARINE avec
* data 2, a la place de Material.EMERALD_BLOCK) : Bukkit 1.8 ne conserve
* aucun NBT sur un bloc plein une fois pose, donc poser cet item produit
* un PRISMARINE (data 2) identique a n'importe quel autre, et le
* comportement (GUI, slots, boutons, messages, couts) reste strictement
* inchange.
*
* Exception a la regle generale ItemBuilder (qui ignore desormais le champ
* "durability", voir ItemBuilder.durability()) : Prismarine est un BLOC
* sans usure reelle (getMaxDurability() == 0), donc sa Damage Value
* represente ici une vraie variante vanilla (2 = Dark Prismarine), jamais
* un identifiant de texture recycle. Elle est donc appliquee explicitement
* ci-dessous via ItemStack.setDurability(), en plus de la regle OptiFine
* CIT (voir emerald_enchant_table.properties) qui gere l'affichage cote
* resource pack via le NBT CustomItemId.
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
                .durability(tex.getDurability())
                .rawName("&2&lTable d'Enchantement Émeraude")
                .customId(ID)
                .noCustomEnchantsTag()
                .build();
        // Voir javadoc de classe : Prismarine n'a pas d'usure reelle, donc
        // sa Damage Value peut etre appliquee sans risque de durabilite
        // negative (contrairement aux outils/armures, ou ce meme champ
        // reste volontairement ignore par ItemBuilder.build()).
        item.setDurability(tex.getDurability());
        return item;
    }
}
