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
 * Gère le cycle de vie complet du Coffre en Émeraude posé dans le monde :
 * - Pose : enregistre sa position dans EmeraldChestManager, puis force un
 * Material (CHEST ou TRAPPED_CHEST) en damier pour empêcher toute fusion
 * visuelle vanilla avec un autre Coffre en Émeraude adjacent (voir
 * enforceCheckerboard ci-dessous).
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
 * fonctionnelle ci-dessus (déjà gérée par l'inventaire virtuel), Minecraft
 * 1.8 fusionne aussi visuellement deux blocs adjacents du MÊME Material
 * (CHEST-CHEST ou TRAPPED_CHEST-TRAPPED_CHEST) en un seul modèle "double
 * coffre", indépendamment de toute logique du plugin — c'est un
 * comportement du moteur de rendu vanilla basé uniquement sur le Material
 * réel des deux blocs. Comme CHEST et TRAPPED_CHEST ne fusionnent JAMAIS
 * entre eux, on force un damier CHEST / TRAPPED_CHEST basé sur la parité
 * de (x + z) : deux blocs orthogonalement adjacents ont toujours des
 * coordonnées x+z de parité opposée, donc toujours un Material différent,
 * donc jamais de fusion, quel que soit le nombre de Coffres en Émeraude
 * posés côte à côte ou leur disposition (ligne, rangée complète, etc.).
 * Aucune texture personnalisée n'existe à ce jour pour ce coffre (modèle
 * vanilla par défaut, identique pour les deux Materials), ce choix de
 * Material réel n'a donc aucun impact visuel autre que la suppression de
 * la fusion. Le contenu (inventaire virtuel) et toutes les protections
 * (cassage, explosion) fonctionnent à l'identique pour les deux Materials
 * (voir isChestLike ci-dessous) : c'est une pure question d'anti-fusion,
 * aucune autre mécanique n'est affectée.
 */
public class EmeraldChestListener implements Listener {

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

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !EmeraldChest.ID.equals(NBTEditor.getCustomId(item))) return;

        Block block = event.getBlock();
        manager.track(block.getLocation());
        enforceCheckerboard(block);
    }

    /**
     * Force le Material du bloc posé (CHEST ou TRAPPED_CHEST) selon la
     * parité de (x + z) de sa position, afin qu'il ne partage jamais le
     * même Material qu'un voisin orthogonal — voir la javadoc de la
     * classe pour le détail du raisonnement.
     */
    private void enforceCheckerboard(Block block) {
        Material desired = Math.floorMod(block.getX() + block.getZ(), 2) == 0
                ? Material.TRAPPED_CHEST
                : Material.CHEST;
        if (block.getType() != desired) {
            byte data = block.getData();
            block.setType(desired);
            block.setData(data);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !isChestLike(block.getType())) return;
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
