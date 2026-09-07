package me.matias.fixenchants.listener;

import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import me.matias.fixenchants.enchant.EnchantmentFixer;
import me.matias.fixenchants.util.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LanguageManager lang;
    private final EnchantmentFixer fixer;
    private final Debugger debugger;

    public PlayerListener(JavaPlugin plugin, ConfigManager config, LanguageManager lang,
                           EnchantmentFixer fixer, Debugger debugger) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.fixer = fixer;
        this.debugger = debugger;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        if (!config.isFixOnJoin()) return;

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int fixedCount = fixer.fixPlayerInventory(player, lang.event("join"));
            if (fixedCount > 0) {
                if (config.isNotifyPlayers()) {
                    player.sendMessage(lang.getPrefixed("messages.items-fixed"));
                }
                player.updateInventory();
                debugger.key("debug.fixed-summary",
                        "count", fixedCount, "player", player.getName(), "context", lang.event("join"));
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!config.isFixOnPickup()) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        if (item != null && item.hasItemMeta() && fixer.fix(item, player, lang.event("pickup"))) {
            event.getItem().setItemStack(item);
            debugger.key("debug.fixed-summary",
                    "count", 1, "player", player.getName(), "context", lang.event("pickup"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (!config.isFixOnInventoryMove()) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item != null && item.hasItemMeta() && fixer.fix(item, player, lang.event("hotbar"))) {
            player.updateInventory();
            debugger.key("debug.fixed-summary",
                    "count", 1, "player", player.getName(), "context", lang.event("hotbar"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (!config.isFixOnInventoryMove()) return;

        Player player = event.getPlayer();
        boolean needsUpdate = false;

        ItemStack mainHand = event.getMainHandItem();
        if (mainHand != null && mainHand.hasItemMeta()
                && fixer.fix(mainHand, player, lang.event("swap-main"))) {
            event.setMainHandItem(mainHand);
            needsUpdate = true;
        }

        ItemStack offHand = event.getOffHandItem();
        if (offHand != null && offHand.hasItemMeta()
                && fixer.fix(offHand, player, lang.event("swap-off"))) {
            event.setOffHandItem(offHand);
            needsUpdate = true;
        }

        if (needsUpdate) {
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }
}
