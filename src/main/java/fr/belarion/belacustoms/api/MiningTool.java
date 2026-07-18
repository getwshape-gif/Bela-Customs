package fr.belarion.belacustoms.api;

/**
 * Marqueur pour les outils capables de casser une zone de blocs autour du
 * bloc cible (hammer, pelle...). Implementee par les items concernes et
 * consommee par les listeners de minage des custom items.
 */
public interface MiningTool {

    /**
     * @return le rayon de la zone autour du bloc casse (1 = zone 3x3, 2 = zone 5x5).
     */
    int getRadius();

    /**
     * @return true si seuls les blocs du MEME type que le bloc cible doivent
     * etre casses (regle imposee par le cahier des charges pour tous les outils
     * de zone : "Casse uniquement le meme type de bloc").
     */
    default boolean sameBlockTypeOnly() {
        return true;
    }
}
