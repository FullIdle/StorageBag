package me.gsqfi.storagebag.storagebag;

import lombok.Getter;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.StringReader;
import java.util.List;

@Getter
public class BagGui extends ListenerInvHolder {
    private final Inventory inventory;
    private final YamlConfiguration yaml;
    private final String uuid;
    private final int bagSlot;
    private final Player player;

    public BagGui(Player player, ItemStack bag, int bagSlot) {
        this.player = player;
        this.yaml = isBag(bag);
        if (bag.getAmount() > 1) {
            throw new IllegalArgumentException("叠加的物品无法识别");
        }
        if (this.yaml == null) {
            throw new IllegalArgumentException("物品参数不是背包类型物品");
        }

        this.uuid = this.yaml.getString("uuid");
        this.inventory = Bukkit.createInventory(this,
                this.yaml.getInt("gui_type") * 9,
                this.yaml.getString("gui_title"));
        this.bagSlot = bagSlot + 27 + this.getInventory().getSize();
        for (int i = 0; i < this.inventory.getSize(); i++) {
            ItemStack itemStack = this.yaml.getItemStack("gui_items." + i);
            if (itemStack != null)
                this.inventory.setItem(i, itemStack);
        }


        this.onClick(e -> {
            if (e.getCurrentItem() == null) return;
            if (e.getHotbarButton() == this.bagSlot || e.getRawSlot() == this.bagSlot) {
                e.setCancelled(true);
            }
        });

        this.onClose(e -> {
            //save
            for (int i = 0; i < e.getInventory().getSize(); i++) {
                this.yaml.set("gui_items." + i, e.getInventory().getItem(i));
            }
            PlayerInventory playerInventory = this.player.getInventory();
            ItemStack item = playerInventory.getItem(bagSlot);
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = itemMeta.getLore();
            lore.remove(lore.size() - 1);
            lore.add(this.yaml.saveToString());
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            playerInventory.setItem(bagSlot, item);
        });
    }

    public static YamlConfiguration isBag(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        Material type = Material.getMaterial(Main.plugin.getConfig().getString("type"));
        if (!itemStack.getType().equals(type)) return null;
        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.isEmpty()) return null;
        if (!lore.get(0).equalsIgnoreCase("yaml")) return null;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new StringReader(lore.get(lore.size() - 1)));
        if (yaml.contains("gui_type")) {
            return yaml;
        }
        return null;
    }
}
