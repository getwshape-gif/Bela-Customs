package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Item de commerce sans capacite particuliere : sert de monnaie/objet de
 * valeur pour l'economie du serveur. Le NBT CustomItemId permet de le
 * distinguer d'un bloc d'emeraude vanilla classique (ex: pour des shops
 * qui ne doivent accepter que la version "officielle" du serveur).
 */
public class ReinforcedEmeraldBlock implements CustomItem {

    public static final String ID = "REINFORCED_EMERALD_BLOCK";

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
        return new ItemBuilder(Material.EMERALD_BLOCK)
                .emeraldName("Bloc d'emeraude renforce", true)
                .progression(true)
                .loreLine("&7Objet de valeur commerciale.")
                .customId(ID)
                .build();
    }
}
