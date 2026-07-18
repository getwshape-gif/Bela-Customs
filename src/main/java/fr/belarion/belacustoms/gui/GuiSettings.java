package fr.belarion.belacustoms.gui;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/** Charge et expose les valeurs de gui.yml (reglages d'affichage des interfaces). */
public final class GuiSettings {

    private final BelaCustoms plugin;

    private int libraryPageSize;

    public GuiSettings(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "gui.yml");
        if (!file.exists()) {
            plugin.saveResource("gui.yml", false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        libraryPageSize = cfg.getInt("library.page-size", 16);
    }

    public int getLibraryPageSize() { return libraryPageSize; }
}
