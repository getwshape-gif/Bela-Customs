package fr.belarion.belacustoms.listeners;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldAnvilItem;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldEnchantTableItem;
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
 *   des blocs premium résistants aux explosions, jamais des blocs
 *   totalement indestructibles.
 * - impossibles à déplacer par un piston.
 * - cassables normalement par un joueur avec une pioche (comme n'importe
 *   quel bloc solide).
 *
 * Bloc de la Table d'Enchantement Émeraude : Material.PRISMARINE avec
 * data 2 (Dark Prismarine) — remplace l'ancien Material.EMERALD_BLOCK.
 * Seule cette variante précise (data 2) est protégée : un Prismarine ou
 * Prismarine Bricks (data 0/1) classique reste un bloc vanilla normal.
 *
 * Bloc de l'Enclume Émeraude : Material.SEA_LANTERN. Ce n'est pas une
 * vraie enclume vanilla — c'est un bloc déclencheur qui ouvre un GUI 100%
 * custom avec un coût fixe en niveaux.
 *
 * IMPORTANT (drop au cassage) : Bukkit 1.8 ne conserve aucun NBT sur un
 * bloc plein une fois posé (pas de tile entity pour ces blocs), donc un
 * cassage vanilla "laissé suivre son cours" redonnerait un bloc vanilla
 * brut (Prismarine / Sea Lantern) SANS nom, lore, NBT CustomItemId ni
 * texture custom — pas le vrai objet que le joueur a posé. onBreak()
 * annule donc systématiquement le cassage vanilla pour ces deux blocs et
 * reconstruit lui-même l'ItemStack CustomItem correspondant via
 * CustomItemRegistry (EMERALD_ENCHANT_TABLE / EMERALD_ANVIL), afin que le
 * joueur récupère bien l'objet custom complet (nom, lore, NBT, texture),
 * identique à celui obtenu via /citem.
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
     * Cassage manuel autorisé pour les deux blocs premium, uniquement à la
     * pioche (comme un bloc minéral classique). Dans les deux cas, le
     * cassage vanilla est annulé et remplacé par un drop de l'ItemStack
     * CustomItem réel (nom, lore, NBT CustomItemId, texture), reconstruit
     * via CustomItemRegistry — voir le commentaire de classe ci-dessus.
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

        boolean isAnvil = block.getType() == Material.SEA_LANTERN;
        String id = isAnvil ? EmeraldAnvilItem.ID : EmeraldEnchantTableItem.ID;
        Material fallback = isAnvil ? Material.SEA_LANTERN : Material.PRISMARINE;

        event.setCancelled(true);
        block.setType(Material.AIR);

        ItemStack drop = BelaCustoms.get().getCustomItemRegistry().get(id)
                .map(CustomItem::build)
                .orElseGet(() -> new ItemStack(fallback));
        block.getWorld().dropItemNaturally(block.getLocation(), drop);
    }
}
