# Bela-Customs

Plugin Spigot 1.8.x unique regroupant l'integralite des anciens plugins
**Belarion-Enchants** et **CustomItems**. Ce depot remplace definitivement
les deux anciens plugins : ils ne doivent plus etre maintenus separement.

## Fonctionnalites

**Custom Enchants** (12) : Speed, Strength, Fire Resistance, No Fall, Anti
Rod, Anti Debuff, Haste II, Aimantation, AutoSmelt, Vein Miner, Gem Hunter,
Soul Collector, Eternal. Livres d'enchantement, Table d'Enchantement
Emeraude, Enclume Emeraude, Bibliotheque d'Enchants paginee.

**Custom Items** (21) : Hammer / Pioche / Pelle / Hache / Houe / Epee en
Emeraude et en Emeraude Renforce, armure complete (casque/plastron/
jambieres/bottes) en Emeraude et Emeraude Renforce, Emeraude Renforcee
(monnaie haut de gamme du serveur, sans capacite speciale).

**Blocs speciaux** : Table d'Enchantement Emeraude (clic-droit sur
Prismarine:2 / Dark Prismarine), Enclume Emeraude (clic-droit sur Sea
Lantern), tous deux proteges contre explosions/pistons et cassables
uniquement a la pioche. Coffre en Emeraude : apparence de coffre simple
mais capacite de double coffre (54 slots), avec protection dediee contre
la fusion visuelle vanilla en double coffre lorsque plusieurs sont poses
cote a cote (voir emeraldchest.ChestMergeGuard).

## Architecture

```
fr.belarion.belacustoms
├── BelaCustoms.java            point d'entree, cablage de tout le plugin
├── api/                        contrats partages des custom items (marqueurs)
├── commands/                   /belacustoms, /enchanttable, /enchantanvil, /enchants, /citem
├── compatibility/               verification tier/cible des enchants + hook de protection externe
├── customenchants/              CustomEnchant, stockage multi-enchant, effets passifs
├── customitems/                 config/manager/items.* (tools, armor, weapons, misc)
├── emeraldanvil/                logique de l'Enclume Emeraude
├── emeraldchest/                logique du Coffre en Emeraude + protection anti-fusion
├── emeraldenchanttable/         logique de la Table d'Enchantement Emeraude
├── gui/                         rendu de toutes les interfaces (table, enclume, bibliotheque)
├── listeners/                   listeners par domaine (enchant/, item/) + protection des blocs
├── managers/                    ConfigManager (general), MessagesManager (tous les messages)
├── recipes/                     scaffold pret pour de futures recettes custom
├── registry/                    registre central des custom items
└── utils/                       NBT, tags caches, tiers, couleurs, calcul de zones
```

## Configuration

- `config.yml` : reglages generaux (protection, debug)
- `custom-enchants.yml` : couts et limites des Custom Enchants
- `custom-items.yml` : statistiques + mapping de textures des Custom Items
- `gui.yml` : reglages d'affichage des interfaces
- `recipes.yml` : recettes de craft custom (aucune pour le moment)
- `messages.yml` : tous les messages du plugin (`enchants.*`, `items.*`)

## Compilation

Requiert spigot-api 1.8.8-R0.1-SNAPSHOT installe en local (via BuildTools.jar,
revision 1.8.8) :

```
mvn clean package
```

Le jar compile se trouve dans `target/Bela-Customs.jar`.

## Notes de migration

- Toutes les permissions sont desormais prefixees `belacustoms.*` (`belacustoms.admin`, `belacustoms.enchants`, `belacustoms.items.admin`,
`belacustoms.items.use`). Mettez a jour vos groupes de permissions.
- La commande admin principale est renommee `/belacustoms` (etait
`/belarionenchants`).
- `/citem` est conservee telle quelle.
