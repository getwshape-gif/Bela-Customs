package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Item de commerce sans capacité particulière : sert de monnaie/objet de
 * valeur pour l'économie du serveur. Le NBT CustomItemId permet de le
 * distinguer d'un bloc d'émeraude vanilla classique (ex: pour des shops
 * qui ne doivent accepter que la version "officielle" du serveur), et sert
 * aussi a l'exclure explicitement du systeme de reparation de l'Enclume
 * Emeraude (voir EmeraldAnvilListener) : seuls des Blocs d'Émeraude
 * NORMAUX (sans ce tag) sont acceptes comme monnaie de reparation.
 *
 * Résistance aux explosions : une fois posé, ce bloc est un EMERALD_BLOCK
 * vanilla classique (Bukkit 1.8 ne conserve pas le NBT d'un bloc plein), il
 * bénéficie donc automatiquement de la protection anti-explosion/piston déjà
 * appliquée à tout EMERALD_BLOCK par BlockProtectionListener, tout en restant
 * cassable normalement à la pioche (jamais totalement indestructible).
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
                .emeraldName("Bloc d'émeraude renforcé", true)
                .progression(true)
                .loreLine("&7Objet de valeur commerciale.")
                .customId(ID)
                .build();
    }
}
