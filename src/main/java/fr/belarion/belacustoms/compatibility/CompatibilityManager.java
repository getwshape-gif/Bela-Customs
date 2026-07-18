package fr.belarion.belacustoms.compatibility;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.inventory.ItemStack;

/**
 * Point d'entree unique pour toute verification de compatibilite entre un
 * item et un Custom Enchant. Utilise par l'Enclume Emeraude et par tout
 * futur systeme d'application d'enchant : evite de repeter la meme logique
 * (tier + cible + anti-doublon) a plusieurs endroits.
 */
public final class CompatibilityManager {

    private CompatibilityManager() {}

    public enum Result {
        OK,
        WRONG_TIER,
        WRONG_TARGET,
        ALREADY_APPLIED
    }

    public static Result check(ItemStack target, CustomEnchant enchant) {
        if (!ItemTierUtil.isEmeraldTier(target)) {
            return Result.WRONG_TIER;
        }
        if (!enchant.getTarget().matches(target)) {
            return Result.WRONG_TARGET;
        }
        if (EnchantStorage.hasEnchant(target, enchant)) {
            return Result.ALREADY_APPLIED;
        }
        return Result.OK;
    }

    public static boolean canApply(ItemStack target, CustomEnchant enchant) {
        return check(target, enchant) == Result.OK;
    }
}
