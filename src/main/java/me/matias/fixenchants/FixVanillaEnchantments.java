package me.matias.fixenchants;

import me.matias.fixenchants.command.FixMyItemsCommand;
import me.matias.fixenchants.command.ReloadCommand;
import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import me.matias.fixenchants.enchant.EnchantmentFixer;
import me.matias.fixenchants.listener.InventoryListener;
import me.matias.fixenchants.listener.PlayerListener;
import me.matias.fixenchants.logging.AsyncFileLogger;
import me.matias.fixenchants.util.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public class FixVanillaEnchantments extends JavaPlugin {

    private ConfigManager configManager;
    private LanguageManager languageManager;
    private AsyncFileLogger fileLogger;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        languageManager = new LanguageManager(this);
        languageManager.load(configManager.getLanguage());

        Debugger debugger = new Debugger(this, configManager, languageManager);

        fileLogger = new AsyncFileLogger(this, configManager, languageManager);
        fileLogger.start();

        EnchantmentFixer fixer = new EnchantmentFixer(configManager, languageManager, fileLogger, debugger);

        Bukkit.getPluginManager().registerEvents(
                new PlayerListener(this, configManager, languageManager, fixer, debugger), this);
        Bukkit.getPluginManager().registerEvents(
                new InventoryListener(this, configManager, languageManager, fixer, debugger), this);

        getCommand("fixmyitems").setExecutor(new FixMyItemsCommand(languageManager, fixer));
        getCommand("fixenchants-reload").setExecutor(
                new ReloadCommand(configManager, languageManager, fileLogger));

        getLogger().info("==========================================");
        getLogger().info("FixVanillaEnchantments v" + getDescription().getVersion());
        getLogger().info("Idioma activo: " + configManager.getLanguage());
        getLogger().info("Encantamientos con límite propio: " + configManager.getVanillaLimits().size());
        getLogger().info("Reglas de incompatibilidad: " + configManager.getIncompatibles().size());
        if (configManager.isFileLoggingEnabled()) {
            getLogger().info("Log de correcciones (asíncrono): plugins/" + getName() + "/logs/");
        }
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (fileLogger != null) {
            fileLogger.stop();
        }
        getLogger().info("FixVanillaEnchantments disabled successfully.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
}
