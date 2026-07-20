package fr.belarion.belacustoms.emeraldchest;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldChest;
import fr.belarion.belacustoms.utils.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
* Gère le cycle de vie complet du Coffre en Émeraude posé dans le monde :
* - Pose : enregistre sa position dans EmeraldChestManager, puis applique
* ChestMergeGuard (voir plus bas) pour empêcher toute fusion visuelle
* vanilla avec un coffre adjacent.
* - Ouverture : intercepte TOUJOURS le clic droit vanilla et ouvre à la
* place un inventaire virtuel de 54 slots — c'est ce qui garantit à la
* fois l'apparence "coffre simple" et la capacité "double coffre", et qui
* empêche toute fusion FONCTIONNELLE avec un coffre voisin puisque le
* vrai tile entity du bloc n'est jamais utilisé.
* - Fermeture : sauvegarde le contenu de l'inventaire virtuel.
* - Explosion : résiste à EmeraldChestManager.MAX_EXPLOSIONS (5) explosions
* de n'importe quelle source (TNT, Creeper, ou toute autre source côté
* serveur), puis est détruit normalement (comme n'importe quel bloc,
* contenu déversé au sol comme un vrai coffre vanilla détruit par une
* explosion).
* - Cassage : nécessite une pioche (comme les autres blocs premium du
* plugin, voir BlockProtectionListener), déverse le contenu au sol et
* redonne l'item Coffre en Émeraude au joueur.
*
* Fusion VISUELLE vanilla (double coffre) : contrairement à la fusion
* fonctionnelle ci-dessus (déjà gérée), Minecraft 1.8 fusionne aussi
* visuellement deux TRAPPED_CHEST adjacents en un seul modèle "double
* coffre", indépendamment de toute logique du plugin — c'est un
* comportement du moteur de rendu vanilla. onPlace() et onChunkLoad()
* appliquent donc ChestMergeGuard sur chaque Coffre en Émeraude concerné
* (lui-même et ses voisins immédiats) pour empêcher définitivement ce
* rendu fusionné : chaque coffre reste visuellement indépendant, quel que
* soit le nombre posé côte à côte.
*/
public class EmeraldChestListener implements Listener {

    private static final BlockFace[] ADJACENT_FACES = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

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
        Material type = block.getType();
        boolean isChestLike = type == Material.TRAPPED_CHEST || type == Material.CHEST;

        ItemStack item = event.getItemInHand();
        boolean isEmeraldChest = item != null && EmeraldChest.ID.equals(NBTEditor.getCustomId(item));
        if (isEmeraldChest) {
            manager.track(block.getLocation());
        }

        if (!isChestLike) return;

        // N'intervient JAMAIS sur des coffres vanilla classiques sans lien
        // avec un Coffre en Émeraude : seul un Coffre en Émeraude, posé ici
        // ou déjà présent en voisin, déclenche la protection anti-fusion.
        boolean touchesEmeraldChest = isEmeraldChest;
        for (BlockFace face : ADJACENT_FACES) {
            Block neighbor = block.getRelative(face);
            Material neighborType = neighbor.getType();
            if (neighborType != Material.TRAPPED_CHEST && neighborType != Material.CHEST) continue;
            if (!manager.isTracked(neighbor.getLocation())) continue;

            touchesEmeraldChest = true;
            ChestMergeGuard.preventMerge(neighbor);
        }

        if (!touchesEmeraldChest) return;

        ChestMergeGuard.preventMerge(block);
        // Second passage différé : certains chemins vanilla ré-exécutent la
        // vérification d'adjacence juste après cet événement (première
        // interaction, mise à jour de bloc suivante) ; on s'assure donc que
        // l'état "aucun voisin" reste bien celui lu en dernier.
        Bukkit.getScheduler().runTask(BelaCustoms.get(), () -> {
            Material current = block.getType();
            if (current == Material.TRAPPED_CHEST || current == Material.CHEST) {
                ChestMergeGuard.preventMerge(block);
            }
        });
    }

    /**
     * Ré-applique ChestMergeGuard à chaque Coffre en Émeraude tracé situé
     * dans un chunk qui vient d'être chargé : une TileEntityChest est
     * recréée à chaque chargement de chunk, ce qui réinitialise son état
     * "vérifié" et exposerait de nouveau le coffre à une fusion visuelle
     * sans cette ré-application.
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        List<Location> tracked = manager.getTrackedLocationsInChunk(
                event.getWorld().getName(), event.getChunk().getX(), event.getChunk().getZ());
        for (Location loc : tracked) {
            ChestMergeGuard.preventMerge(loc.getBlock());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.TRAPPED_CHEST) return;
        if (!manager.isTracked(block.getLocation())) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
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
        if (block.getType() != Material.TRAPPED_CHEST) return;
        if (!manager.isTracked(block.getLocation())) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || !isPickaxe(hand.getType())) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(player, "enchants.blocks.need-pickaxe");
            return;
        }

        // Cassage gere entierement a la main : le drop naturel vanilla d'un
        // TRAPPED_CHEST ne correspond pas a notre item custom (nom/lore/NBT),
        // on l'empeche donc et on redonne explicitement le bon item.
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
            if (block.getType() != Material.TRAPPED_CHEST) continue;
            Location loc = block.getLocation();
            if (!manager.isTracked(loc)) continue;

            boolean destroyed = manager.registerExplosionHit(loc);
            if (!destroyed) {
                // Survit a cette explosion (moins de MAX_EXPLOSIONS subies) :
                // on retire le bloc de la liste pour empecher sa destruction,
                // meme technique que BlockProtectionListener.
                it.remove();
            } else {
                // Explosion de trop : destruction normale, contenu deverse
                // comme un vrai coffre vanilla detruit par une explosion.
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
