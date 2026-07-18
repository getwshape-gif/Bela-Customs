package fr.belarion.belacustoms;

import fr.belarion.belacustoms.commands.BelaCustomsCommand;
import fr.belarion.belacustoms.commands.CustomItemCommand;
import fr.belarion.belacustoms.customenchants.AntiDebuffGuardTask;
import fr.belarion.belacustoms.customenchants.EffectManager;
import fr.belarion.belacustoms.customenchants.EnchantSettings;
import fr.belarion.belacustoms.customitems.config.ItemStatsConfig;
import fr.belarion.belacustoms.customitems.config.ItemTextureRegistry;
import fr.belarion.belacustoms.customitems.manager.CustomItemManager;
import fr.belarion.belacustoms.emeraldanvil.EmeraldAnvilListener;
import fr.belarion.belacustoms.emeraldenchanttable.EnchantTableListener;
import fr.belarion.belacustoms.gui.GuiSettings;
import fr.belarion.belacustoms.listeners.BlockProtectionListener;
import fr.belarion.belacustoms.listeners.enchant.EnchantCombatListener;
import fr.belarion.belacustoms.listeners.enchant.EnchantFallDamageListener;
import fr.belarion.belacustoms.listeners.enchant.EnchantFishingListener;
import fr.belarion.belacustoms.listeners.enchant.EnchantMiningListener;
import fr.belarion.belacustoms.listeners.enchant.EnchantPotionProtectionListener;
import fr.belarion.belacustoms.listeners.item.ItemCombatListener;
import fr.belarion.belacustoms.listeners.item.ItemCraftProtectionListener;
import fr.belarion.belacustoms.listeners.item.ItemDurabilityListener;
import fr.belarion.belacustoms.listeners.item.ItemFarmingListener;
import fr.belarion.belacustoms.listeners.item.ItemMiningListener;
import fr.belarion.belacustoms.managers.ConfigManager;
import fr.belarion.belacustoms.managers.MessagesManager;
import fr.belarion.belacustoms.recipes.RecipeManager;
import fr.belarion.belacustoms.registry.CustomItemRegistry;
import fr.belarion.belacustoms.utils.NBTEditor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Point d'entree unique du plugin Bela-Customs, fusion complete des anciens
 * plugins Belarion-Enchants et CustomItems.
 *
 * Cablage de l'ensemble du systeme au demarrage :
 *   managers (config/messages) -> customenchants/customitems (registries) ->
 *   listeners -> commandes -> taches recurrentes.
 *
 * Pour ajouter un nouveau Custom Enchant : voir customenchants.CustomEnchant.
 * Pour ajouter un nouveau Custom Item : voir registry.CustomItemRegistry.
 * Pour ajouter une nouvelle recette : voir recipes.RecipeManager.
 */
public class BelaCustoms extends JavaPlugin {

    private static BelaCustoms instance;

    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private EnchantSettings enchantSettings;
    private GuiSettings guiSettings;

    private ItemStatsConfig itemStatsConfig;
    private ItemTextureRegistry itemTextureRegistry;
    private CustomItemRegistry customItemRegistry;
    private CustomItemManager customItemManager;

    private RecipeManager recipeManager;

    private EffectManager effectManager;
    private AntiDebuffGuardTask antiDebuffGuardTask;

    public static BelaCustoms get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfigs();

        if (!NBTEditor.isAvailable()) {
            getLogger().warning("NBTEditor indisponible sur cette version de serveur : "
                    + "les fonctionnalites Unbreakable/identification NBT des custom items seront degradees.");
        }

        customItemRegistry = new CustomItemRegistry(itemStatsConfig, itemTextureRegistry);
        customItemManager = new CustomItemManager(customItemRegistry);

        recipeManager = new RecipeManager(this);
        recipeManager.registerAll();

        registerListeners();
        registerCommands();
        startRecurringTasks();

        getLogger().info("Bela-Customs v1.0.0 (Spigot 1.8.8) actif : "
                + fr.belarion.belacustoms.customenchants.CustomEnchant.values().length + " custom enchants, "
                + customItemRegistry.getIds().size() + " custom items charges.");
    }

    @Override
    public void onDisable() {
        if (effectManager != null) {
            effectManager.cancel();
        }
        if (antiDebuffGuardTask != null) {
            antiDebuffGuardTask.cancel();
        }
        getLogger().info("Bela-Customs desactive.");
    }

    private void loadConfigs() {
        configManager = new ConfigManager(this);
        configManager.load();

        messagesManager = new MessagesManager(this);
        messagesManager.load();

        enchantSettings = new EnchantSettings(this);
        enchantSettings.load();

        guiSettings = new GuiSettings(this);
        guiSettings.load();

        itemStatsConfig = new ItemStatsConfig(this);
        itemStatsConfig.load();

        itemTextureRegistry = new ItemTextureRegistry(this);
        itemTextureRegistry.load();
    }

    private void registerListeners() {
        // Table d'Enchantement Emeraude / Enclume Emeraude
        getServer().getPluginManager().registerEvents(new EnchantTableListener(), this);
        getServer().getPluginManager().registerEvents(new EmeraldAnvilListener(), this);

        // Protection des blocs custom (partagee table + enclume)
        getServer().getPluginManager().registerEvents(new BlockProtectionListener(), this);

        // Mecaniques de Custom Enchants a evenement specifique
        getServer().getPluginManager().registerEvents(new EnchantMiningListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantCombatListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantFishingListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantFallDamageListener(), this);
        getServer().getPluginManager().registerEvents(new EnchantPotionProtectionListener(), this);

        // Mecaniques de Custom Items
        getServer().getPluginManager().registerEvents(new ItemMiningListener(customItemManager), this);
        getServer().getPluginManager().registerEvents(new ItemFarmingListener(customItemManager), this);
        getServer().getPluginManager().registerEvents(new ItemCombatListener(customItemManager), this);
        getServer().getPluginManager().registerEvents(new ItemDurabilityListener(customItemManager), this);
        getServer().getPluginManager().registerEvents(new ItemCraftProtectionListener(customItemManager), this);
    }

    private void registerCommands() {
        BelaCustomsCommand belaCustomsCommand = new BelaCustomsCommand(this);
        setExecutor("belacustoms", belaCustomsCommand);
        setExecutor("enchanttable", belaCustomsCommand);
        setExecutor("enchantanvil", belaCustomsCommand);
        setExecutor("enchants", belaCustomsCommand);

        CustomItemCommand customItemCommand = new CustomItemCommand(this, customItemManager);
        PluginCommand citem = getCommand("citem");
        if (citem != null) {
            citem.setExecutor(customItemCommand);
            citem.setTabCompleter(customItemCommand);
        }
    }

    private void setExecutor(String name, BelaCustomsCommand executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
        }
    }

    private void startRecurringTasks() {
        // Effets passifs permanents (Speed, Strength, Fire Resistance, Haste) : 1 fois/seconde
        effectManager = new EffectManager();
        effectManager.runTaskTimer(this, 20L, 20L);

        // Garde Anti Debuff : tres haute frequence (0.1s) pour une reactivite quasi instantanee
        antiDebuffGuardTask = new AntiDebuffGuardTask();
        antiDebuffGuardTask.runTaskTimer(this, 1L, 2L);
    }

    /**
     * Recharge la configuration a chaud (/belacustoms reload) : reglages
     * generaux, Custom Enchants et GUI. Les statistiques de Custom Items
     * (custom-items.yml) et le registre d'items necessitent un redemarrage
     * du serveur pour etre repris en compte, comme c'etait deja le cas
     * avant la fusion (le plugin CustomItems ne proposait aucune commande
     * de reload).
     */
    public void reloadAll() {
        configManager.load();
        messagesManager.load();
        enchantSettings.load();
        guiSettings.load();
    }

    public ConfigManager getConfigManager() { return configManager; }
    public MessagesManager getMessagesManager() { return messagesManager; }
    public EnchantSettings getEnchantSettings() { return enchantSettings; }
    public GuiSettings getGuiSettings() { return guiSettings; }
    public ItemStatsConfig getItemStatsConfig() { return itemStatsConfig; }
    public ItemTextureRegistry getItemTextureRegistry() { return itemTextureRegistry; }
    public CustomItemRegistry getCustomItemRegistry() { return customItemRegistry; }
    public CustomItemManager getCustomItemManager() { return customItemManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}
