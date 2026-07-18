package fr.belarion.belacustoms.customenchants;

import fr.belarion.belacustoms.gui.GuiUtil;
import fr.belarion.belacustoms.utils.HiddenTag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Création / lecture des livres custom obtenus à la Table d'Enchantement Émeraude. */
public final class EnchantBookUtil {

    public static final String TAG_KEY = "belench";

    private EnchantBookUtil() {}

    public static ItemStack createBook(CustomEnchant enchant) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(enchant.getColor().toString() + ChatColor.BOLD + "Livre : " + enchant.getDisplayName());

        List<String> lore = new ArrayList<String>(GuiUtil.buildEnchantLore(enchant));
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "À combiner dans l'Enclume Émeraude.");
        meta.setLore(lore);
        book.setItemMeta(meta);

        HiddenTag.write(book, TAG_KEY, enchant.getId());
        return book;
    }

    /** Custom enchant contenu dans un livre custom, sinon null. */
    public static CustomEnchant readEnchant(ItemStack item) {
        String id = HiddenTag.read(item, TAG_KEY);
        if (id == null) return null;
        return CustomEnchant.fromId(id);
    }

    public static boolean isCustomBook(ItemStack item) {
        return readEnchant(item) != null;
    }
}
