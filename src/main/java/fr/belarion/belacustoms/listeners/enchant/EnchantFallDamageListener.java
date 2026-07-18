package fr.belarion.belacustoms.listeners.enchant;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/** No Fall : annule entierement les degats de chute si les bottes portent l'enchant. */
public class EnchantFallDamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !ItemTierUtil.isEmeraldTier(boots)) return;
        if (!EnchantStorage.hasEnchant(boots, CustomEnchant.NO_FALL)) return;

        event.setCancelled(true);
    }
}
