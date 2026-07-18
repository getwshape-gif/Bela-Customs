package fr.belarion.belacustoms.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

/**
 * Utilitaire d'acces au NBT brut d'un ItemStack via reflexion NMS.
 *
 * Pourquoi de la reflexion et pas l'API Bukkit standard ?
 * En Spigot 1.8.x, ItemMeta ne propose ni setUnbreakable() (ajoute en 1.11),
 * ni de PersistentDataContainer (ajoute en 1.14). Pour marquer un item comme
 * incassable (NBT "Unbreakable") ou pour lui attribuer un identifiant interne
 * invisible ("CustomItemId"), il faut donc manipuler le NBTTagCompound NMS
 * directement. Cette classe encapsule toute cette reflexion afin que le
 * reste du plugin n'ait jamais a en dependre directement.
 *
 * Compatible avec toutes les revisions NMS 1.8 (v1_8_R1, v1_8_R2, v1_8_R3)
 * puisque le nom de package est detecte dynamiquement au chargement.
 */
public final class NBTEditor {

    /** Cle NBT utilisee pour stocker l'identifiant interne du custom item. */
    public static final String CUSTOM_ID_KEY = "CustomItemId";

    private static final String VERSION;
    private static boolean available = true;

    static {
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        VERSION = pkg.substring(pkg.lastIndexOf('.') + 1);
    }

    private NBTEditor() {
    }

    public static boolean isAvailable() {
        return available;
    }

    private static Class<?> nmsClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + VERSION + "." + name);
    }

    private static Class<?> obcClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + name);
    }

    private static Object toNMSCopy(ItemStack item) throws Exception {
        Class<?> craftItemStack = obcClass("inventory.CraftItemStack");
        Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, item);
    }

    private static ItemStack toBukkitCopy(Object nmsItem) throws Exception {
        Class<?> craftItemStack = obcClass("inventory.CraftItemStack");
        Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", nmsClass("ItemStack"));
        return (ItemStack) asBukkitCopy.invoke(null, nmsItem);
    }

    private static Object getOrCreateTag(Object nmsItem) throws Exception {
        Class<?> nmsItemStackClass = nmsClass("ItemStack");
        Method hasTag = nmsItemStackClass.getMethod("hasTag");
        Method getTag = nmsItemStackClass.getMethod("getTag");
        Method setTag = nmsItemStackClass.getMethod("setTag", nmsClass("NBTTagCompound"));

        Object tag;
        if ((boolean) hasTag.invoke(nmsItem)) {
            tag = getTag.invoke(nmsItem);
        } else {
            tag = nmsClass("NBTTagCompound").newInstance();
            setTag.invoke(nmsItem, tag);
        }
        return tag;
    }

    /**
     * Ecrit une paire cle/valeur String dans le NBT racine de l'item et
     * retourne un NOUVEL ItemStack (les ItemStack Bukkit sont immuables du
     * point de vue de cette API : on remplace toujours la reference).
     */
    public static ItemStack setString(ItemStack item, String key, String value) {
        if (!available) return item;
        try {
            Object nmsItem = toNMSCopy(item);
            Object tag = getOrCreateTag(nmsItem);
            tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, key, value);
            return toBukkitCopy(nmsItem);
        } catch (Exception ex) {
            available = false;
            Bukkit.getLogger().warning("[Bela-Customs] NBTEditor indisponible sur cette version NMS (" + VERSION + "): " + ex);
            return item;
        }
    }

    public static String getString(ItemStack item, String key) {
        if (!available || item == null) return null;
        try {
            Object nmsItem = toNMSCopy(item);
            Class<?> nmsItemStackClass = nmsClass("ItemStack");
            boolean hasTag = (boolean) nmsItemStackClass.getMethod("hasTag").invoke(nmsItem);
            if (!hasTag) return null;
            Object tag = nmsItemStackClass.getMethod("getTag").invoke(nmsItem);
            boolean hasKey = (boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, key);
            if (!hasKey) return null;
            return (String) tag.getClass().getMethod("getString", String.class).invoke(tag, key);
        } catch (Exception ex) {
            return null;
        }
    }

    public static ItemStack setBoolean(ItemStack item, String key, boolean value) {
        if (!available) return item;
        try {
            Object nmsItem = toNMSCopy(item);
            Object tag = getOrCreateTag(nmsItem);
            tag.getClass().getMethod("setBoolean", String.class, boolean.class).invoke(tag, key, value);
            return toBukkitCopy(nmsItem);
        } catch (Exception ex) {
            available = false;
            Bukkit.getLogger().warning("[Bela-Customs] NBTEditor indisponible sur cette version NMS (" + VERSION + "): " + ex);
            return item;
        }
    }

    public static boolean getBoolean(ItemStack item, String key) {
        if (!available || item == null) return false;
        try {
            Object nmsItem = toNMSCopy(item);
            Class<?> nmsItemStackClass = nmsClass("ItemStack");
            boolean hasTag = (boolean) nmsItemStackClass.getMethod("hasTag").invoke(nmsItem);
            if (!hasTag) return false;
            Object tag = nmsItemStackClass.getMethod("getTag").invoke(nmsItem);
            boolean hasKey = (boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, key);
            if (!hasKey) return false;
            return (boolean) tag.getClass().getMethod("getBoolean", String.class).invoke(tag, key);
        } catch (Exception ex) {
            return false;
        }
    }

    /** Marque l'item comme incassable au sens NBT vanilla (tag "Unbreakable"). */
    public static ItemStack setUnbreakable(ItemStack item) {
        return setBoolean(item, "Unbreakable", true);
    }

    /**
     * @return true si l'item porte le tag NBT vanilla "Unbreakable" (voir
     * setUnbreakable()). Utilise notamment par le systeme de reparation de
     * l'Enclume Emeraude pour rejeter les items renforces (deja incassables,
     * donc rien a reparer) sans dependre du registre CustomItem.
     */
    public static boolean isUnbreakable(ItemStack item) {
        return getBoolean(item, "Unbreakable");
    }

    /** Ecrit l'identifiant interne du custom item dans le NBT (invisible au joueur). */
    public static ItemStack setCustomId(ItemStack item, String id) {
        return setString(item, CUSTOM_ID_KEY, id);
    }

    /** @return l'identifiant interne du custom item, ou null si l'item n'en est pas un. */
    public static String getCustomId(ItemStack item) {
        return getString(item, CUSTOM_ID_KEY);
    }
}
