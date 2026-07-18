package fr.belarion.belacustoms.utils;

import org.bukkit.inventory.ItemStack;

public final class ItemTierUtil {

    public static final String TAG_KEY = "beltier";

    private ItemTierUtil() {}

    public static void setTier(ItemStack item, ItemTier tier) {
        HiddenTag.write(item, TAG_KEY, tier.getId());
    }

    public static ItemTier getTier(ItemStack item) {
        String id = HiddenTag.read(item, TAG_KEY);
        if (id == null) return null;
        return ItemTier.fromId(id);
    }

    public static boolean isEmeraldTier(ItemStack item) {
        return getTier(item) != null;
    }
}
