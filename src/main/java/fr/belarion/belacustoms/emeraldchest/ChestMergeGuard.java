package fr.belarion.belacustoms.emeraldchest;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
* Empêche par réflexion NMS la fusion visuelle vanilla de deux coffres
* adjacents (Material.CHEST / Material.TRAPPED_CHEST) en un double coffre.
*
* Contexte technique (Minecraft/CraftBukkit 1.8, TileEntityChest) : la
* détection "coffre voisin" n'est calculée qu'UNE SEULE FOIS par
* TileEntityChest, au premier appel de sa méthode de vérification
* d'adjacence, grâce à un champ booléen "déjà vérifié" qui empêche tout
* nouveau calcul tant que le chunk reste chargé. Il n'existe aucune API
* Bukkit publique pour piloter ce comportement : cette classe force donc,
* par pure réflexion Java (AUCUNE dépendance de compilation sur les
* classes CraftBukkit/NMS — le plugin ne dépend que de spigot-api, voir
* pom.xml — donc aucun risque de casser la compilation), les champs
* internes de la TileEntityChest correspondante à l'état "déjà vérifiée,
* aucun voisin trouvé", ce qui empêche définitivement Minecraft de
* fusionner ce coffre avec quoi que ce soit.
*
* Les noms de champs NMS de la 1.8 sont obfusqués (une seule lettre) et
* peuvent varier légèrement d'une sous-révision à l'autre : plutôt que de
* viser un nom précis (fragile), cette classe repère les champs par leur
* STRUCTURE, stable depuis la 1.8 (vérifié sur le source déobfusqué
* net.minecraft.server.v1_8_R3.TileEntityChest) :
* - le champ "vérifié" est l'unique champ de type boolean déclaré
*   directement sur TileEntityChest ;
* - les 4 champs "voisin" sont les champs dont le type est
*   TileEntityChest lui-même (auto-référence), déclarés directement sur
*   cette même classe.
*
* Échoue silencieusement (un seul warning loggé) si cette structure ne
* correspond pas à celle attendue (ex : changement de version de
* serveur) : les Coffres en Émeraude restent alors pleinement
* fonctionnels (inventaire virtuel géré par EmeraldChestManager /
* EmeraldChestListener, totalement indépendant de ce mécanisme), seule la
* protection anti-fusion visuelle serait indisponible.
*/
public final class ChestMergeGuard {

    private static boolean unavailable = false;
    private static String craftPackage;
    private static Field checkedField;
    private static List<Field> neighborFields;

    private ChestMergeGuard() {
    }

    /**
     * A appeler juste apres la pose d'un Coffre en Emeraude (ou d'un bloc
     * adjacent a un Coffre en Emeraude deja trace), et a nouveau au
     * chargement du chunk qui le contient (voir EmeraldChestListener).
     */
    public static void preventMerge(Block block) {
        if (unavailable || block == null) return;

        try {
            Object tileEntity = getTileEntity(block);
            if (tileEntity == null) return;

            resolveFields(tileEntity.getClass());
            if (checkedField == null || neighborFields.size() != 4) {
                throw new IllegalStateException("structure TileEntityChest inattendue (champs non trouves)");
            }

            checkedField.set(tileEntity, true);
            for (Field neighborField : neighborFields) {
                neighborField.set(tileEntity, null);
            }
        } catch (Throwable t) {
            unavailable = true;
            Bukkit.getLogger().warning("[Bela-Customs] Protection anti-fusion des Coffres en Emeraude indisponible sur cette version de serveur (" + t + "). Les coffres restent pleinement fonctionnels, seule cette protection visuelle est desactivee.");
        }
    }

    private static void resolveFields(Class<?> teChestClass) {
        if (checkedField != null) return; // deja resolu (mis en cache apres le premier appel reussi)

        List<Field> booleans = new ArrayList<>();
        List<Field> selfTyped = new ArrayList<>();
        for (Field field : teChestClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType() == boolean.class) {
                booleans.add(field);
            } else if (field.getType() == teChestClass) {
                selfTyped.add(field);
            }
        }

        if (booleans.size() == 1) {
            checkedField = booleans.get(0);
        }
        neighborFields = selfTyped;
    }

    private static Object getTileEntity(Block block) throws Exception {
        Object craftWorld = block.getWorld();
        Method getHandle = craftWorld.getClass().getMethod("getHandle");
        Object nmsWorld = getHandle.invoke(craftWorld);

        if (craftPackage == null) {
            craftPackage = Bukkit.getServer().getClass().getPackage().getName();
        }
        String nmsPackage = craftPackage.replace("org.bukkit.craftbukkit", "net.minecraft.server");

        Class<?> blockPositionClass = Class.forName(nmsPackage + ".BlockPosition");
        Object blockPosition = blockPositionClass.getConstructor(int.class, int.class, int.class)
                .newInstance(block.getX(), block.getY(), block.getZ());

        Method getTileEntity = nmsWorld.getClass().getMethod("getTileEntity", blockPositionClass);
        return getTileEntity.invoke(nmsWorld, blockPosition);
    }
}

