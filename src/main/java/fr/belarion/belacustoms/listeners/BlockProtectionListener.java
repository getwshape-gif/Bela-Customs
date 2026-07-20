package fr.belarion.belacustoms.listeners;

import fr.belarion.belacustoms.BelaCustoms;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

/**
 * Protège les deux blocs premium partagés par la Table d'Enchantement
 * Émeraude et l'Enclume Émeraude :
 * - insensibles à TOUTES les explosions (TNT / Creeper / autres) : ce sont
 * des blocs premium résistants aux explosions, jamais des blocs
 * totalement indestructibles.
 * - impossibles à déplacer par un piston.
 * - cassables normalement par un joueur avec une pioche (comme n'importe
 * quel bloc solide), en respectant les protections/claims externes
 * puisque l'événement n'est jamais annulé dans ce cas : le bloc est bien
 * détruit et récupéré par le joueur.
 *
 * Bloc de la Table d'Enchantement Émeraude : Material.PRISMARINE avec
 * data 2 (Dark Prismarine) — remplace l'ancien Material.EMERALD_BLOCK.
 * Seule cette variante précise (data 2) est protégée : un Prismarine ou
 * Prismarine Bricks (data 0/1) classique reste un bloc vanilla normal.
 * Le Prismarine se drop deja lui-meme normalement sous pioche en vanilla
 * (aucune intervention necessaire ici).
 *
 * Bloc de l'Enclume Émeraude : Material.SEA_LANTERN. Ce n'est pas une
 * vraie enclume vanilla — c'est un bloc déclencheur qui ouvre un GUI 100%
 * custom avec un coût fixe en niveaux. Il n'existe donc aucun état
 * "endommagée / très endommagée / détruite" à gérer : elle reste
 * structurellement toujours "parfaite", sans le moindre code
 * supplémentaire nécessaire. Contrairement au Prismarine, Sea Lantern ne
 * se drop JAMAIS lui-meme sous pioche vanilla sans Silk Touch (il donne
 * des Prismarine Crystals) : onBreak() reproduit donc manuellement un
 * cassage "normal" pour ce bloc precis, afin qu'il redonne bien un Sea
 * Lantern (voir plus bas).
 *
 * Listener volontairement placé au niveau du package `listeners` (et non
 * dans `emeraldanvil` ou `emeraldenchanttable`) car il protège les DEUX
 * blocs à la fois : c'est une règle transverse, pas propre à une seule
 * fonctionnalité.
 */
public class BlockProtectionListener implements Listener {

    private boolean isProtected(Block block) {
        Material type = block.getType();
        if (type == Material.SEA_LANTERN) return true;
        return type == Material.PRISMARINE && block.getData() == 2;
    }

    private boolean isPickaxe(Material type) {
        return type == Material.WOOD_PICKAXE
                || type == Material.STONE_PICKAXE
                || type == Material.IRON_PICKAXE
                || type == Material.GOLD_PICKAXE
                || type == Material.DIAMOND_PICKAXE;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        filter(event.blockList());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        filter(event.blockList());
    }

    private void filter(List<Block> blocks) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            if (isProtected(it.next())) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Cassage manuel autorise pour les deux blocs premium, uniquement a la
     * pioche (comme un bloc mineral classique). Aucune annulation en dehors
     * de ce controle d'outil pour le Prismarine (le joueur casse normalement
     * le bloc, qui est detruit et recupere, en respectant les eventuelles
     * protections/claims d'autres plugins). Le Sea Lantern (Enclume
     * Émeraude) est un cas particulier : voir plus bas.
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!isProtected(block)) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || !isPickaxe(hand.getType())) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(player, "enchants.blocks.need-pickaxe");
            return;
        }

        // Material.SEA_LANTERN ne se drop JAMAIS lui-meme sous pioche sans
        // Silk Touch en vanilla (il donne des Prismarine Crystals) : on
        // reproduit donc manuellement un cassage "normal" qui redonne bien
        // un Sea Lantern, exactement comme n'importe quel autre bloc mine a
        // la pioche. Le Prismarine (Table d'Enchantement) se drop deja
        // correctement lui-meme en vanilla : aucune intervention necessaire,
        // on laisse l'evenement suivre son cours normal dans ce cas.
        if (block.getType() == Material.SEA_LANTERN) {
            event.setCancelled(true);
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SEA_LANTERN));
        }
    }
}
