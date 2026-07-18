package fr.belarion.belacustoms.emeraldanvil;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.compatibility.CompatibilityManager;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantBookUtil;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.gui.emeraldanvil.EmeraldAnvilGUI;
import fr.belarion.belacustoms.managers.MessagesManager;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Map;

/**
 * Gère l'Enclume Émeraude : ouverture (clic droit sur Sea Lantern), la
 * combinaison item + livre (custom multi-enchant OU vanilla classique),
 * et empêche l'usage d'une vraie enclume vanilla sur du stuff émeraude.
 */
public class EmeraldAnvilListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.SEA_LANTERN) return;

        event.setCancelled(true);
        event.getPlayer().openInventory(EmeraldAnvilGUI.build());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Empêche de traiter du stuff émeraude dans une VRAIE enclume vanilla.
        if (event.getInventory() != null && event.getInventory().getType() == InventoryType.ANVIL
                && !EmeraldAnvilGUI.TITLE.equals(title)) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (ItemTierUtil.isEmeraldTier(current) || ItemTierUtil.isEmeraldTier(cursor)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player) {
                    BelaCustoms.get().getMessagesManager().send((Player) event.getWhoClicked(), "enchants.anvil.protected");
                }
            }
            return;
        }

        if (!EmeraldAnvilGUI.TITLE.equals(title)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        int raw = event.getRawSlot();
        Inventory top = event.getView().getTopInventory();

        if (raw >= top.getSize()) {
            if (event.isShiftClick()) {
                handleShiftClickIntoAnvil(event, top);
            }
            return;
        }

        if (raw == EmeraldAnvilGUI.SLOT_ITEM) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && !ItemTierUtil.isEmeraldTier(cursor)) {
                event.setCancelled(true);
            }
            return;
        }

        if (raw == EmeraldAnvilGUI.SLOT_BOOK) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR && cursor.getType() != Material.ENCHANTED_BOOK) {
                event.setCancelled(true);
            }
            return;
        }

        event.setCancelled(true);

        if (raw != EmeraldAnvilGUI.SLOT_CONFIRM) return;

        forge(player, top);
    }

    /**
     * Reproduit le Shift + Click vanilla depuis l'inventaire du joueur : un
     * item Émeraude part automatiquement dans le slot Item, un livre
     * (vanilla ou custom) part automatiquement dans le slot Livre. Tout
     * autre item, ou un slot déjà occupé, reste bloqué.
     */
    private void handleShiftClickIntoAnvil(InventoryClickEvent event, Inventory top) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (ItemTierUtil.isEmeraldTier(clicked)) {
            ItemStack existing = top.getItem(EmeraldAnvilGUI.SLOT_ITEM);
            if (existing == null || existing.getType() == Material.AIR) {
                top.setItem(EmeraldAnvilGUI.SLOT_ITEM, clicked.clone());
                event.setCurrentItem(null);
            }
            return;
        }

        if (clicked.getType() == Material.ENCHANTED_BOOK) {
            ItemStack existing = top.getItem(EmeraldAnvilGUI.SLOT_BOOK);
            if (existing == null || existing.getType() == Material.AIR) {
                top.setItem(EmeraldAnvilGUI.SLOT_BOOK, clicked.clone());
                event.setCurrentItem(null);
            }
        }
    }

    private void forge(Player player, Inventory top) {
        MessagesManager msg = BelaCustoms.get().getMessagesManager();

        ItemStack base = top.getItem(EmeraldAnvilGUI.SLOT_ITEM);
        ItemStack book = top.getItem(EmeraldAnvilGUI.SLOT_BOOK);

        if (base == null || book == null || base.getType() == Material.AIR || book.getType() == Material.AIR) {
            msg.send(player, "enchants.anvil.need-both");
            return;
        }

        if (!ItemTierUtil.isEmeraldTier(base)) {
            msg.send(player, "enchants.anvil.wrong-tier");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        CustomEnchant custom = EnchantBookUtil.readEnchant(book);
        boolean applied = false;

        if (custom != null) {
            CompatibilityManager.Result result = CompatibilityManager.check(base, custom);
            if (result == CompatibilityManager.Result.WRONG_TARGET) {
                msg.send(player, "enchants.anvil.wrong-target", "enchant", custom.getDisplayName(), "target", custom.getTarget().getLabel());
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                return;
            }
            if (result == CompatibilityManager.Result.ALREADY_APPLIED) {
                msg.send(player, "enchants.anvil.already-has", "item", base.getItemMeta().getDisplayName(), "enchant", custom.getDisplayName());
                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                return;
            }
            // result == OK (tier déjà vérifié plus haut)
        } else if (!(book.getType() == Material.ENCHANTED_BOOK && book.getItemMeta() instanceof EnchantmentStorageMeta)) {
            msg.send(player, "enchants.anvil.invalid-book");
            return;
        }

        int cost = BelaCustoms.get().getEnchantSettings().getEmeraldAnvilCost();
        if (player.getLevel() < cost) {
            msg.send(player, "enchants.anvil.not-enough-levels", "cost", String.valueOf(cost));
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
            return;
        }

        if (custom != null) {
            applied = EnchantStorage.addEnchant(base, custom);
            if (applied && custom.isVanillaEquivalent()) {
                base.addUnsafeEnchantment(custom.getVanillaEquivalent(), custom.getVanillaLevel());
            }
        } else {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();
            for (Map.Entry<Enchantment, Integer> entry : esm.getStoredEnchants().entrySet()) {
                base.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                applied = true;
            }
        }

        if (!applied) {
            msg.send(player, "enchants.anvil.invalid-book");
            return;
        }

        player.setLevel(player.getLevel() - cost);

        // Comme une vraie enclume : le résultat part directement dans
        // l'inventaire du joueur, les deux slots d'entrée sont vides
        // immédiatement. "base" est retiré du GUI avant d'être donné, il
        // ne peut donc jamais se retrouver à la fois dans l'enclume et
        // dans l'inventaire (pas de duplication possible).
        top.setItem(EmeraldAnvilGUI.SLOT_ITEM, null);
        top.setItem(EmeraldAnvilGUI.SLOT_BOOK, null);

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(base);
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItem(player.getLocation(), leftover);
        }

        msg.send(player, "enchants.anvil.success");
        player.playSound(player.getLocation(), Sound.ANVIL_USE, 1f, 1f);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!EmeraldAnvilGUI.TITLE.equals(event.getView().getTitle())) return;
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        Inventory top = event.getView().getTopInventory();

        int[] slots = new int[]{EmeraldAnvilGUI.SLOT_ITEM, EmeraldAnvilGUI.SLOT_BOOK};
        for (int slot : slots) {
            ItemStack item = top.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItem(player.getLocation(), leftover);
                }
                top.setItem(slot, null);
            }
        }
    }
}
