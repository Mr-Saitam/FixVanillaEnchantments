package me.matias.fixenchants.enchant;

import me.matias.fixenchants.config.ConfigManager;
import me.matias.fixenchants.config.LanguageManager;
import me.matias.fixenchants.logging.AsyncFileLogger;
import me.matias.fixenchants.util.Debugger;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class EnchantmentFixer {

    private final ConfigManager config;
    private final LanguageManager lang;
    private final AsyncFileLogger fileLogger;
    private final Debugger debugger;

    public EnchantmentFixer(ConfigManager config, LanguageManager lang,
                             AsyncFileLogger fileLogger, Debugger debugger) {
        this.config = config;
        this.lang = lang;
        this.fileLogger = fileLogger;
        this.debugger = debugger;
    }


    public boolean fix(ItemStack item, Player player, String eventDescription) {
        if (item == null || !item.hasItemMeta()) return false;

        boolean changed = fixItem(item, player, eventDescription);

        if (config.isFixShulkerContents()) {
            changed |= fixShulkerBoxContents(item, player, eventDescription, 0);
        }

        return changed;
    }

    public int fixPlayerInventory(Player player, String eventDescription) {
        int fixedCount = 0;
        List<ItemStack> items = new ArrayList<>();

        Collections.addAll(items, player.getInventory().getContents());
        Collections.addAll(items, player.getInventory().getArmorContents());
        items.add(player.getInventory().getItemInOffHand());
        items.add(player.getItemOnCursor());

        for (ItemStack item : items) {
            if (item == null) continue;
            if (fix(item, player, eventDescription)) {
                fixedCount++;
            }
        }

        return fixedCount;
    }

    private boolean fixItem(ItemStack item, Player player, String eventDescription) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        Map<Enchantment, Integer> enchants = new HashMap<>(meta.getEnchants());
        if (enchants.isEmpty()) return false;

        boolean changed = false;
        List<String> actions = new ArrayList<>();

        if (config.isRemoveInvalidEnchants()) {
            Set<Enchantment> invalid = new HashSet<>();
            for (Enchantment enchant : enchants.keySet()) {
                if (!enchant.canEnchantItem(item)) {
                    invalid.add(enchant);
                    String action = lang.get("log.action-invalid",
                            "enchant", enchant.getKey().getKey(),
                            "level", enchants.get(enchant),
                            "item_type", item.getType().name());
                    actions.add(action);
                    debugger.raw(action);
                }
            }
            for (Enchantment enchant : invalid) {
                meta.removeEnchant(enchant);
                changed = true;
            }
        }

        enchants = new HashMap<>(meta.getEnchants());
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            int maxLevel = config.getVanillaLimits().getOrDefault(enchant, enchant.getMaxLevel());

            if (level > maxLevel) {
                meta.removeEnchant(enchant);
                meta.addEnchant(enchant, maxLevel, true);
                changed = true;
                String action = lang.get("log.action-level",
                        "enchant", enchant.getKey().getKey(),
                        "level", level,
                        "max", maxLevel);
                actions.add(action);
                debugger.raw(action);
            }
        }

        enchants = new HashMap<>(meta.getEnchants());
        Set<Enchantment> toRemove = new HashSet<>();

        for (Enchantment e1 : enchants.keySet()) {
            List<Enchantment> incompatibleList = config.getIncompatibles().get(e1);
            if (incompatibleList == null) continue;

            for (Enchantment e2 : incompatibleList) {
                if (enchants.containsKey(e2) && !toRemove.contains(e1) && !toRemove.contains(e2)) {
                    Enchantment toRemoveEnch = determineEnchantmentToRemove(e1, e2, enchants);
                    toRemove.add(toRemoveEnch);
                    Enchantment kept = toRemoveEnch.equals(e1) ? e2 : e1;

                    String action = lang.get("log.action-incompatible",
                            "enchant", toRemoveEnch.getKey().getKey(),
                            "level", enchants.get(toRemoveEnch),
                            "other", kept.getKey().getKey(),
                            "other_level", enchants.get(kept));
                    actions.add(action);
                    debugger.raw(action);
                }
            }
        }

        for (Enchantment e : toRemove) {
            meta.removeEnchant(e);
            changed = true;
        }

        if (changed) {
            item.setItemMeta(meta);

            if (player != null) {
                fileLogger.logAction(player, item, eventDescription, String.join(" | ", actions));
            }
        }

        return changed;
    }


    private boolean fixShulkerBoxContents(ItemStack item, Player player, String eventDescription, int depth) {
        if (depth > 1) return false;

        if (!item.getType().name().endsWith("_SHULKER_BOX")) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta rawMeta = item.getItemMeta();
        if (!(rawMeta instanceof BlockStateMeta)) return false;

        BlockStateMeta meta = (BlockStateMeta) rawMeta;
        if (!(meta.getBlockState() instanceof ShulkerBox)) return false;

        ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
        Inventory inv = shulker.getInventory();
        ItemStack[] contents = inv.getContents();

        boolean changed = false;
        String nestedDescription = eventDescription + lang.event("shulker-suffix");

        for (ItemStack contained : contents) {
            if (contained == null || !contained.hasItemMeta()) continue;

            if (fixItem(contained, player, nestedDescription)) {
                changed = true;
            }
            if (fixShulkerBoxContents(contained, player, eventDescription, depth + 1)) {
                changed = true;
            }
        }

        if (changed) {
            inv.setContents(contents);
            meta.setBlockState(shulker);
            item.setItemMeta(meta);
        }

        return changed;
    }

    private Enchantment determineEnchantmentToRemove(Enchantment e1, Enchantment e2,
                                                      Map<Enchantment, Integer> enchants) {
        String key1 = e1.getKey().getKey();
        String key2 = e2.getKey().getKey();

        List<String> priority = config.getEnchantmentPriority();
        int priority1 = priority.indexOf(key1);
        int priority2 = priority.indexOf(key2);

        if (priority1 != -1 && priority2 != -1) {
            return priority1 < priority2 ? e2 : e1;
        }
        if (priority1 != -1) return e2;
        if (priority2 != -1) return e1;

        int level1 = enchants.get(e1);
        int level2 = enchants.get(e2);
        if (level1 != level2) {
            return level1 >= level2 ? e2 : e1;
        }

        return key1.compareTo(key2) <= 0 ? e2 : e1;
    }
}
