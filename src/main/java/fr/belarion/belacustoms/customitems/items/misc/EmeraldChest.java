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
    * Choix de Material.ENDER_CHEST (remplace l'ancien Material.TRAPPED_CHEST,
                                     * voir historique du plugin) : contrairement à un coffre normal ou piégé,
    * un Coffre de l'End n'a strictement AUCUNE mécanique de fusion visuelle
    * ou fonctionnelle en Minecraft — ce n'est même pas une possibilité du
    * jeu. Deux Coffres en Émeraude posés côte à côte restent donc TOUJOURS
    * deux blocs strictement indépendants, sans protection à mettre en place
    * (voir historique du plugin pour l'ancien hack par réflexion NMS
    * ChestMergeGuard, devenu inutile et supprimé).
    *
    * Le comportement spécial vanilla d'un Coffre de l'End (stockage perso
                                                            * partagé du joueur, 27 slots) ne se déclenche jamais : EmeraldChestListener
    * intercepte systématiquement le clic droit avant d'atteindre le vrai
    * inventaire du bloc, qui reste donc toujours vide et sans lien avec
    * l'Ender Chest personnel du joueur.
    *
    * Texture : assets/minecraft/textures/entity/chest/ender.png (resource
                                                                  * pack) — un seul fichier suffit, un Coffre de l'End n'a pas de variante
    * "double" puisqu'il ne fusionne jamais.
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
        return new ItemBuilder(Material.ENDER_CHEST)
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
