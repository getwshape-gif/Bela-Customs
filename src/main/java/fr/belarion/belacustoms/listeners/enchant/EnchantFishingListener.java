package fr.belarion.belacustoms.listeners.enchant;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Anti Rod : rend la canne a peche totalement inoffensive contre un joueur
 * qui porte l'enchant. Trois couches de protection :
 *  1) PlayerFishEvent(CAUGHT_ENTITY) annule des la collision du hameçon.
 *  2) Le hameçon est retire et la velocite du joueur restauree au tick
 *     suivant, au cas ou une pull aurait deja ete appliquee.
 *  3) EntityDamageByEntityEvent annule si le degat provient d'un FishHook.
 */
public class EnchantFishingListener implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;

        Entity caught = event.getCaught();
        if (!(caught instanceof Player)) return;
        final Player victim = (Player) caught;

        if (!hasAntiRod(victim)) return;

        event.setCancelled(true);

        final Entity hook = event.getHook();
        final Vector safeVelocity = victim.getVelocity();

        // Filet de securite : sur certaines implementations 1.8, la pull peut
        // deja avoir ete calculee au moment ou l'event est leve. On force la
        // velocite d'origine et on retire le hameçon au tick suivant.
        Bukkit.getScheduler().runTask(BelaCustoms.get(), new Runnable() {
            @Override
            public void run() {
                victim.setVelocity(safeVelocity);
                if (hook != null && !hook.isDead()) {
                    hook.remove();
                }
            }
        });
    }

    @EventHandler
    public void onHookDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof FishHook)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        if (hasAntiRod(victim)) {
            event.setCancelled(true);
        }
    }

    private boolean hasAntiRod(Player player) {
        return hasOn(player.getInventory().getBoots())
                || hasOn(player.getInventory().getHelmet())
                || hasOn(player.getInventory().getChestplate())
                || hasOn(player.getInventory().getLeggings());
    }

    private boolean hasOn(ItemStack item) {
        return item != null && ItemTierUtil.isEmeraldTier(item)
                && EnchantStorage.hasEnchant(item, CustomEnchant.ANTI_ROD);
    }
}
