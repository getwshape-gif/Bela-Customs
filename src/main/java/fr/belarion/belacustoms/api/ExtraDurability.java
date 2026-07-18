package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les items "de base" (non renforces) qui doivent avoir plus
 * de durabilite qu'un equivalent diamant, sans etre totalement incassables.
 *
 * Limitation technique : Bukkit 1.8 ne permet pas de modifier la durabilite
 * maximale d'un Material (c'est code en dur cote NMS). On simule donc une
 * durabilite superieure en annulant probabilistiquement une partie des
 * degats de durabilite : avec un multiplicateur de 2.5, l'item perd en
 * moyenne un point de durabilite toutes les 2.5 utilisations au lieu de
 * chaque utilisation, soit une duree de vie effective 2.5x superieure a
 * l'equivalent diamant vanilla (regle : Durabilite Emeraude = Durabilite
 * Diamant vanilla x 2.5).
 * Consommee par le listener de durabilite des custom items.
 */
public interface ExtraDurability {

    /**
     * @return le multiplicateur de duree de vie effective (2.5 = items
     * emeraude non renforces durent 2.5x plus longtemps qu'un equivalent
     * diamant vanilla).
     */
    double getDurabilityMultiplier();
}
