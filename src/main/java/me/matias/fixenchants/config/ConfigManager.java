package me.matias.fixenchants.config;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class ConfigManager {

    private final JavaPlugin plugin;

    private String language;
    private boolean notifyPlayers;
    private boolean fixOnJoin;
    private boolean fixOnInventoryMove;
    private boolean fixOnPickup;
    private boolean removeInvalidEnchants;
    private boolean debugMode;
    private boolean enableFileLogging;
    private boolean fixShulkerContents;
    private boolean fixContainers;

    private List<String> enchantmentPriority = Collections.emptyList();
    private final Map<Enchantment, Integer> vanillaLimits = new HashMap<>();
    private final Map<Enchantment, List<Enchantment>> incompatibles = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        language = config.getString("language", "es");

        notifyPlayers = config.getBoolean("settings.notify-players", true);
        fixOnJoin = config.getBoolean("settings.fix-on-join", true);
        fixOnInventoryMove = config.getBoolean("settings.fix-on-inventory-move", true);
        fixOnPickup = config.getBoolean("settings.fix-on-pickup", true);
        removeInvalidEnchants = config.getBoolean("settings.remove-invalid-enchants", true);
        debugMode = config.getBoolean("settings.debug-mode", false);
        enableFileLogging = config.getBoolean("settings.enable-file-logging", true);
        fixShulkerContents = config.getBoolean("settings.fix-shulker-contents", true);
        fixContainers = config.getBoolean("settings.fix-containers", true);

        enchantmentPriority = config.getStringList("settings.enchantment-priority");

        loadLimits(config);
        loadIncompatibles(config);
    }

    private void loadLimits(FileConfiguration config) {
        vanillaLimits.clear();

        if (!config.isConfigurationSection("limits")) {
            plugin.getLogger().warning("No 'limits' section found in config.yml");
            return;
        }

        for (String key : config.getConfigurationSection("limits").getKeys(false)) {
            try {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                if (enchant != null) {
                    vanillaLimits.put(enchant, config.getInt("limits." + key));
                } else {
                    plugin.getLogger().warning("Unknown enchantment: " + key);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading enchantment limit for " + key, e);
            }
        }
    }

    private void loadIncompatibles(FileConfiguration config) {
        incompatibles.clear();

        if (!config.isConfigurationSection("incompatible")) {
            plugin.getLogger().warning("No 'incompatible' section found in config.yml");
            return;
        }

        for (String key : config.getConfigurationSection("incompatible").getKeys(false)) {
            try {
                Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase()));
                if (enchant == null) {
                    plugin.getLogger().warning("Unknown enchantment: " + key);
                    continue;
                }

                List<Enchantment> list = new ArrayList<>();
                for (String value : config.getStringList("incompatible." + key)) {
                    Enchantment other = Enchantment.getByKey(NamespacedKey.minecraft(value.toLowerCase()));
                    if (other != null) {
                        list.add(other);
                    } else {
                        plugin.getLogger().warning("Unknown incompatible enchantment: " + value);
                    }
                }
                incompatibles.put(enchant, list);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading incompatibilities for " + key, e);
            }
        }
    }

    public String getLanguage() {
        return language;
    }

    public boolean isNotifyPlayers() {
        return notifyPlayers;
    }

    public boolean isFixOnJoin() {
        return fixOnJoin;
    }

    public boolean isFixOnInventoryMove() {
        return fixOnInventoryMove;
    }

    public boolean isFixOnPickup() {
        return fixOnPickup;
    }

    public boolean isRemoveInvalidEnchants() {
        return removeInvalidEnchants;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isFileLoggingEnabled() {
        return enableFileLogging;
    }

    public void setFileLoggingEnabled(boolean value) {
        this.enableFileLogging = value;
    }

    public boolean isFixShulkerContents() {
        return fixShulkerContents;
    }

    public boolean isFixContainers() {
        return fixContainers;
    }

    public List<String> getEnchantmentPriority() {
        return enchantmentPriority;
    }

    public Map<Enchantment, Integer> getVanillaLimits() {
        return vanillaLimits;
    }

    public Map<Enchantment, List<Enchantment>> getIncompatibles() {
        return incompatibles;
    }
}
