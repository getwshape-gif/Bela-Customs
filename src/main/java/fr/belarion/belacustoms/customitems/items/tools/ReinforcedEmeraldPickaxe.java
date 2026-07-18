package fr.belarion.belacustoms.customitems.items.tools;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Pioche emeraude renforcee : Efficacite II offerte de base (superieure a la
 * pioche emeraude simple), Unbreakable.
 *
 * Note sur les paliers annonces dans le cahier des charges (Obsidienne
 * ~2s avec Efficacite V, Packed Ice / Mossy Cobblestone one-shot avec
 * Efficacite V + Haste II) : ce sont les temps de minage vanilla reels
 * obtenus des lors que le joueur combine cette pioche (compatible avec les
 * enchantements vanilla jusqu'a Efficacite V) avec l'effet de potion Haste II.
 * Aucune surcouche n'est necessaire : la pioche etant compatible avec tous
 * les enchantements vanilla et deja plus rapide qu'une pioche diamant de
 * base, ces paliers sont atteints naturellement par le moteur de Minecraft.
 */
public class ReinforcedEmeraldPickaxe implements CustomItem {

    public static final String ID = "REINFORCED_EMERALD_PICKAXE";
    private final ItemTextureRegistry textures;

    public ReinforcedEmeraldPickaxe(ItemTextureRegistry textures) {
        this.textures = textures;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public boolean isUnbreakable() {
        return true;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(ID).orElse(new ItemTexture(Material.DIAMOND_PICKAXE, (short) 0));
        return new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName("Pioche", true)
                .progression(true)
                .loreLines("&7Minage ultra rapide.", "&7Maîtrise les blocs résistants.")
                .unbreakableTag()
                .enchant(Enchantment.DIG_SPEED, 2)
                .unbreakable(true)
                .tier(ItemTier.EMERALD_RENFORCE)
                .customId(ID)
                .build();
    }
}
