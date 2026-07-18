package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les pieces d'armure offrant une reduction de degats
 * supplementaire par rapport au diamant. Meme limitation que WeaponBonus :
 * pas d'attribute modifiers natifs en 1.8, le bonus est donc applique
 * manuellement par le listener de combat via EntityDamageEvent.
 */
public interface ArmorBonus {

    /**
     * @return le pourcentage de reduction de degats supplementaire apporte
     * par cette piece (ex: 0.05 = 5%).
     */
    double getDamageReductionPercent();
}
