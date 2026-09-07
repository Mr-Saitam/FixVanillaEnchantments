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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryListener implements Listener {

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final LanguageManager lang;
    private final EnchantmentFixer fixer;
    private final Debugger debugger;

    public InventoryListener(JavaPlugin plugin, ConfigManager config, LanguageManager lang,
                              EnchantmentFixer fixer, Debugger debugger) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.fixer = fixer;
        this.debugger = debugger;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!config.isFixOnInventoryMove()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean currentHasMeta = currentItem != null && currentItem.hasItemMeta();
        boolean cursorHasMeta = cursor != null && cursor.hasItemMeta();

        if (!currentHasMeta && !cursorHasMeta) return;

        Player player = (Player) event.getWhoClicked();


        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean needsUpdate = false;

            ItemStack liveCurrent = event.getCurrentItem();
            if (liveCurrent != null && liveCurrent.hasItemMeta()
                    && fixer.fix(liveCurrent, player, lang.event("inventory-click"))) {
                event.setCurrentItem(liveCurrent);
                needsUpdate = true;
            }

            ItemStack liveCursor = event.getCursor();
            if (liveCursor != null && liveCursor.hasItemMeta()
                    && fixer.fix(liveCursor, player, lang.event("cursor"))) {
                event.setCursor(liveCursor);
                needsUpdate = true;
            }

            if (needsUpdate) {
                player.updateInventory();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!config.isFixOnInventoryMove()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        boolean anyMeta = false;
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && item.hasItemMeta()) {
                anyMeta = true;
                break;
            }
        }
        if (!anyMeta) return;

        Player player = (Player) event.getWhoClicked();

        Bukkit.getScheduler().runTask(plugin, () -> {
            boolean needsUpdate = false;

            for (ItemStack item : event.getNewItems().values()) {
                if (item != null && item.hasItemMeta()
                        && fixer.fix(item, player, lang.event("drag"))) {
                    needsUpdate = true;
                }
            }

            if (needsUpdate) {
                player.updateInventory();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!config.isFixOnInventoryMove()) return;
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        Inventory topInventory = event.getInventory();

        Bukkit.getScheduler().runTask(plugin, () -> {
            int fixedCount = 0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.hasItemMeta()
                        && fixer.fix(item, player, lang.event("inventory-close"))) {
                    fixedCount++;
                }
            }


            if (config.isFixContainers() && isTrackedContainer(topInventory)) {
                ItemStack[] containerContents = topInventory.getContents();
                boolean containerChanged = false;

                String containerDesc = lang.event("container-close", "container", topInventory.getType().name());

                for (ItemStack item : containerContents) {
                    if (item != null && item.hasItemMeta()
                            && fixer.fix(item, player, containerDesc)) {
                        fixedCount++;
                        containerChanged = true;
                    }
                }

                if (containerChanged) {
                    topInventory.setContents(containerContents);
                }
            }

            if (fixedCount > 0) {
                player.updateInventory();
                debugger.key("debug.fixed-summary",
                        "count", fixedCount, "player", player.getName(), "context", lang.event("inventory-close"));
            }
        });
    }

    private boolean isTrackedContainer(Inventory inventory) {
        switch (inventory.getType()) {
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
            case ENDER_CHEST:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
                return true;
            default:
                return false;
        }
    }
}
