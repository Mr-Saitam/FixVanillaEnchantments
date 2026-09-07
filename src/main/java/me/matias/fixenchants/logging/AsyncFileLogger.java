package me.matias.fixenchants.logging;

import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


public class AsyncFileLogger {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LanguageManager lang;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyy-MM-dd");

    private BufferedWriter writer;
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private int flushTaskId = -1;

    public AsyncFileLogger(JavaPlugin plugin, ConfigManager config, LanguageManager lang) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
    }

    public void start() {
        if (!config.isFileLoggingEnabled()) return;

        try {
            File logsFolder = new File(plugin.getDataFolder(), "logs");
            if (!logsFolder.exists()) {
                logsFolder.mkdirs();
            }

            File logFile = new File(logsFolder, "fixes-" + fileNameFormat.format(new Date()) + ".log");
            boolean isNewFile = !logFile.exists();

            writer = new BufferedWriter(new FileWriter(logFile, true));

            if (isNewFile) {
                enqueue(lang.get("log.session-started"));
                enqueue(lang.get("log.server-started-at", "date", dateFormat.format(new Date())));
                enqueue(lang.get("log.plugin-version", "version", plugin.getDescription().getVersion()));
            }

            flushTaskId = Bukkit.getScheduler()
                    .runTaskTimerAsynchronously(plugin, this::flush, 100L, 100L)
                    .getTaskId();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create log file!", e);
            config.setFileLoggingEnabled(false);
        }
    }

    public void stop() {
        flush();
        if (flushTaskId != -1) {
            Bukkit.getScheduler().cancelTask(flushTaskId);
            flushTaskId = -1;
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not close log file cleanly", e);
            } finally {
                writer = null;
            }
        }
    }

    private void enqueue(String line) {
        if (!config.isFileLoggingEnabled()) return;
        queue.add(line);
    }

    private void flush() {
        if (writer == null) return;

        String line;
        boolean wroteAny = false;
        try {
            while ((line = queue.poll()) != null) {
                writer.write(line);
                writer.newLine();
                wroteAny = true;
            }
            if (wroteAny) {
                writer.flush();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not write to log file", e);
        }
    }

    public void logAction(Player player, ItemStack item, String eventDescription, String details) {
        if (!config.isFileLoggingEnabled()) return;

        String playerName = player != null ? player.getName() : "Unknown";
        String itemName = displayName(item);
        String itemType = item != null ? item.getType().name() : "UNKNOWN";

        String line = lang.get("log.entry-format",
                "timestamp", dateFormat.format(new Date()),
                "player", playerName,
                "item", itemName,
                "item_type", itemType,
                "action", eventDescription,
                "details", details);

        enqueue(line);
    }

    private String displayName(ItemStack item) {
        if (item == null) return "Unknown Item";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name();
    }
}
