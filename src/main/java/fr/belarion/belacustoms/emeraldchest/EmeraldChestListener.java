package fr.belarion.belacustoms.emeraldchest;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldChest;
import fr.belarion.belacustoms.utils.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
* Gere le cycle de vie complet du Coffre en Emeraude pose dans le monde :
* - Pose : enregistre sa position dans EmeraldChestManager. Aucune verification
    * d'adjacence n'est plus necessaire - voir EmeraldChest pour le choix de
    * Material.ENDER_CHEST, qui ne fusionne jamais avec quoi que ce soit par
    * construction du jeu (contrairement a l'ancienne implementation basee sur
    * Material.TRAPPED_CHEST, qui necessitait soit un hack NMS dedie
    * (ChestMergeGuard), soit de refuser la pose adjacente - les deux
    * supprimes, la pose est desormais toujours autorisee cote a cote).
    * - Ouverture : intercepte TOUJOURS le clic droit vanilla et ouvre a la
    * place un inventaire virtuel de 54 slots - c'est ce qui garantit a la
    * fois l'apparence "coffre simple" et la capacite "double coffre", et
    * qui empeche toute interaction avec le vrai stockage personnel Ender
    * Chest du joueur (jamais atteint).
    * - Fermeture : sauvegarde le contenu de l'inventaire virtuel.
    * - Explosion : resiste a EmeraldChestManager.MAX_EXPLOSIONS (5) explosions
    * de n'importe quelle source (TNT, Creeper, ou toute autre source cote
    * serveur), puis est detruit normalement (comme n'importe quel bloc,
                                              * contenu deverse au sol comme un vrai Coffre de l'End detruit par une
    * explosion).
* - Cassage : necessite une pioche (comme les autres blocs premium du
    * plugin, voir BlockProtectionListener), deverse le contenu au sol et
    * redonne l'item Coffre en Emeraude au joueur.
    */
public class EmeraldChestListener implements Listener {

private final EmeraldChestManager manager;

public EmeraldChestListener(EmeraldChestManager manager) {
    this.manager = manager;
}

private boolean isPickaxe(Material type) {
    return type == Material.WOOD_PICKAXE
        || type == Material.STONE_PICKAXE
        || type == Material.IRON_PICKAXE
        || type == Material.GOLD_PICKAXE
        || type == Material.DIAMOND_PICKAXE;
}

@EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ENDER_CHEST) return;

    ItemStack item = event.getItemInHand();
        if (item == null || !EmeraldChest.ID.equals(NBTEditor.getCustomId(item))) return;

    manager.track(block.getLocation());
    }

@EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST) return;
        if (!manager.isTracked(block.getLocation())) return;

Player player = event.getPlayer();
       
       // Convention vanilla : un clic droit en sneak sur un conteneur ne
       // l'ouvre jamais, afin de permettre de poser un bloc ou un coffre
       // contre lui (au-dessus, en-dessous, sur un cote, etc.). On laisse
       // simplement l'evenement suivre son cours normal dans ce cas (pas de GUI).
       if (player.isSneaking()) return;
       
       event.setCancelled(true);
       Location loc = block.getLocation();
       
       Inventory inv = Bukkit.createInventory(
        new EmeraldChestHolder(loc),
        EmeraldChestManager.SIZE,
        ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "✦ Coffre en Émeraude ✦"
        );
        inv.setContents(manager.getContents(loc));
        player.openInventory(inv);
    }

@EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof EmeraldChestHolder)) return;
        Location loc = ((EmeraldChestHolder) holder).getLocation();
        manager.setContents(loc, event.getInventory().getContents());
    }

@EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ENDER_CHEST) return;
        if (!manager.isTracked(block.getLocation())) return;

    Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || !isPickaxe(hand.getType())) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(player, "enchants.blocks.need-pickaxe");
            return;
        }

    event.setCancelled(true);
        Location loc = block.getLocation();
        spillContents(loc);
        manager.untrack(loc);
        block.setType(Material.AIR);
        loc.getWorld().dropItemNaturally(loc, new EmeraldChest().build());
    }

@EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

@EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

private void handleExplosion(List<Block> blocks) {
    Iterator<Block> it = blocks.iterator();
    while (it.hasNext()) {
        Block block = it.next();
        if (block.getType() != Material.ENDER_CHEST) continue;
        Location loc = block.getLocation();
        if (!manager.isTracked(loc)) continue;

    boolean destroyed = manager.registerExplosionHit(loc);
        if (!destroyed) {
            it.remove();
        } else {
            spillContents(loc);
            manager.untrack(loc);
        }
    }
}

private void spillContents(Location loc) {
    ItemStack[] contents = manager.getContents(loc);
    for (ItemStack item : contents) {
        if (item != null && item.getType() != Material.AIR) {
            loc.getWorld().dropItemNaturally(loc, item);
        }
    }
}
}
