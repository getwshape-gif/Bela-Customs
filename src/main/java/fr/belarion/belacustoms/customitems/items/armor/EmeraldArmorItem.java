package fr.belarion.belacustoms.customitems.items.armor;

import fr.belarion.belacustoms.api.ArmorBonus;
import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.api.ExtraDurability;
import fr.belarion.belacustoms.api.ItemBuilder;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTexture;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.utils.ItemTier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Classe générique pour les 4 pièces d'armure émeraude (et leurs variantes
 * renforcées). Le matériau de base reste DIAMOND_* : cela garantit des
 * points d'armure vanilla déjà supérieurs à toute autre armure, la
 * progression "supérieure au diamant" étant ensuite assurée par le bonus
 * de réduction de dégâts supplémentaire (ArmorBonus, appliqué par
 * ItemCombatListener) - l'API Bukkit 1.8 ne proposant pas d'attribute
 * modifiers natifs (ajoutés en 1.13) pour créer un matériau d'armure
 * totalement inédit.
 */
public class EmeraldArmorItem implements CustomItem, ArmorBonus, ExtraDurability {

    public enum Slot {
        HELMET("Casque", Material.DIAMOND_HELMET),
        CHESTPLATE("Plastron", Material.DIAMOND_CHESTPLATE),
        LEGGINGS("Jambières", Material.DIAMOND_LEGGINGS),
        BOOTS("Bottes", Material.DIAMOND_BOOTS);

        final String displayName;
        final Material material;

        Slot(String displayName, Material material) {
            this.displayName = displayName;
            this.material = material;
        }
    }

    private final Slot slot;
    private final boolean reinforced;
    private final double reductionPercent;
    private final String id;
    private final ItemTextureRegistry textures;

    public EmeraldArmorItem(Slot slot, boolean reinforced, ItemStatsConfig stats, ItemTextureRegistry textures) {
        this.slot = slot;
        this.reinforced = reinforced;
        this.id = (reinforced ? "REINFORCED_EMERALD_" : "EMERALD_") + slot.name();
        this.reductionPercent = reinforced
                ? stats.getArmorReinforcedReductionPerPiece()
                : stats.getArmorEmeraldReductionPerPiece();
        this.textures = textures;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isUnbreakable() {
        return reinforced;
    }

    @Override
    public double getDamageReductionPercent() {
        return reductionPercent;
    }

    @Override
    public double getDurabilityMultiplier() {
        return 1.5;
    }

    @Override
    public ItemStack build() {
        ItemTexture tex = textures.get(id).orElse(new ItemTexture(slot.material, (short) 0));
        ItemBuilder builder = new ItemBuilder(tex.getMaterial())
                .durability(tex.getDurability())
                .emeraldName(slot.displayName, reinforced)
                .progression(reinforced)
                .loreLine(reinforced ? "&7Protection supérieure." : "&7Protection améliorée.");
        if (reinforced) {
            builder.unbreakableTag().unbreakable(true);
        }
        builder.tier(reinforced ? ItemTier.EMERALD_RENFORCE : ItemTier.EMERALD);
        builder.customId(id);
        return builder.build();
    }
}
