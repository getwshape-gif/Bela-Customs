package fr.belarion.belacustoms.customitems.items.misc;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Coffre en Émeraude : apparence d'un coffre simple, mais avec un inventaire
 * virtuel de 54 slots (capacité double coffre) une fois posé — voir
 * fr.belarion.belacustoms.emeraldchest (EmeraldChestManager /
 * EmeraldChestListener) pour toute la logique de pose/ouverture/persistance.
 *
 * Choix de Material.TRAPPED_CHEST (jamais Material.CHEST) : ce sont deux
 * Materials distincts en 1.8 qui ne fusionnent JAMAIS entre eux, ce qui
 * garantit qu'un Coffre en Émeraude ne se combine jamais visuellement avec
 * un coffre vanilla normal posé à côté. La fusion avec un AUTRE Coffre en
 * Émeraude adjacent est elle empêchée au niveau fonctionnel : le vrai bloc
 * posé n'est jamais ouvert via le chemin vanilla (EmeraldChestListener
 * intercepte systématiquement le clic droit), donc son vrai tile entity
 * reste toujours vide et chaque Coffre en Émeraude garde un inventaire
 * totalement indépendant, quel que soit ce qui est posé à côté.
 *
 * Texture : utilise pour l'instant le modèle vanilla de Trapped Chest, sans
 * assigner de valeur de durabilité/texture-ID (voir ItemBuilder.durability())
 * — un futur resource pack pourra retexturer Material.TRAPPED_CHEST en vert
 * émeraude sans qu'aucun code ne change ici.
 */
public class EmeraldChest implements CustomItem {

    public static final String ID = "EMERALD_CHEST";

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
        return new ItemBuilder(Material.TRAPPED_CHEST)
                .emeraldName("Coffre", false)
                .loreLines(
                        "&7Permet de stocker autant",
                        "&7qu'un double coffre tout en",
                        "&7conservant la taille d'un",
                        "&7coffre simple."
                )
                .loreLine("&a✓ Grande capacité")
                .customId(ID)
                .noCustomEnchantsTag()
                .build();
    }
}
