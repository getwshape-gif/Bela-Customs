package fr.belarion.belacustoms.listeners.item;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

import java.util.Optional;
import java.util.Random;

/**
 * Fait respecter la regle "items renforces = incassables" (filet de securite
 * en plus du tag NBT Unbreakable) et simule la duree de vie superieure des
 * items emeraude de base via ExtraDurability.
 */
public class ItemDurabilityListener implements Listener {

    private final CustomItemManager manager;
    private final Random random = new Random();

    public ItemDurabilityListener(CustomItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Optional<CustomItem> opt = manager.getCustomItem(event.getItem());
        if (!opt.isPresent()) {
            return;
        }
        CustomItem custom = opt.get();

        if (custom.isUnbreakable()) {
            event.setCancelled(true);
            return;
        }

        if (custom instanceof ExtraDurability) {
            double multiplier = ((ExtraDurability) custom).getDurabilityMultiplier();
            double cancelChance = 1.0 - (1.0 / multiplier);
            if (random.nextDouble() < cancelChance) {
                event.setCancelled(true);
            }
        }
    }
}
