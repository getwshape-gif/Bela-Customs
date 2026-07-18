package fr.belarion.belacustoms.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> c(String... lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            result.add(c(line));
        }
        return result;
    }

    public static List<String> c(List<String> lines) {
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(c(line));
        }
        return result;
    }
}
