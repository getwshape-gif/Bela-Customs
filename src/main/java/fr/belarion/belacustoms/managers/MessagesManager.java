package fr.belarion.belacustoms.managers;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Charge messages.yml et fournit des messages formates/colores, avec
 * placeholders simples. Point d'entree UNIQUE pour tous les messages du
 * plugin (Custom Enchants comme Custom Items) : evite la duplication qui
 * existait avant la fusion entre un systeme de messages.yml (cote enchants)
 * et des messages codes en dur (cote items).
 */
public final class MessagesManager {

    private final BelaCustoms plugin;
    private FileConfiguration messages;
    private String prefix = "";

    public MessagesManager(BelaCustoms plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);

        // Complete avec les valeurs par defaut embarquees dans le jar si des cles manquent.
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            Reader reader = new InputStreamReader(defStream, StandardCharsets.UTF_8);
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
            messages.setDefaults(defaults);
        }

        prefix = color(messages.getString("prefix", ""));
    }

    public String get(String path) {
        String raw = messages.getString(path, path);
        return color(raw);
    }

    public String get(String path, String... placeholdersAndValues) {
        String raw = get(path);
        for (int i = 0; i + 1 < placeholdersAndValues.length; i += 2) {
            raw = raw.replace("%" + placeholdersAndValues[i] + "%", placeholdersAndValues[i + 1]);
        }
        return raw;
    }

    public void send(Player player, String path, String... placeholdersAndValues) {
        player.sendMessage(prefix + get(path, placeholdersAndValues));
    }

    /** Variante generique acceptant tout CommandSender (utile pour la console, ex: /citem depuis la console). */
    public void send(CommandSender sender, String path, String... placeholdersAndValues) {
        sender.sendMessage(prefix + get(path, placeholdersAndValues));
    }

    private String color(String raw) {
        if (raw == null) return "";
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
