package fr.belarion.belacustoms.compatibility;

import fr.belarion.belacustoms.BelaCustoms;
import fr.belarion.belacustoms.customenchants.CustomEnchant;
import fr.belarion.belacustoms.customenchants.EnchantStorage;
import fr.belarion.belacustoms.customenchants.EnchantTarget;
import fr.belarion.belacustoms.utils.ItemTierUtil;
import org.bukkit.inventory.ItemStack;

/**
 * Point d'entrée unique pour toute vérification de compatibilité entre un
 * item et un Custom Enchant. Utilisé par l'Enclume Émeraude et par tout
 * futur système d'application d'enchant : évite de répéter la même logique
 * (tier + cible + anti-doublon + limite d'équilibrage PvP) à plusieurs
 * endroits.
 */
public final class CompatibilityManager {

    private CompatibilityManager() {}

    public enum Result {
        OK,
        WRONG_TIER,
        WRONG_TARGET,
        ALREADY_APPLIED,
        ARMOR_LIMIT_REACHED
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
        // Équilibrage PvP : une pièce d'armure ne peut recevoir qu'un nombre
        // limité de Custom Enchants (armes/outils : pas de limite). Le test
        // porte sur le Material reel de l'item (EnchantTarget.ARMOR.matches),
        // pas sur la cible de "enchant", pour couvrir tout systeme actuel ou
        // futur qui tenterait d'appliquer un enchant sur une piece d'armure.
        if (EnchantTarget.ARMOR.matches(target)) {
            int max = BelaCustoms.get().getEnchantSettings().getMaxArmorEnchants();
            if (EnchantStorage.getEnchants(target).size() >= max) {
                return Result.ARMOR_LIMIT_REACHED;
            }
        }
        return Result.OK;
    }

    public static boolean canApply(ItemStack target, CustomEnchant enchant) {
        return check(target, enchant) == Result.OK;
    }
}
