package fr.belarion.belacustoms.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Verifie si un joueur est autorise a casser un bloc AVANT toute destruction
 * automatique declenchee par un custom item (hammer, pelle, tree capitator...).
 *
 * Technique utilisee : au lieu de dependre "en dur" de Factions ou de
 * WorldGuard (ce qui obligerait a lier le plugin a une API precise et a la
 * maintenir a chaque changement de version de ces plugins tiers), on emet
 * un BlockBreakEvent "sonde" pour le joueur et le bloc concerne, sans
 * jamais casser reellement le bloc avec cet event. Tous les plugins de
 * protection presents sur le serveur (Factions, FactionsUUID, WorldGuard,
 * GriefPrevention, Towny, etc.) ecoutent deja BlockBreakEvent et l'annulent
 * (event.setCancelled(true)) si le joueur n'a pas la permission de casser
 * a cet endroit. Il suffit donc de lire event.isCancelled() apres l'appel
 * pour savoir, de facon totalement generique, si la destruction reelle
 * (via Block#breakNaturally) doit avoir lieu.
 *
 * Cette approche garantit la compatibilite avec N'IMPORTE QUEL plugin de
 * protection sans dependance de compilation, conformement a la regle du
 * cahier des charges : "Avant chaque destruction automatique : verifier si
 * le joueur peut casser le bloc."
 */
public final class ProtectionHook {

    /**
     * Garde de reentrance : indique qu'un BlockBreakEvent actuellement
     * distribue est une SONDE emise par canBreak(), et non un vrai clic
     * joueur. Sans ce garde, les listeners de ce plugin qui ecoutent aussi
     * BlockBreakEvent reagiraient a leur propre sonde et en emettraient une
     * nouvelle, provoquant une recursion infinie (StackOverflowError).
     * Chaque listener doit verifier ProtectionHook.isProbing() en tout debut
     * de methode et retourner immediatement si vrai.
     */
    private static final ThreadLocal<Boolean> PROBING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ProtectionHook() {
    }

    public static boolean isProbing() {
        return PROBING.get();
    }

    public static boolean canBreak(Player player, Block block) {
        if (PROBING.get()) {
            // Securite supplementaire : ne jamais imbriquer une sonde dans une autre sonde.
            return true;
        }
        PROBING.set(Boolean.TRUE);
        try {
            BlockBreakEvent probe = new BlockBreakEvent(block, player);
            Bukkit.getPluginManager().callEvent(probe);
            return !probe.isCancelled();
        } finally {
            PROBING.set(Boolean.FALSE);
        }
    }
}
