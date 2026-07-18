package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les armes infligeant un bonus de degats par rapport au
 * materiau vanilla equivalent. Necessaire car l'API Bukkit 1.8 ne propose
 * pas encore ItemMeta#addAttributeModifier (introduit en 1.13) : le bonus
 * est donc applique manuellement par le listener de combat via
 * EntityDamageByEntityEvent.
 */
public interface WeaponBonus {

    /**
     * @return les degats bonus (en demi-coeurs, ex: 3.0 = 1.5 coeur) ajoutes
     * a l'attaque au corps a corps.
     */
    double getBonusDamage();
}
