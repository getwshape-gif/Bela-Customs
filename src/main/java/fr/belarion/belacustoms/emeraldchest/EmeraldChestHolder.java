package fr.belarion.belacustoms.emeraldchest;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Porteur d'un inventaire virtuel de Coffre en Émeraude : sert uniquement à
 * retrouver la position du bloc dans le monde lorsque l'inventaire se ferme
 * (voir EmeraldChestListener.onClose), pour sauvegarder son contenu via
 * EmeraldChestManager.
 */
public class EmeraldChestHolder implements InventoryHolder {

    private final Location location;

    public EmeraldChestHolder(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public Inventory getInventory() {
        // Jamais utilisé : ce holder ne sert que de porte-métadonnée
        // (localisation), l'inventaire réel est créé séparément par
        // Bukkit.createInventory() dans EmeraldChestListener.onInteract().
        return null;
    }
}
