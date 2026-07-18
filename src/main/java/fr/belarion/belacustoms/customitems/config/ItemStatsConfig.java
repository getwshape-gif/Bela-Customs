package fr.belarion.belacustoms.customitems.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Charge la section `stats` de custom-items.yml et expose des accesseurs
 * types pour chaque statistique de custom item (rayon de minage, bonus de
 * degats, reduction de degats d'armure...).
 *
 * Remplace le passage repete d'un FileConfiguration brut + chemin de cle
 * "en dur" (config.getInt("hammer.emerald.radius", 1)) a chaque classe
 * d'item : chaque valeur n'est lue et documentee qu'a un seul endroit.
 */
public class ItemStatsConfig {

    private final Plugin plugin;
    private FileConfiguration config;

    public ItemStatsConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "custom-items.yml");
        if (!file.exists()) {
            plugin.saveResource("custom-items.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public int getHammerEmeraldRadius() { return config.getInt("stats.hammer.emerald.radius", 1); }
    public int getHammerReinforcedRadius() { return config.getInt("stats.hammer.reinforced.radius", 2); }

    public int getShovelEmeraldRadius() { return config.getInt("stats.shovel.emerald.radius", 1); }
    public int getShovelReinforcedRadius() { return config.getInt("stats.shovel.reinforced.radius", 2); }

    public int getHoeEmeraldRadius() { return config.getInt("stats.hoe.emerald.radius", 0); }
    public int getHoeReinforcedRadius() { return config.getInt("stats.hoe.reinforced.radius", 1); }

    public int getAxeEmeraldLength() { return config.getInt("stats.axe.emerald.length", 3); }

    public double getSwordEmeraldBonusDamage() { return config.getDouble("stats.sword.emerald.bonus-damage", 3.0); }
    public double getSwordReinforcedBonusDamage() { return config.getDouble("stats.sword.reinforced.bonus-damage", 5.0); }

    public double getArmorEmeraldReductionPerPiece() { return config.getDouble("stats.armor.emerald.reduction-per-piece", 0.05); }
    public double getArmorReinforcedReductionPerPiece() { return config.getDouble("stats.armor.reinforced.reduction-per-piece", 0.08); }
}
