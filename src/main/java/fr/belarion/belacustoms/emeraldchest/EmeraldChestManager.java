package fr.belarion.belacustoms.emeraldchest;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Gère la persistance de tous les Coffres en Émeraude posés sur le serveur :
 * position, contenu virtuel (54 slots, capacité double coffre) et nombre
 * d'explosions déjà subies. Stocké dans emerald-chests.yml, chargé au
 * démarrage et sauvegardé à chaque modification (pose/casse/explosion/
 * fermeture d'inventaire) pour survivre à un crash serveur sans perte de
 * données.
 *
 * Le bloc réel posé (Material.TRAPPED_CHEST) n'est JAMAIS ouvert via le
 * chemin vanilla : son tile entity reste toujours vide. Tout le contenu
 * "double coffre" est entièrement virtuel et géré ici, ce qui permet à un
 * Coffre en Émeraude de garder un inventaire totalement indépendant, même
 * d'un autre Coffre en Émeraude posé juste à côté (voir EmeraldChest pour le
 * détail du choix de Material).
 */
public class EmeraldChestManager {

    public static final int SIZE = 54;
    public static final int MAX_EXPLOSIONS = 5;

    private final BelaCustoms plugin;
    private final Map<String, ChestData> chests = new HashMap<>();
    private File file;

    public EmeraldChestManager(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    private static class ChestData {
        int explosionHits;
        ItemStack[] contents = new ItemStack[SIZE];
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    public boolean isTracked(Location loc) {
        return chests.containsKey(key(loc));
    }

    /** Enregistre un Coffre en Émeraude qui vient d'être posé (inventaire vide, 0 explosion subie). */
    public void track(Location loc) {
        chests.put(key(loc), new ChestData());
        save();
    }

    /** Oublie un Coffre en Émeraude (cassé ou détruit par explosion). */
    public void untrack(Location loc) {
        chests.remove(key(loc));
        save();
    }

    /** @return le contenu virtuel du coffre (toujours un tableau de taille SIZE, jamais null). */
    public ItemStack[] getContents(Location loc) {
        ChestData data = chests.get(key(loc));
        return data != null ? data.contents : new ItemStack[SIZE];
    }

    public void setContents(Location loc, ItemStack[] contents) {
        ChestData data = chests.get(key(loc));
        if (data == null) return;
        data.contents = contents;
        save();
    }

    /**
     * Enregistre qu'une explosion vient de toucher ce coffre.
     * @return true si le coffre vient d'atteindre MAX_EXPLOSIONS et doit
     * donc être réellement détruit (l'appelant est responsable de déverser
     * le contenu et d'appeler untrack()).
     */
    public boolean registerExplosionHit(Location loc) {
        ChestData data = chests.get(key(loc));
        if (data == null) return false;
        data.explosionHits++;
        boolean destroyed = data.explosionHits >= MAX_EXPLOSIONS;
        save();
        return destroyed;
    }

    public void load() {
        chests.clear();
        file = new File(plugin.getDataFolder(), "emerald-chests.yml");
        if (!file.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("chests");
        if (root == null) return;

        for (String locKey : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(locKey);
            if (section == null) continue;

            ChestData data = new ChestData();
            data.explosionHits = section.getInt("hits", 0);

            ConfigurationSection contentsSection = section.getConfigurationSection("contents");
            if (contentsSection != null) {
                for (String slotKey : contentsSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(slotKey);
                        if (slot >= 0 && slot < SIZE) {
                            data.contents[slot] = contentsSection.getItemStack(slotKey);
                        }
                    } catch (NumberFormatException ignored) {
                        // clé invalide dans le fichier : on l'ignore simplement.
                    }
                }
            }
            chests.put(locKey, data);
        }
    }

    public void save() {
        if (file == null) {
            file = new File(plugin.getDataFolder(), "emerald-chests.yml");
        }
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<String, ChestData> entry : chests.entrySet()) {
            String base = "chests." + entry.getKey();
            ChestData data = entry.getValue();
            cfg.set(base + ".hits", data.explosionHits);
            for (int slot = 0; slot < SIZE; slot++) {
                ItemStack item = data.contents[slot];
                if (item != null && item.getType() != Material.AIR) {
                    cfg.set(base + ".contents." + slot, item);
                }
            }
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            plugin.getLogger().warning("[Bela-Customs] Impossible de sauvegarder emerald-chests.yml : " + ex);
        }
    }
}
