package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les houes capables de recolter et replanter automatiquement
 * une zone de cultures. Consommee par le listener d'agriculture des custom items.
 */
public interface ReplantingHoe {

    /**
     * @return le rayon de la zone de recolte (0 = uniquement le bloc casse,
     * 1 = zone 3x3 autour du bloc casse).
     */
    int getRadius();
}
