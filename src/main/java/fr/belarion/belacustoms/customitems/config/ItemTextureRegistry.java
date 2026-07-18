package fr.belarion.belacustoms.customitems.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Charge la section `textures` de custom-items.yml : associe a chaque
 * identifiant interne de custom item (le meme ID que celui stocke en NBT,
 * ex: "EMERALD_HAMMER") un Material vanilla + une Damage Value uniques.
 *
 * Le fichier est copie dans le dossier de donnees du plugin au premier
 * demarrage (saveResource) afin d'etre modifiable sans recompiler le
 * plugin, conformement au cahier des charges ("Les IDs doivent etre
 * facilement modifiables sans modifier le code Java").
 */
public class ItemTextureRegistry {

    private final Plugin plugin;
    private final Map<String, ItemTexture> textures = new HashMap<>();

    public ItemTextureRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        textures.clear();

        File file = new File(plugin.getDataFolder(), "custom-items.yml");
        if (!file.exists()) {
            plugin.saveResource("custom-items.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection texturesSection = config.getConfigurationSection("textures");
        if (texturesSection == null) {
            plugin.getLogger().warning("[custom-items.yml] Section 'textures' introuvable : aucune texture chargee.");
            return;
        }

        for (String key : texturesSection.getKeys(false)) {
            ConfigurationSection section = texturesSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String materialName = section.getString("material");
            int durability = section.getInt("durability", 0);
            if (materialName == null) {
                plugin.getLogger().warning("[custom-items.yml] Entree textures/'" + key + "' invalide : materiau manquant.");
                continue;
            }
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("[custom-items.yml] Materiau inconnu pour '" + key + "' : " + materialName);
                continue;
            }
            textures.put(key.toUpperCase(Locale.ROOT), new ItemTexture(material, (short) durability));
        }
    }

    public Optional<ItemTexture> get(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(textures.get(id.toUpperCase(Locale.ROOT)));
    }
}
