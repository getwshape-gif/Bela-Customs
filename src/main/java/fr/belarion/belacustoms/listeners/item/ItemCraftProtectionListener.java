package fr.belarion.belacustoms.listeners.item;

import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Protections generiques transverses a tous les custom items :
 * empeche qu'un custom item soit utilise comme ingredient dans une recette
 * de craft vanilla (ce qui ferait perdre son NBT/son identite ou permettrait
 * des exploits de duplication de stats).
 */
public class ItemCraftProtectionListener implements Listener {

    private final CustomItemManager manager;

    public ItemCraftProtectionListener(CustomItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient == null) {
                continue;
            }
            if (manager.isCustomItem(ingredient)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }
}
