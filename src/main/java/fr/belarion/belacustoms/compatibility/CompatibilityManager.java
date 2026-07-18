package fr.belarion.belacustoms.compatibility;

import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.inventory.ItemStack;

/**
 * Point d'entrée unique pour toute vérification de compatibilité entre un
 * item et un Custom Enchant. Utilisé par l'Enclume Émeraude et par tout
 * futur système d'application d'enchant : évite de répéter la même logique
 * (tier + cible + anti-doublon) à plusieurs endroits.
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
