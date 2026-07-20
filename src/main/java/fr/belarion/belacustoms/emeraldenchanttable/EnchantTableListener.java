package fr.belarion.belacustoms.emeraldenchanttable;

import fr.belarion.belacustoms.gui.emeraldenchanttable.EnchantLibraryGUI;
import fr.belarion.belacustoms.gui.emeraldenchanttable.EnchantTableGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Gère tout le cycle de vie de la Table d'Enchantement Émeraude :
 * ouverture (clic droit sur un bloc Prismarine avec data 2, c'est-à-dire
 * Dark Prismarine — bloc de référence depuis le remplacement du Bloc
 * d'Émeraude), clics dans la table et dans la bibliothèque d'enchants
 * (avec pagination), fermeture (rend le livre posé).
 */
public class EnchantTableListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        // Dark Prismarine (Material.PRISMARINE, data 2) : voir
        // custom-items.yml (EMERALD_ENCHANT_TABLE) et
        // emerald_enchant_table.properties pour le reste du mapping.
        if (block.getType() != Material.PRISMARINE || block.getData() != 2) return;

        event.setCancelled(true);
        event.getPlayer().openInventory(EnchantTableGUI.build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (EnchantLibraryGUI.TITLE.equals(title)) {
            handleLibraryClick(event, player);
            return;
        }

        if (!EnchantTableGUI.TITLE.equals(title)) return;
        handleTableClick(event, player);
    }

    private void handleTableClick(InventoryClickEvent event, Player player) {
        Inventory top = event.getView().getTopInventory();
        int raw = event.getRawSlot();

        if (raw >= top.getSize()) {
            if (event.isShiftClick()) {
                handleShiftClickIntoTable(event, top);
            }
            return;
        }

        if (raw == EnchantTableGUI.SLOT_BOOK) {
            // Placement direct (sans shift) : n'accepte qu'un livre vierge, ou une
            // main vide pour reprendre le livre déjà posé. Bloque tout le reste.
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && cursor.getType() != Material.BOOK) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        if (raw == EnchantTableGUI.SLOT_LIBRARY) {
            player.openInventory(EnchantLibraryGUI.build(0));
            return;
        }

        if (raw == EnchantTableGUI.SLOT_ENCHANT) {
            EnchantTableGUI.tryEnchant(player, top);
        }
    }

    /**
     * Reproduit le comportement vanilla du Shift + Click depuis l'inventaire
     * du joueur : un livre vierge shift-cliqué est envoyé automatiquement
     * dans le slot d'entrée de la table, en fusionnant avec la pile déjà
     * présente si possible. Tout autre item reste bloqué (placement invalide).
     */
    private void handleShiftClickIntoTable(InventoryClickEvent event, Inventory top) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.BOOK) return;

        ItemStack existing = top.getItem(EnchantTableGUI.SLOT_BOOK);
        if (existing == null || existing.getType() == Material.AIR) {
            top.setItem(EnchantTableGUI.SLOT_BOOK, clicked.clone());
            event.setCurrentItem(null);
            return;
        }

        if (existing.getType() != Material.BOOK) return;

        int space = existing.getMaxStackSize() - existing.getAmount();
        if (space <= 0) return;

        int moved = Math.min(space, clicked.getAmount());
        existing.setAmount(existing.getAmount() + moved);
        top.setItem(EnchantTableGUI.SLOT_BOOK, existing);

        if (moved >= clicked.getAmount()) {
            event.setCurrentItem(null);
        } else {
            clicked.setAmount(clicked.getAmount() - moved);
            event.setCurrentItem(clicked);
        }
    }

    private void handleLibraryClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        int raw = event.getRawSlot();

        if (raw == EnchantLibraryGUI.SLOT_BACK) {
            player.openInventory(EnchantTableGUI.build());
            return;
        }
        if (raw == EnchantLibraryGUI.SLOT_NEXT) {
            int page = getPage(event.getView().getTopInventory()) + 1;
            player.openInventory(EnchantLibraryGUI.build(page));
            return;
        }
        if (raw == EnchantLibraryGUI.SLOT_PREV) {
            int page = getPage(event.getView().getTopInventory()) - 1;
            player.openInventory(EnchantLibraryGUI.build(page));
        }
    }

    /** La page actuelle est encodée dans la quantité du bouton Retour (page + 1). */
    private int getPage(Inventory top) {
        ItemStack back = top.getItem(EnchantLibraryGUI.SLOT_BACK);
        if (back == null) return 0;
        return Math.max(0, back.getAmount() - 1);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!EnchantTableGUI.TITLE.equals(event.getView().getTitle())) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Inventory top = event.getView().getTopInventory();

        ItemStack item = top.getItem(EnchantTableGUI.SLOT_BOOK);
        if (item != null && item.getType() != Material.AIR) {
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
            top.setItem(EnchantTableGUI.SLOT_BOOK, null);
        }
    }
}
