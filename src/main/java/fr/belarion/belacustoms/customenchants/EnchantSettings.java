package fr.belarion.belacustoms.customenchants;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * Charge et expose les valeurs de custom-enchants.yml (couts, limites du
 * Vein Miner). Remplace l'ancien ConfigManager du plugin Belarion-Enchants,
 * desormais scinde par domaine (voir managers.ConfigManager pour les
 * reglages generaux et gui.GuiSettings pour l'affichage).
 */
public final class EnchantSettings {

    private final BelaCustoms plugin;

    private int enchantTableCost;
    private int emeraldAnvilCost;
    private int veinMinerMaxBlocks;

    public EnchantSettings(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "custom-enchants.yml");
        if (!file.exists()) {
            plugin.saveResource("custom-enchants.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        enchantTableCost = cfg.getInt("costs.enchant-table", 60);
        emeraldAnvilCost = cfg.getInt("costs.emerald-anvil", 30);
        veinMinerMaxBlocks = cfg.getInt("vein-miner-max-blocks", 64);
    }

    public int getEnchantTableCost() { return enchantTableCost; }
    public int getEmeraldAnvilCost() { return emeraldAnvilCost; }
    public int getVeinMinerMaxBlocks() { return veinMinerMaxBlocks; }
}
