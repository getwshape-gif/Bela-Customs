package fr.belarion.belacustoms.listeners.item;

import fr.belarion.belacustoms.api.ArmorBonus;
import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.WeaponBonus;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Applique le bonus de degats des epees emeraude et la reduction de degats
 * supplementaire des armures emeraude.
 *
 * Pourquoi une gestion manuelle et pas des attribute modifiers ? L'API
 * Bukkit 1.8 ne propose pas encore ItemMeta#addAttributeModifier (introduit
 * en 1.13). Le bonus est donc calcule et applique directement sur les
 * evenements de degats, ce qui reste totalement compatible avec les
 * Custom Enchants d'autres plugins (ils s'appliquent normalement avant/apres
 * dans la chaine d'evenements selon leur propre priorite).
 */
public class ItemCombatListener implements Listener {

    /** Securite anti-abus : la reduction cumulee de toutes les pieces d'armure ne peut jamais depasser 80%. */
    private static final double MAX_ARMOR_REDUCTION = 0.80;

    private final CustomItemManager manager;

    public ItemCombatListener(CustomItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onWeaponDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player attacker = (Player) event.getDamager();
        ItemStack hand = attacker.getItemInHand();

        Optional<CustomItem> opt = manager.getCustomItem(hand);
        if (opt.isPresent() && opt.get() instanceof WeaponBonus) {
            double bonus = ((WeaponBonus) opt.get()).getBonusDamage();
            event.setDamage(event.getDamage() + bonus);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorReduction(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();

        double totalReduction = 0.0;
        for (ItemStack armorPiece : victim.getInventory().getArmorContents()) {
            if (armorPiece == null) {
                continue;
            }
            Optional<CustomItem> opt = manager.getCustomItem(armorPiece);
            if (opt.isPresent() && opt.get() instanceof ArmorBonus) {
                totalReduction += ((ArmorBonus) opt.get()).getDamageReductionPercent();
            }
        }

        if (totalReduction <= 0) {
            return;
        }
        totalReduction = Math.min(totalReduction, MAX_ARMOR_REDUCTION);
        event.setDamage(event.getDamage() * (1.0 - totalReduction));
    }
}
