package fr.belarion.belacustoms.listeners.enchant;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.customenchants.EnchantTarget;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gere les Custom Enchants qui interviennent au minage : Vein Miner,
 * AutoSmelt et Aimantation (cote blocs mines). Tout part d'un seul
 * BlockBreakEvent pour rester leger.
 */
public class EnchantMiningListener implements Listener {

    private static final Set<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE, Material.LAPIS_ORE
    );

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getItemInHand();

        if (!ItemTierUtil.isEmeraldTier(tool)) return;

        List<CustomEnchant> enchants = EnchantStorage.getEnchants(tool);
        if (enchants.isEmpty()) return;

        boolean autosmelt = enchants.contains(CustomEnchant.AUTOSMELT);
        boolean magnet = enchants.contains(CustomEnchant.MAGNET);
        boolean veinMiner = enchants.contains(CustomEnchant.VEIN_MINER);

        Block origin = event.getBlock();
        Material originalType = origin.getType();

        if (autosmelt || magnet) {
            // BlockBreakEvent#setDropItems n'existe pas dans l'API Spigot 1.8.8 :
            // on annule l'event et on gere nous-memes la casse + les drops.
            List<ItemStack> drops = new ArrayList<ItemStack>(origin.getDrops(tool));
            if (autosmelt) smelt(drops);
            event.setCancelled(true);
            origin.setType(Material.AIR);
            deliver(player, origin, drops, magnet);
        }

        if (veinMiner && ORES.contains(originalType) && EnchantTarget.PICKAXE.matches(tool)) {
            mineVein(player, origin, originalType, tool, autosmelt, magnet);
        }
    }

    /** Parcours en largeur (BFS) du filon connecte, casse uniquement le meme minerai. */
    private void mineVein(Player player, Block origin, Material oreType, ItemStack tool,
                           boolean autosmelt, boolean magnet) {
        int max = BelaCustoms.get().getEnchantSettings().getVeinMinerMaxBlocks();

        Set<Block> visited = new HashSet<Block>();
        ArrayDeque<Block> queue = new ArrayDeque<Block>();
        visited.add(origin);
        queue.add(origin);

        int broken = 0;
        while (!queue.isEmpty() && broken < max) {
            Block current = queue.poll();

            if (current != origin) {
                if (current.getType() != oreType) continue;

                List<ItemStack> drops = new ArrayList<ItemStack>(current.getDrops(tool));
                if (autosmelt) smelt(drops);
                current.setType(Material.AIR);
                deliver(player, current, drops, magnet);
                broken++;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Block neighbor = current.getRelative(dx, dy, dz);
                        if (visited.contains(neighbor)) continue;
                        visited.add(neighbor);
                        if (neighbor.getType() == oreType) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private void smelt(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.getType() == Material.IRON_ORE) {
                drop.setType(Material.IRON_INGOT);
            } else if (drop.getType() == Material.GOLD_ORE) {
                drop.setType(Material.GOLD_INGOT);
            }
        }
    }

    private void deliver(Player player, Block source, List<ItemStack> drops, boolean magnet) {
        for (ItemStack drop : drops) {
            if (magnet) {
                Map<Integer, ItemStack> leftovers = player.getInventory().addItem(drop);
                for (ItemStack leftover : leftovers.values()) {
                    player.getWorld().dropItem(source.getLocation().add(0.5, 0.5, 0.5), leftover);
                }
            } else {
                source.getWorld().dropItemNaturally(source.getLocation(), drop);
            }
        }
    }
}
