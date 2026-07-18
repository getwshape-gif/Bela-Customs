package fr.belarion.belacustoms.customitems.manager;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.registry.CustomItemRegistry;
import fr.belarion.belacustoms.utils.NBTEditor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Point d'entree principal pour identifier et distribuer des custom items.
 * Utilise par la commande /citem et par les listeners.
 */
public class CustomItemManager {

    private final CustomItemRegistry registry;

    public CustomItemManager(CustomItemRegistry registry) {
        this.registry = registry;
    }

    public CustomItemRegistry getRegistry() {
        return registry;
    }

    /** @return true si l'ItemStack porte un tag NBT CustomItemId connu du registre. */
    public boolean isCustomItem(ItemStack item) {
        return getId(item) != null;
    }

    /** @return l'identifiant interne stocke dans le NBT de l'item, ou null. */
    public String getId(ItemStack item) {
        if (item == null) {
            return null;
        }
        return NBTEditor.getCustomId(item);
    }

    /** @return la definition CustomItem correspondant a l'ItemStack donne, si presente en NBT et connue du registre. */
    public Optional<CustomItem> getCustomItem(ItemStack item) {
        String id = getId(item);
        if (id == null) {
            return Optional.empty();
        }
        return registry.get(id);
    }

    /**
     * Donne `amount` exemplaires de l'item `id` au joueur.
     * @return true si l'id existait dans le registre.
     */
    public boolean give(Player player, String id, int amount) {
        Optional<CustomItem> opt = registry.get(id);
        if (!opt.isPresent()) {
            return false;
        }
        CustomItem definition = opt.get();
        for (int i = 0; i < amount; i++) {
            ItemStack stack = definition.build();
            player.getInventory().addItem(stack).values()
                    .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
        return true;
    }
}
