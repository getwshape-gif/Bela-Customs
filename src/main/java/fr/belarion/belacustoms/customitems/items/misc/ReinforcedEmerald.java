package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Émeraude Renforcée : remplace l'ancien Bloc d'Émeraude Renforcé, qui
 * n'est plus un bloc posable (voir historique du plugin).
 *
 * Item de commerce PUR, sans aucune capacité spéciale : sert uniquement de
 * monnaie haut de gamme du serveur, pour les échanges avec les PNJ et
 * entre joueurs. Aucune mécanique supplémentaire (pas de réparation, pas
 * de custom enchant, pas de bonus quelconque, pas de ligne de
 * progression puisqu'il n'existe pas de version "non renforcée" de cet
 * objet).
 *
 * Le NBT CustomItemId permet de la distinguer d'une émeraude vanilla
 * classique (ex : pour des shops/PNJ qui ne doivent accepter que la
 * version "officielle" du serveur).
 */
public class ReinforcedEmerald implements CustomItem {

    public static final String ID = "REINFORCED_EMERALD";

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
        return new ItemBuilder(Material.EMERALD)
                .rawName("&e" + ItemBuilder.STAR + " &aÉmeraude &2Renforcée")
                .loreLine("&7Objet de valeur commerciale.")
                .customId(ID)
                .noCustomEnchantsTag()
                .build();
    }
}
