package fr.belarion.belacustoms.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcule les zones de blocs (plan perpendiculaire au regard du joueur)
 * utilisees par les outils de zone des custom items (hammer, pelle).
 */
public final class AreaBreakUtil {

    private AreaBreakUtil() {
    }

    /** Determine la face regardee par le joueur (haut/bas/nord/sud/est/ouest). */
    public static BlockFace getFace(Player player) {
        Vector dir = player.getLocation().getDirection();
        double x = Math.abs(dir.getX());
        double y = Math.abs(dir.getY());
        double z = Math.abs(dir.getZ());

        if (y > x && y > z) {
            return dir.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        } else if (x > z) {
            return dir.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            return dir.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }
    }

    /**
     * @return tous les blocs du plan perpendiculaire a `face`, centres sur
     * `center`, sur un rayon donne (1 = 3x3, 2 = 5x5), sans inclure `center`.
     */
    public static List<Block> getPlaneArea(Block center, BlockFace face, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                Block relative;
                switch (face) {
                    case UP:
                    case DOWN:
                        relative = center.getRelative(a, 0, b);
                        break;
                    case EAST:
                    case WEST:
                        relative = center.getRelative(0, a, b);
                        break;
                    default:
                        relative = center.getRelative(a, b, 0);
                        break;
                }
                blocks.add(relative);
            }
        }
        return blocks;
    }
}
