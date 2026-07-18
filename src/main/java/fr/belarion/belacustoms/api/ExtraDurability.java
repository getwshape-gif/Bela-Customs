package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les items "de base" (non renforces) qui doivent avoir plus
 * de durabilite qu'un equivalent diamant, sans etre totalement incassables.
 *
 * Limitation technique : Bukkit 1.8 ne permet pas de modifier la durabilite
 * maximale d'un Material (c'est code en dur cote NMS). On simule donc une
 * durabilite superieure en annulant probabilistiquement une partie des
 * degats de durabilite : avec un multiplicateur de 1.5, l'item perd en
 * moyenne un point de durabilite toutes les 1.5 utilisations au lieu de
 * chaque utilisation, soit +50% de duree de vie effective.
 * Consommee par le listener de durabilite des custom items.
 */
public interface ExtraDurability {

    /**
     * @return le multiplicateur de duree de vie effective (1.5 = +50%).
     */
    double getDurabilityMultiplier();
}
