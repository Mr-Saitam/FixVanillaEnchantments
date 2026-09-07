package me.matias.fixenchants.util;

import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Debugger {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LanguageManager lang;

    public Debugger(JavaPlugin plugin, ConfigManager config, LanguageManager lang) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
    }

    public void raw(String message) {
        if (config.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public void key(String path, Object... replacements) {
        if (config.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] " + lang.get(path, replacements));
        }
    }
}
