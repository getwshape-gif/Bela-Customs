package fr.belarion.belacustoms.registry;

import fr.belarion.belacustoms.api.CustomItem;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.customitems.items.armor.EmeraldArmorItem;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldAnvilItem;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldChest;
import fr.belarion.belacustoms.customitems.items.misc.EmeraldEnchantTableItem;
import fr.belarion.belacustoms.customitems.items.misc.ReinforcedEmeraldBlock;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldAxe;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldHammer;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldHoe;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldPickaxe;
import fr.belarion.belacustoms.customitems.items.tools.EmeraldShovel;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldAxe;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldHammer;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldHoe;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldPickaxe;
import fr.belarion.belacustoms.customitems.items.tools.ReinforcedEmeraldShovel;
import fr.belarion.belacustoms.customitems.items.weapons.EmeraldSword;
import fr.belarion.belacustoms.customitems.items.weapons.ReinforcedEmeraldSword;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
* Registre central de tous les custom items du plugin.
    *
    * Pour ajouter un nouvel item a l'avenir :
* 1. Creer une classe implementant CustomItem (+ les marqueurs pertinents :
* MiningTool, ReplantingHoe, TreeFeller, WeaponBonus, ArmorBonus, ExtraDurability)
    * dans le sous-package customitems.items.* approprie.
    * 2. Ajouter une ligne register(new MonNouvelItem(...)); ci-dessous.
    * C'est la SEULE modification necessaire : les listeners lisent les
    * marqueurs de facon generique et n'ont pas besoin d'etre modifies.
    */
public class CustomItemRegistry {

private final Map<String, CustomItem> items = new LinkedHashMap<>();

public CustomItemRegistry(ItemStatsConfig stats, ItemTextureRegistry textures) {
    // Outils
    register(new EmeraldHammer(stats, textures));
    register(new ReinforcedEmeraldHammer(stats, textures));
    register(new EmeraldHoe(stats, textures));
    register(new ReinforcedEmeraldHoe(stats, textures));
    register(new EmeraldPickaxe(textures));
    register(new ReinforcedEmeraldPickaxe(textures));
    register(new EmeraldShovel(stats, textures));
    register(new ReinforcedEmeraldShovel(stats, textures));
    register(new EmeraldAxe(stats, textures));
    register(new ReinforcedEmeraldAxe(textures));

    // Armures (4 emplacements x 2 tiers)
    for (EmeraldArmorItem.Slot slot : EmeraldArmorItem.Slot.values()) {
        register(new EmeraldArmorItem(slot, false, stats, textures));
        register(new EmeraldArmorItem(slot, true, stats, textures));
    }

    // Armes
    register(new EmeraldSword(stats, textures));
    register(new ReinforcedEmeraldSword(stats, textures));

    // Commerce
    register(new ReinforcedEmeraldBlock());

    // Blocs premium
    register(new EmeraldChest());

    // Blocs declencheurs (Table d'Enchantement Emeraude / Enclume Emeraude) :
    // obtention via /citem, NBT CustomItemId et texture geres ici comme
    // n'importe quel autre custom item. Le clic-droit d'ouverture et le
    // GUI restent geres tels quels par emeraldenchanttable.EnchantTableListener
    // / emeraldanvil.EmeraldAnvilListener (inchanges) : Bukkit 1.8 ne
    // conserve de toute facon aucun NBT sur un bloc plein une fois pose
    // (voir ReinforcedEmeraldBlock pour la meme remarque), donc le
    // declenchement par Material reste strictement identique.
    register(new EmeraldEnchantTableItem(textures));
    register(new EmeraldAnvilItem(textures));
}

private void register(CustomItem item) {
    items.put(item.getId().toUpperCase(Locale.ROOT), item);
}

public Optional<CustomItem> get(String id) {
    if (id == null) return Optional.empty();
    return Optional.ofNullable(items.get(id.toUpperCase(Locale.ROOT)));
}

public Collection<CustomItem> getAll() {
    return items.values();
}

public Set<String> getIds() {
    return items.keySet();
}
}
