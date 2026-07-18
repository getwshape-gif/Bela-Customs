package fr.belarion.belacustoms.api;

import org.bukkit.inventory.ItemStack;

/**
 * Contrat de base que doit respecter chaque custom item du systeme.
 * Chaque implementation possede un identifiant interne unique (utilise pour
 * le tag NBT "CustomItemId") et sait construire son propre ItemStack fini
 * (nom, lore, materiau, enchantements vanilla, unbreakable, etc.).
 */
public interface CustomItem {

    /**
     * @return l'identifiant interne unique de l'item (ex: "EMERALD_HAMMER").
     * Utilise comme cle dans le CustomItemRegistry et comme valeur du tag NBT.
     */
    String getId();

    /**
     * @return un nouvel ItemStack pret a l'emploi representant cet item.
     * Doit toujours retourner une NOUVELLE instance (pas de partage d'etat mutable).
     */
    ItemStack build();

    /**
     * @return true si l'item doit etre incassable (NBT Unbreakable + annulation
     * de la perte de durabilite). Par convention, tous les items "renforces"
     * retournent true ici.
     */
    boolean isUnbreakable();
}
