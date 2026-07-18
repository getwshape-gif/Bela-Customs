package fr.belarion.belacustoms.customenchants;

import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Garde rapprochee d'Anti Debuff : retire instantanement tout effet de
 * potion negatif chez un joueur qui porte l'enchant, sur une frequence
 * tres elevee (toutes les 2 ticks, soit 0.1s) au lieu d'une fois par
 * seconde comme les autres effets passifs.
 *
 * Separee de EffectManager expres : l'API Bukkit/Spigot 1.8.8 ne fournit
 * pas d'evenement "avant application" generique pour un effet de potion
 * (EntityPotionEffectEvent n'existe qu'a partir de versions bien plus
 * recentes). La vraie prevention est geree en amont par le listener de
 * protection anti-potion pour les potions lancees (PotionSplashEvent,
 * annulees avant meme d'atteindre le joueur) ; cette tache est le filet de
 * securite pour toute autre source d'effet negatif (mob, commande, etc.),
 * avec une latence maximale de 0.1s au lieu d'1s.
 */
public class AntiDebuffGuardTask extends BukkitRunnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!hasAntiDebuff(player)) continue;

            for (PotionEffectType type : EffectManager.NEGATIVE_EFFECTS) {
                if (player.hasPotionEffect(type)) {
                    player.removePotionEffect(type);
                }
            }
        }
    }

    private boolean hasAntiDebuff(Player player) {
        PlayerInventory inv = player.getInventory();
        return hasOn(inv.getBoots()) || hasOn(inv.getHelmet())
                || hasOn(inv.getChestplate()) || hasOn(inv.getLeggings());
    }

    private boolean hasOn(ItemStack item) {
        return item != null && ItemTierUtil.isEmeraldTier(item)
                && EnchantStorage.hasEnchant(item, CustomEnchant.ANTI_DEBUFF);
    }
}
