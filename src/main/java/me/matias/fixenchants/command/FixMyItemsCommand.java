package me.matias.fixenchants.command;

import me.matias.fixenchants.config.LanguageManager;
import me.matias.fixenchants.enchant.EnchantmentFixer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FixMyItemsCommand implements CommandExecutor {

    private final LanguageManager lang;
    private final EnchantmentFixer fixer;

    public FixMyItemsCommand(LanguageManager lang, EnchantmentFixer fixer) {
        this.lang = lang;
        this.fixer = fixer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(lang.get("messages.in-game-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("fixvanillaenchants.use")) {
            player.sendMessage(lang.getPrefixed("messages.no-permission"));
            return true;
        }

        int fixedCount = fixer.fixPlayerInventory(player, lang.event("manual"));
        if (fixedCount > 0) {
            player.updateInventory();
            player.sendMessage(lang.getPrefixed("messages.fixed-manual"));
        } else {
            player.sendMessage(lang.getPrefixed("messages.no-items-to-fix"));
        }
        return true;
    }
}
