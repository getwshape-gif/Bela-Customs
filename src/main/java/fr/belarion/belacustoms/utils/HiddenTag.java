package fr.belarion.belacustoms.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * En 1.8 il n'y a pas de PersistentDataContainer pour stocker des donnees
 * cachees sur un item. Technique : on encode "cle:valeur" en precedant
 * chaque caractere du code couleur (invisible en jeu), et on colle ca
 * dans une ligne de lore. Invisible pour le joueur, lisible par le plugin.
 *
 * write() remplace toujours la ligne existante portant la meme cle au lieu
 * d'en ajouter une nouvelle : un tag ne peut donc jamais etre duplique.
 *
 * Utilise par le systeme de Custom Enchants (tier + enchants multiples d'un
 * item) via ItemTierUtil / customenchants.EnchantStorage / customenchants.EnchantBookUtil.
 */
public final class HiddenTag {

    private HiddenTag() {}

    public static String encode(String key, String value) {
        StringBuilder sb = new StringBuilder();
        String raw = key + ":" + value;
        for (char c : raw.toCharArray()) {
            sb.append(ChatColor.COLOR_CHAR).append(c);
        }
        return sb.toString();
    }

    /** Retourne la valeur du tag present sur l'item pour cette cle, sinon null. */
    public static String read(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return null;
        for (String line : meta.getLore()) {
            String decoded = decode(line);
            if (decoded != null && decoded.startsWith(key + ":")) {
                return decoded.substring(key.length() + 1);
            }
        }
        return null;
    }

    /** Ecrit (ou remplace si deja present) une ligne de lore invisible portant le tag. */
    public static void write(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList<String>();

        String newLine = encode(key, value);
        boolean replaced = false;
        for (int i = 0; i < lore.size(); i++) {
            String decoded = decode(lore.get(i));
            if (decoded != null && decoded.startsWith(key + ":")) {
                lore.set(i, newLine);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            lore.add(newLine);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /** Supprime la ligne de lore invisible portant cette cle, si presente. */
    public static void remove(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return;
        List<String> lore = new ArrayList<String>(meta.getLore());
        boolean changed = false;
        for (int i = lore.size() - 1; i >= 0; i--) {
            String decoded = decode(lore.get(i));
            if (decoded != null && decoded.startsWith(key + ":")) {
                lore.remove(i);
                changed = true;
            }
        }
        if (changed) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
    }

    /** true si la ligne est entierement composee de caracteres invisibles (une ligne de tag). */
    public static boolean isHiddenLine(String line) {
        return decode(line) != null;
    }

    /** true si cette ligne est precisement le tag cache correspondant a cette cle. */
    public static boolean matchesKey(String line, String key) {
        String decoded = decode(line);
        return decoded != null && decoded.startsWith(key + ":");
    }

    private static String decode(String line) {
        if (line == null || line.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        char[] chars = line.toCharArray();
        int i = 0;
        while (i < chars.length - 1 && chars[i] == ChatColor.COLOR_CHAR) {
            sb.append(chars[i + 1]);
            i += 2;
        }
        if (i != chars.length) return null;
        if (sb.length() == 0) return null;
        return sb.toString();
    }
}
