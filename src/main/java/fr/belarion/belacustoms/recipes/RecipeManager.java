package fr.belarion.belacustoms.recipes;

import fr.belarion.belacustoms.BelaCustoms;

/**
 * Point d'entree centralise pour toutes les recettes de craft custom du
 * plugin, definies dans recipes.yml.
 *
 * Aucune recette n'est enregistree pour le moment (ni Belarion-Enchants, ni
 * CustomItems n'en definissaient avant la fusion) : ce manager est un
 * scaffold pret a l'emploi, deja cable dans BelaCustoms#onEnable(), pour
 * que l'ajout d'une future recette se limite a :
 *   1. Declarer la recette dans recipes.yml.
 *   2. Ajouter une methode registerXxx() ci-dessous qui construit une
 *      Bukkit ShapedRecipe/ShapelessRecipe et l'enregistre via
 *      Bukkit#addRecipe(...), puis l'appeler depuis registerAll().
 */
public final class RecipeManager {

    private final BelaCustoms plugin;

    public RecipeManager(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    /** Enregistre toutes les recettes custom du plugin aupres du serveur. */
    public void registerAll() {
        // Aucune recette custom definie pour le moment. Voir la Javadoc de
        // cette classe pour la marche a suivre lors de l'ajout d'une premiere
        // recette.
        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("[Bela-Customs] RecipeManager : aucune recette custom enregistree.");
        }
    }
}
