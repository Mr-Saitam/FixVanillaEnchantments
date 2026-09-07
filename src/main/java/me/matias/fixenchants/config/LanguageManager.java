package me.matias.fixenchants.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;


public class LanguageManager {

    private final JavaPlugin plugin;

    private FileConfiguration active;
    private FileConfiguration fallback;
    private String prefix;

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(String languageCode) {
        String code = normalize(languageCode);

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, code + ".yml");

        if (!langFile.exists()) {
            try {
                plugin.saveResource("lang/" + code + ".yml", false);
            } catch (IllegalArgumentException notBundled) {
                plugin.getLogger().warning("No hay un archivo de idioma embebido para '" + code
                        + "'. Usando español por defecto. Podés crear "
                        + "plugins/FixVanillaEnchantments/lang/" + code + ".yml manualmente "
                        + "con tu propia traducción.");
                code = "es";
                langFile = new File(langFolder, code + ".yml");
                if (!langFile.exists()) {
                    plugin.saveResource("lang/es.yml", false);
                }
            }
        }

        active = YamlConfiguration.loadConfiguration(langFile);
        fallback = loadEmbedded("lang/en.yml");

        prefix = colorize(getRaw("prefix", "&6[Encantamientos]"));
    }

    private FileConfiguration loadEmbedded(String resourcePath) {
        try (InputStream is = plugin.getResource(resourcePath)) {
            if (is == null) return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load embedded fallback language file", e);
            return null;
        }
    }

    private String normalize(String languageCode) {
        return (languageCode == null || languageCode.trim().isEmpty())
                ? "es"
                : languageCode.trim().toLowerCase();
    }

    private String getRaw(String path, String def) {
        if (active != null && active.isSet(path)) return active.getString(path);
        if (fallback != null && fallback.isSet(path)) return fallback.getString(path);
        return def;
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String get(String path) {
        return colorize(getRaw(path, path));
    }


    public String get(String path, Object... replacements) {
        String msg = getRaw(path, path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace("%" + replacements[i] + "%", String.valueOf(replacements[i + 1]));
        }
        return colorize(msg);
    }

    public String getPrefixed(String path, Object... replacements) {
        return prefix + " " + get(path, replacements);
    }

    public String getPrefix() {
        return prefix;
    }

    public String event(String key, Object... replacements) {
        return get("event-types." + key, replacements);
    }
}
