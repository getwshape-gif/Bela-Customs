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
 * Protege les deux blocs premium partages par la Table d'Enchantement
 * Emeraude et l'Enclume Emeraude :
 * - insensibles a TOUTES les explosions (TNT / Creeper / autres) : ce sont
 *   des blocs premium resistants aux explosions, jamais des blocs
 *   totalement indestructibles.
 * - impossibles a deplacer par un piston.
 * - cassables normalement par un joueur avec une pioche (comme n'importe
 *   quel bloc solide), en respectant les protections/claims externes
 *   puisque l'evenement n'est jamais annule dans ce cas : le bloc est bien
 *   detruit et recupere par le joueur.
 *
 * L'Enclume Emeraude (Sea Lantern) n'est pas une vraie enclume vanilla —
 * c'est un bloc declencheur qui ouvre un GUI 100% custom avec un cout fixe
 * en niveaux. Il n'existe donc aucun etat "endommagee / tres endommagee /
 * detruite" a gerer : elle reste structurellement toujours "parfaite",
 * sans le moindre code supplementaire necessaire.
 *
 * Listener volontairement place au niveau du package `listeners` (et non
 * dans `emeraldanvil` ou `emeraldenchanttable`) car il protege les DEUX
 * blocs a la fois : c'est une regle transverse, pas propre a une seule
 * fonctionnalite.
 */
public class BlockProtectionListener implements Listener {

    private boolean isProtected(Material type) {
        return type == Material.EMERALD_BLOCK || type == Material.SEA_LANTERN;
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
            if (isProtected(it.next().getType())) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isProtected(block.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Cassage manuel autorise pour les deux blocs premium, uniquement a la
     * pioche (comme un bloc mineral classique). Aucune annulation en dehors
     * de ce controle d'outil : le joueur casse normalement le bloc, qui est
     * detruit et recupere, en respectant les eventuelles protections/claims
     * d'autres plugins (l'evenement n'est pas touche dans ce cas).
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (!isProtected(type)) return;

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        if (hand == null || !isPickaxe(hand.getType())) {
            event.setCancelled(true);
            BelaCustoms.get().getMessagesManager().send(player, "enchants.blocks.need-pickaxe");
        }
    }
}
