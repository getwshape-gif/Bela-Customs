package fr.belarion.belacustoms.customenchants;

import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Tache centrale unique qui gere les effets passifs "permanents" des Custom
 * Enchants (Speed, Strength, Fire Resistance, Haste II).
 *
 * Ces effets sont appliques avec une duree tres longue (LONG_DURATION) et ne
 * sont RE-appliques que lorsqu'ils sont absents ou sur le point d'expirer
 * (REFRESH_THRESHOLD) : contrairement a une ancienne version qui reappliquait
 * un effet de 3 secondes toutes les secondes (ce qui faisait clignoter un
 * timer visible autour de "2 secondes"), le joueur voit ici un effet stable,
 * sans interruption ni clignotement. Des qu'un enchant n'est plus actif
 * (item retire), l'effet correspondant est retire immediatement au lieu
 * d'attendre son expiration naturelle.
 *
 * Anti Debuff n'est plus gere ici : la reactivite requise (bloquer un
 * debuff avant meme qu'il soit visible) est assuree par AntiDebuffGuardTask
 * (frequence tres elevee) et par le listener de protection anti-potion
 * (annulation des potions lancees avant application).
 */
public class EffectManager extends BukkitRunnable {

    /** Durée très longue (~13h88) utilisée pour simuler un effet permanent. */
    private static final int LONG_DURATION = 1_000_000;
    /** Si la duree restante d'un effet descend sous ce seuil, on le rafraichit. */
    private static final int REFRESH_THRESHOLD = 40;

    public static final PotionEffectType[] NEGATIVE_EFFECTS = new PotionEffectType[]{
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS,
            PotionEffectType.BLINDNESS,
            PotionEffectType.WITHER,
            PotionEffectType.SLOW_DIGGING,
            PotionEffectType.HUNGER,
            PotionEffectType.CONFUSION
    };

    public static boolean isNegative(PotionEffectType type) {
        for (PotionEffectType negative : NEGATIVE_EFFECTS) {
            if (negative.equals(type)) return true;
        }
        return false;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handlePlayer(player);
        }
    }

    private void handlePlayer(Player player) {
        PlayerInventory inv = player.getInventory();

        Set<CustomEnchant> active = EnumSet.noneOf(CustomEnchant.class);
        collect(inv.getBoots(), active);
        collect(inv.getHelmet(), active);
        collect(inv.getChestplate(), active);
        collect(inv.getLeggings(), active);

        ItemStack held = inv.getItemInHand();
        List<CustomEnchant> heldEnchants = held != null ? EnchantStorage.getEnchants(held) : null;
        boolean haste = heldEnchants != null && heldEnchants.contains(CustomEnchant.HASTE_II);

        applyOrRemove(player, active.contains(CustomEnchant.SPEED), PotionEffectType.SPEED, 1);
        applyOrRemove(player, active.contains(CustomEnchant.STRENGTH), PotionEffectType.INCREASE_DAMAGE, 0);
        applyOrRemove(player, active.contains(CustomEnchant.FIRE_RESISTANCE), PotionEffectType.FIRE_RESISTANCE, 0);
        applyOrRemove(player, haste, PotionEffectType.FAST_DIGGING, 1);
    }

    private void applyOrRemove(Player player, boolean shouldBeActive, PotionEffectType type, int amplifier) {
        if (shouldBeActive) {
            applyPermanent(player, type, amplifier);
        } else if (player.hasPotionEffect(type)) {
            player.removePotionEffect(type);
        }
    }

    /**
     * N'ecrit un nouvel effet que si necessaire (absent, mauvais amplifier,
     * ou duree restante trop courte) pour ne jamais faire clignoter/reset
     * un timer visible pour rien.
     */
    private void applyPermanent(Player player, PotionEffectType type, int amplifier) {
        for (PotionEffect existing : player.getActivePotionEffects()) {
            if (existing.getType().equals(type)) {
                if (existing.getAmplifier() == amplifier && existing.getDuration() > REFRESH_THRESHOLD) {
                    return;
                }
                break;
            }
        }
        player.addPotionEffect(new PotionEffect(type, LONG_DURATION, amplifier, true, false), true);
    }

    private void collect(ItemStack item, Set<CustomEnchant> into) {
        if (item == null || !ItemTierUtil.isEmeraldTier(item)) return;
        into.addAll(EnchantStorage.getEnchants(item));
    }
}
