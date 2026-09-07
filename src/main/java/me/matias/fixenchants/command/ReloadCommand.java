package me.matias.fixenchants.command;

import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import me.matias.fixenchants.logging.AsyncFileLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final ConfigManager config;
    private final LanguageManager lang;
    private final AsyncFileLogger fileLogger;

    public ReloadCommand(ConfigManager config, LanguageManager lang, AsyncFileLogger fileLogger) {
        this.config = config;
        this.lang = lang;
        this.fileLogger = fileLogger;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fixvanillaenchants.reload")) {
            sender.sendMessage(lang.getPrefixed("messages.no-permission"));
            return true;
        }

        boolean loggingWasEnabled = config.isFileLoggingEnabled();


        config.load();
        lang.load(config.getLanguage());

        if (config.isFileLoggingEnabled() && !loggingWasEnabled) {
            fileLogger.start();
        } else if (!config.isFileLoggingEnabled() && loggingWasEnabled) {
            fileLogger.stop();
        }

        sender.sendMessage(lang.getPrefixed("messages.reload-success"));
        return true;
    }
}
