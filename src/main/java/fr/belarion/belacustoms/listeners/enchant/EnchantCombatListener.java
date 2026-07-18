package fr.belarion.belacustoms.listeners.enchant;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Gere deux Custom Enchants lies a la mort d'une creature :
 * - Aimantation (Magnet) : si l'arme du tueur la porte, les drops partent
 *   directement dans son inventaire au lieu de tomber au sol.
 * - XP Boost : si l'arme du tueur la porte, l'experience vanilla obtenue
 *   sur le kill est doublee (+100%). Ne touche jamais aux drops/loot,
 *   uniquement a l'experience (getDroppedExp/setDroppedExp), et fonctionne
 *   sur toute creature vanilla donnant de l'XP puisqu'elle multiplie la
 *   valeur deja calculee par le jeu (0 reste 0 sur les creatures qui n'en
 *   donnent pas).
 */
public class EnchantCombatListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.getKiller() instanceof Player)) return;
        Player killer = entity.getKiller();

        ItemStack weapon = killer.getItemInHand();
        if (!ItemTierUtil.isEmeraldTier(weapon)) return;

        if (EnchantStorage.hasEnchant(weapon, CustomEnchant.XP_BOOST)) {
            event.setDroppedExp(event.getDroppedExp() * 2);
        }

        if (!EnchantStorage.hasEnchant(weapon, CustomEnchant.MAGNET)) return;

        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
            ItemStack drop = it.next();
            Map<Integer, ItemStack> leftovers = killer.getInventory().addItem(drop);
            for (ItemStack leftover : leftovers.values()) {
                killer.getWorld().dropItem(entity.getLocation(), leftover);
            }
            it.remove();
        }
    }
}
