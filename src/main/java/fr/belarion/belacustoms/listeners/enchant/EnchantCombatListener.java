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
 * Gere l'Aimantation (Magnet) cote loots de mobs : si l'arme du tueur porte
 * Aimantation, les drops partent directement dans son inventaire au lieu
 * de tomber au sol.
 */
public class EnchantCombatListener implements Listener {

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.getKiller() instanceof Player)) return;
        Player killer = entity.getKiller();

        ItemStack weapon = killer.getItemInHand();
        if (!ItemTierUtil.isEmeraldTier(weapon)) return;
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
