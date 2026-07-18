package fr.belarion.belacustoms.listeners.item;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ReplantingHoe;
import fr.belarion.belacustoms.compatibility.ProtectionHook;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import fr.belarion.belacustoms.utils.AreaBreakUtil;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gere la recolte + replantation automatique des houes emeraude.
 *
 * Ble, carottes et pommes de terre sont nativement presents en 1.8.x.
 * La betterave (Beetroot) n'existe qu'a partir de Minecraft 1.9 : son
 * support est enregistre de facon defensive (putIfExists) et s'activera
 * automatiquement sans modification de code si ce plugin tourne un jour
 * sur une version plus recente ; sur un serveur strictement 1.8.x elle est
 * simplement ignoree (aucune erreur, aucun crash).
 */
public class ItemFarmingListener implements Listener {

    private static final Map<Material, Material> CROP_TO_SEED = new HashMap<>();

    static {
        CROP_TO_SEED.put(Material.CROPS, Material.SEEDS);
        CROP_TO_SEED.put(Material.CARROT, Material.CARROT_ITEM);
        CROP_TO_SEED.put(Material.POTATO, Material.POTATO_ITEM);
        putIfExists("BEETROOT_BLOCK", "BEETROOT_SEEDS");
    }

    private static void putIfExists(String cropName, String seedName) {
        try {
            Material crop = Material.valueOf(cropName);
            Material seed = Material.valueOf(seedName);
            CROP_TO_SEED.put(crop, seed);
        } catch (IllegalArgumentException ex) {
            // Culture indisponible sur cette version du serveur : ignoree silencieusement.
        }
    }

    private final CustomItemManager manager;

    public ItemFarmingListener(CustomItemManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCropBreak(BlockBreakEvent event) {
        if (ProtectionHook.isProbing()) {
            // Idem ItemMiningListener : ignorer les sondes emises par ProtectionHook
            // pour eviter une recursion infinie.
            return;
        }

        Block block = event.getBlock();
        Material cropType = block.getType();
        if (!CROP_TO_SEED.containsKey(cropType) || !isFullyGrown(block)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack hand = player.getItemInHand();
        Optional<CustomItem> opt = manager.getCustomItem(hand);
        if (!opt.isPresent() || !(opt.get() instanceof ReplantingHoe)) {
            return;
        }

        ReplantingHoe hoe = (ReplantingHoe) opt.get();
        Material seed = CROP_TO_SEED.get(cropType);

        // On gere nous-memes la destruction + replantation du bloc central :
        // on annule l'event vanilla pour eviter un double traitement du bloc.
        event.setCancelled(true);
        replant(player, block, cropType, seed);

        if (hoe.getRadius() > 0) {
            BlockFace face = AreaBreakUtil.getFace(player);
            List<Block> area = AreaBreakUtil.getPlaneArea(block, face, hoe.getRadius());
            for (Block other : area) {
                if (other.getType() != cropType || !isFullyGrown(other)) {
                    continue;
                }
                if (!ProtectionHook.canBreak(player, other)) {
                    continue;
                }
                replant(player, other, cropType, seed);
            }
        }
    }

    private boolean isFullyGrown(Block block) {
        MaterialData data = block.getState().getData();
        if (data instanceof Crops) {
            return ((Crops) data).getState() == CropState.RIPE;
        }
        return true;
    }

    /** Casse naturellement le bloc (drops normaux) puis le replante si le joueur possede la graine. */
    private void replant(Player player, Block block, Material cropType, Material seedItem) {
        ItemStack tool = player.getItemInHand();
        block.breakNaturally(tool);

        if (!consumeSeed(player, seedItem)) {
            return; // pas de graine : recolte simple, sans replantation
        }

        block.setType(cropType);
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Crops) {
            ((Crops) data).setState(CropState.SEEDED);
            state.setData(data);
            state.update(true, false);
        }
    }

    private boolean consumeSeed(Player player, Material seedItem) {
        ItemStack probe = new ItemStack(seedItem, 1);
        if (!player.getInventory().containsAtLeast(probe, 1)) {
            return false;
        }
        player.getInventory().removeItem(probe);
        return true;
    }
}
