package fr.belarion.belacustoms.gui.emeraldenchanttable;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantBookUtil;
import fr.belarion.belacustoms.gui.GuiUtil;
import fr.belarion.belacustoms.managers.MessagesManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * GUI de la Table d'Enchantement Emeraude.
 * 27 slots, style premium : gris fonce dominant, bordures gris clair,
 * quelques accents emeraude autour des slots fonctionnels.
 * Livre vierge : slot 13. Bouton Enchanter : slot 22. Bibliotheque : slot 26.
 */
public final class EnchantTableGUI {

    public static final String TITLE = ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "✦ Table Emeraude ✦";

    public static final int SLOT_BOOK = 13;
    public static final int SLOT_ENCHANT = 22;
    public static final int SLOT_LIBRARY = 26;

    /** Vitres emeraude decoratives encadrant les slots fonctionnels. */
    private static final int[] ACCENT_SLOTS = new int[]{12, 14, 21, 23};

    private static final Random RANDOM = new Random();

    private EnchantTableGUI() {}

    public static Inventory build() {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        GuiUtil.fillPremiumBackground(inv, ACCENT_SLOTS);
        // Avant-derniere case (slot 25) : vitre gris pale pour respecter le design du fond.
        inv.setItem(25, GuiUtil.pane(GuiUtil.GLASS_LIGHT_GRAY));

        inv.setItem(SLOT_BOOK, null);
        inv.setItem(SLOT_ENCHANT, buildEnchantButton());
        inv.setItem(SLOT_LIBRARY, buildLibraryButton());

        return inv;
    }

    public static ItemStack buildEnchantButton() {
        List<String> lore = new ArrayList<String>();
        lore.add(GuiUtil.SEPARATOR);
        lore.add("");
        lore.add(ChatColor.GRAY + "Place un livre vierge");
        lore.add(ChatColor.GRAY + "dans la case prevue.");
        lore.add("");
        lore.add(ChatColor.GREEN + "Cout" + ChatColor.WHITE + " " + BelaCustoms.get().getEnchantSettings().getEnchantTableCost() + " niveaux");
        lore.add(ChatColor.GREEN + "Gain" + ChatColor.WHITE + " 1 enchant aleatoire");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Toutes les chances sont egales.");
        lore.add(GuiUtil.SEPARATOR);
        return GuiUtil.button(Material.EMERALD, ChatColor.GREEN, "✦ Enchanter ✦",
                lore.toArray(new String[0]));
    }

    public static ItemStack buildLibraryButton() {
        return GuiUtil.button(Material.BOOK_AND_QUILL, ChatColor.WHITE, "Bibliotheque d'Enchants",
                GuiUtil.SEPARATOR,
                ChatColor.GRAY + "Consulte tous les",
                ChatColor.GRAY + "custom enchants disponibles.",
                GuiUtil.SEPARATOR);
    }

    public static void tryEnchant(Player player, Inventory top) {
        ItemStack book = top.getItem(SLOT_BOOK);
        MessagesManager msg = BelaCustoms.get().getMessagesManager();
        int cost = BelaCustoms.get().getEnchantSettings().getEnchantTableCost();

        if (book == null || book.getType() != Material.BOOK) {
            msg.send(player, "enchants.table.need-book");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        if (player.getLevel() < cost) {
            msg.send(player, "enchants.table.not-enough-levels", "cost", String.valueOf(cost), "current", String.valueOf(player.getLevel()));
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        if (book.getAmount() > 1) {
            book.setAmount(book.getAmount() - 1);
            top.setItem(SLOT_BOOK, book);
        } else {
            top.setItem(SLOT_BOOK, null);
        }
        player.setLevel(player.getLevel() - cost);

        CustomEnchant[] all = CustomEnchant.values();
        CustomEnchant picked = all[RANDOM.nextInt(all.length)];

        ItemStack result = EnchantBookUtil.createBook(picked);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(result);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItem(player.getLocation(), leftover);
        }

        msg.send(player, "enchants.table.success", "enchant", picked.getDisplayName());
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1.2f);
    }
}
