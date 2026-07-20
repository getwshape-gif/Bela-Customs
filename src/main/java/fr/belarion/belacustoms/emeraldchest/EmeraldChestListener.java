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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Gère le cycle de vie complet du Coffre en Émeraude posé dans le monde :
 * - Pose : refusée si un autre Coffre en Émeraude est déjà présent sur un
 *   côté horizontal adjacent (voir hasAdjacentEmeraldChest ci-dessous),
 *   sinon enregistre sa position dans EmeraldChestManager. Le bloc reste
 *   toujours Material.TRAPPED_CHEST (jamais changé), pour que la texture
 *   du pack de ressources s'applique systématiquement.
 * - Ouverture : intercepte le clic droit vanilla (sauf si le joueur est en
 *   train de sneak, voir plus bas) et ouvre à la place un inventaire
 *   virtuel de 54 slots — c'est ce qui garantit à la fois l'apparence
 *   "coffre simple" et la capacité "double coffre", et qui empêche toute
 *   fusion FONCTIONNELLE avec un coffre voisin puisque le vrai tile entity
 *   du bloc n'est jamais utilisé.
 * - Fermeture : sauvegarde le contenu de l'inventaire virtuel.
 * - Explosion : résiste à EmeraldChestManager.MAX_EXPLOSIONS (5) explosions
 *   de n'importe quelle source (TNT, Creeper, ou toute autre source côté
 *   serveur), puis est détruit normalement (comme n'importe quel bloc,
 *   contenu déversé au sol comme un vrai coffre vanilla détruit par une
 *   explosion).
 * - Cassage : nécessite une pioche (comme les autres blocs premium du
 *   plugin, voir BlockProtectionListener), déverse le contenu au sol et
 *   redonne l'item Coffre en Émeraude au joueur.
 *
 * Fusion VISUELLE vanilla (double coffre) : Minecraft 1.8 fusionne
 * TOUJOURS visuellement deux blocs adjacents du MÊME Material (ici
 * TRAPPED_CHEST-TRAPPED_CHEST) en un seul modèle "double coffre",
 * indépendamment de toute logique du plugin — c'est un comportement du
 * moteur de rendu vanilla basé uniquement sur le Material réel des deux
 * blocs, qu'aucun pack de ressources ni code serveur ne peut annuler tant
 * que le Material reste identique des deux côtés. Une précédente tentative
 * consistait à alterner CHEST/TRAPPED_CHEST en damier pour casser cette
 * égalité de Material ; elle a été abandonnée car seul TRAPPED_CHEST porte
 * la texture personnalisée du Coffre en Émeraude dans le pack de
 * ressources du serveur, ce qui faisait apparaître un coffre sur deux avec
 * l'apparence vanilla par défaut. La pose est donc désormais tout
 * simplement refusée quand elle collerait deux Coffres en Émeraude
 * ensemble (voir onPlace), ce qui garantit à la fois une texture toujours
 * correcte et l'absence totale de fusion, sans dépendre du pack de
 * ressources ni de paquets réseau bas niveau.
 */
public class EmeraldChestListener implements Listener {

    /** Aucune fusion verticale n'existe en vanilla : seuls les 4 côtés horizontaux comptent. */
    private static final BlockFace[] HORIZONTAL_FACES = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final EmeraldChestManager manager;

    public EmeraldChestListener(EmeraldChestManager manager) {
        this.manager = manager;
    }

    private boolean isChestLike(Material type) {
        return type == Material.TRAPPED_CHEST || type == Material.CHEST;
    }

    private boolean isPickaxe(Material type) {
        return type == Material.WOOD_PICKAXE
                || type == Material.STONE_PICKAXE
                || type == Material.IRON_PICKAXE
                || type == Material.GOLD_PICKAXE
                || type == Material.DIAMOND_PICKAXE;
    }

    /**
     * Vrai si un Coffre en Émeraude déjà posé et suivi par le manager se
     * trouve sur l'un des 4 côtés horizontaux du bloc donné.
     */
    private boolean hasAdjacentEmeraldChest(Block block) {
        for (BlockFace face : HORIZONTAL_FACES) {
            Block neighbor = block.getRelative(face);
            if (isChestLike(neighbor.getType()) && manager.isTracked(neighbor.getLocation())) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !EmeraldChest.ID.equals(NBTEditor.getCustomId(item))) return;

        Block block = event.getBlock();

        // Empeche la pose collee a un autre Coffre en Emeraude : voir la
        // javadoc de la classe pour le raisonnement complet (fusion
        // visuelle vanilla impossible a eviter autrement sans casser la
        // texture du pack de ressources).
        if (hasAdjacentEmeraldChest(block)) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(event.getPlayer(), "emerald-chest.too-close");
            return;
        }

        manager.track(block.getLocation());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !isChestLike(block.getType())) return;
        if (!manager.isTracked(block.getLocation())) return;

        Player player = event.getPlayer();

        // Convention vanilla : un clic droit en sneak sur un conteneur ne
        // l'ouvre jamais, afin de permettre de poser un bloc contre lui
        // (au-dessus, en-dessous, sur un cote, etc.). On laisse simplement
        // l'evenement suivre son cours normal dans ce cas (pas de GUI).
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
        if (!isChestLike(block.getType())) return;
        if (!manager.isTracked(block.getLocation())) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || !isPickaxe(hand.getType())) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(player, "enchants.blocks.need-pickaxe");
            return;
        }

        // Cassage gere entierement a la main : le drop naturel vanilla d'un
        // CHEST/TRAPPED_CHEST ne correspond pas a notre item custom (nom/lore/NBT),
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
            if (!isChestLike(block.getType())) continue;
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
