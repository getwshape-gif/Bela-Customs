package fr.belarion.belacustoms.listeners.enchant;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EffectManager;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

/**
 * Vraie prevention (avant application) d'Anti Debuff pour les potions
 * lancees : si une potion contient un effet negatif, l'intensite est mise
 * a 0 pour chaque joueur protege parmi les entites touchees. Les autres
 * entites touchees par le meme jet ne sont pas affectees.
 *
 * Les potions vanilla ne melangent jamais un effet positif et un effet
 * negatif dans le meme jet : neutraliser tout le potion splash pour un
 * joueur protege ne bloque donc jamais un effet positif qui devrait
 * continuer a fonctionner.
 */
public class EnchantPotionProtectionListener implements Listener {

    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        boolean negative = false;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (EffectManager.isNegative(effect.getType())) {
                negative = true;
                break;
            }
        }
        if (!negative) return;

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player && hasAntiDebuff((Player) entity)) {
                event.setIntensity(entity, 0.0D);
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
