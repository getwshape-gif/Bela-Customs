package fr.belarion.belacustoms.managers;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Charge et expose les valeurs de config.yml : reglages generaux, communs a
 * tout le plugin (pas specifiques aux Custom Enchants ni aux Custom Items).
 *
 * Voir customenchants.EnchantSettings pour custom-enchants.yml et
 * customitems.config.ItemStatsConfig pour custom-items.yml.
 */
public final class ConfigManager {

    private final BelaCustoms plugin;

    private boolean protectionCheckEnabled;
    private boolean debug;

    public ConfigManager(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        protectionCheckEnabled = cfg.getBoolean("protection.check-enabled", true);
        debug = cfg.getBoolean("debug", false);
    }

    public boolean isProtectionCheckEnabled() { return protectionCheckEnabled; }
    public boolean isDebug() { return debug; }
}
