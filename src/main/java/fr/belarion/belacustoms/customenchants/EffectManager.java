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
 * IMPORTANT (potions vanilla) : quand l'enchant n'est PAS/plus actif, on ne
 * doit JAMAIS retirer aveuglement un effet du meme type present sur le
 * joueur, car ce pourrait etre une vraie potion bue ou lancee (Speed,
 * Strength, Fire Resistance, Haste sont des effets de potion vanilla tout a
 * fait normaux). On ne retire donc que l'effet qui porte clairement la
 * signature de notre propre effet permanent (duree residuelle enorme, voir
 * OWN_EFFECT_DURATION_THRESHOLD) : une vraie potion, meme prolongee au
 * Redstone, ne s'approche jamais de cette duree. Voir removeIfOwnEffect().
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
    /**
     * Seuil bien au-dessus de toute duree de potion vanilla realiste (meme
     * prolongee au Redstone), utilise pour reconnaitre sans ambiguite un
     * effet applique par ce manager avant de le retirer automatiquement.
     */
    private static final int OWN_EFFECT_DURATION_THRESHOLD = LONG_DURATION / 2;

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
        } else {
            removeIfOwnEffect(player, type);
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

    /**
     * Retire l'effet du type donne UNIQUEMENT s'il s'agit clairement de
     * notre propre effet permanent (duree residuelle enorme). Une vraie
     * potion (bue ou lancee) du meme type, avec une duree vanilla normale,
     * n'est jamais touchee : elle continue son cours normalement, comme
     * n'importe quelle potion sur un joueur qui ne porte pas cet enchant.
     */
    private void removeIfOwnEffect(Player player, PotionEffectType type) {
        for (PotionEffect existing : player.getActivePotionEffects()) {
            if (existing.getType().equals(type)) {
                if (existing.getDuration() > OWN_EFFECT_DURATION_THRESHOLD) {
                    player.removePotionEffect(type);
                }
                return;
            }
        }
    }

    private void collect(ItemStack item, Set<CustomEnchant> into) {
        if (item == null || !ItemTierUtil.isEmeraldTier(item)) return;
        into.addAll(EnchantStorage.getEnchants(item));
    }
}
