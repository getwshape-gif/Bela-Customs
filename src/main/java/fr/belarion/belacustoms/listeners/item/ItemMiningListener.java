package fr.belarion.belacustoms.listeners.item;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.MiningTool;
import fr.belarion.belacustoms.api.TreeFeller;
import fr.belarion.belacustoms.compatibility.ProtectionHook;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldAxe;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldShovel;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldShovel;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import fr.belarion.belacustoms.utils.AreaBreakUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Gere la destruction de zone pour le Hammer et la Pelle, la colonne 1x3
 * pour la Hache emeraude simple, et le Tree Capitator pour la Hache emeraude
 * renforcee. La Pioche emeraude n'a pas de comportement special ici (elle
 * casse normalement un seul bloc, plus vite, comme n'importe quel outil).
 *
 * Toutes les destructions automatiques passent par ProtectionHook.canBreak
 * avant Block#breakNaturally, conformement a la regle du cahier des charges.
 */
public class ItemMiningListener implements Listener {

    private static final Set<Material> SHOVEL_COMPATIBLE = EnumSet.of(
            Material.DIRT, Material.GRASS, Material.SAND, Material.GRAVEL,
            Material.CLAY, Material.SOIL, Material.MYCEL, Material.SOUL_SAND, Material.SNOW_BLOCK
    );

    private static final Set<Material> LOG_TYPES = EnumSet.of(Material.LOG, Material.LOG_2);

    private static final int TREE_FELLER_MAX_BLOCKS = 256;

    private final CustomItemManager manager;

    public ItemMiningListener(CustomItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ProtectionHook.isProbing()) {
            // Cet event est une sonde emise par ProtectionHook.canBreak() pour un
            // AUTRE bloc de la zone : on ne doit pas la retraiter comme un nouveau
            // clic joueur, sinon boucle infinie (StackOverflowError).
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        Optional<CustomItem> opt = manager.getCustomItem(hand);
        if (!opt.isPresent()) {
            return;
        }

        CustomItem custom = opt.get();
        Block center = event.getBlock();
        Material originalType = center.getType();

        if (custom instanceof TreeFeller) {
            if (LOG_TYPES.contains(originalType)) {
                handleTreeFeller(player, center, hand);
            }
            return;
        }

        if (!(custom instanceof MiningTool)) {
            return;
        }

        MiningTool tool = (MiningTool) custom;
        if (tool.getRadius() <= 0) {
            return;
        }

        if (custom.getId().equals(EmeraldAxe.ID)) {
            if (!LOG_TYPES.contains(originalType)) {
                return;
            }
            handleColumn(player, center, originalType, tool.getRadius(), hand);
            return;
        }

        boolean isShovel = custom.getId().equals(EmeraldShovel.ID) || custom.getId().equals(ReinforcedEmeraldShovel.ID);
        if (isShovel && !SHOVEL_COMPATIBLE.contains(originalType)) {
            return;
        }

        BlockFace face = AreaBreakUtil.getFace(player);
        List<Block> area = AreaBreakUtil.getPlaneArea(center, face, tool.getRadius());
        breakArea(player, area, originalType, tool.sameBlockTypeOnly(), hand);
    }

    private void breakArea(Player player, List<Block> blocks, Material requiredType, boolean sameTypeOnly, ItemStack tool) {
        for (Block block : blocks) {
            if (block.getType() == Material.AIR) {
                continue;
            }
            if (sameTypeOnly && block.getType() != requiredType) {
                continue;
            }
            if (!ProtectionHook.canBreak(player, block)) {
                continue;
            }
            block.breakNaturally(tool);
        }
    }

    private void handleColumn(Player player, Block center, Material requiredType, int length, ItemStack tool) {
        Block current = center;
        for (int i = 1; i < length; i++) {
            current = current.getRelative(BlockFace.UP);
            if (current.getType() != requiredType) {
                break;
            }
            if (!ProtectionHook.canBreak(player, current)) {
                continue;
            }
            current.breakNaturally(tool);
        }
    }

    private void handleTreeFeller(Player player, Block origin, ItemStack tool) {
        Material logType = origin.getType();
        Set<Block> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(origin);

        int processed = 0;
        BlockFace[] directions = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        while (!queue.isEmpty() && processed < TREE_FELLER_MAX_BLOCKS) {
            Block current = queue.poll();
            processed++;

            if (!current.equals(origin)) {
                if (ProtectionHook.canBreak(player, current)) {
                    current.breakNaturally(tool);
                }
            }

            for (BlockFace face : directions) {
                Block neighbor = current.getRelative(face);
                if (visited.contains(neighbor)) {
                    continue;
                }
                if (neighbor.getType() == logType) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }
}
