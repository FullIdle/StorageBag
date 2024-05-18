package me.gsqfi.storagebag.storagebag;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.fullidle.ficore.ficore.common.api.ineventory.ListenerInvHolder;
import me.fullidle.ficore.ficore.common.bukkit.inventory.CraftItemStack;
import me.fullidle.ficore.ficore.common.bukkit.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.StringReader;
import java.util.ArrayList;

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
            System.out.println(e.getView().convertSlot(e.getRawSlot()));
            if (e.getHotbarButton() == this.bagSlot - 27 - this.inventory.getSize()||
                    e.getRawSlot() == this.bagSlot) {
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
            ArrayList<String> lore = new ArrayList<>();
            lore.add("§8§lItemStack-List:");
            int x = 0;
            for (int i = 0; i < 5; i++) {
                ItemStack itemStack = e.getInventory().getItem(i);
                if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                    lore.add("§8§l  - "+itemStack.getType().name() + "§8§l * "+itemStack.getAmount());
                    x++;
                }
            }
            if (x != 0)
                itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);

            me.fullidle.ficore.ficore.common.bukkit.inventory.ItemStack itemStack = new me.fullidle.ficore.ficore.common.bukkit.inventory.
                    ItemStack(CraftItemStack.asNMSCopy(item));
            NBTTagCompound compound = new NBTTagCompound(itemStack.getNBTTag());
            compound.setString("StorageBag_Data",this.yaml.saveToString());
            itemStack.setNBTTag(compound.getNbtTagCompound());
            playerInventory.setItem(bagSlot, CraftItemStack.asBukkitCopy(itemStack.getV1_itemStack()));
        });
    }

    public static YamlConfiguration isBag(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().equals(Material.AIR)) return null;
        Material type = Material.getMaterial(Main.plugin.getConfig().getString("type"));
        if (!itemStack.getType().equals(type)) return null;
        me.fullidle.ficore.ficore.common.bukkit.inventory.ItemStack v1_ItemStack = new me.fullidle.ficore.ficore.common.bukkit.inventory.ItemStack(CraftItemStack.asNMSCopy(itemStack));
        Object nbtTag = v1_ItemStack.getNBTTag();
        if (nbtTag == null) return null;
        NBTTagCompound compound = new NBTTagCompound(nbtTag);
        String yaml_string = compound.getString("StorageBag_Data");
        if (yaml_string == null||yaml_string.isEmpty()) return null;
        return YamlConfiguration.loadConfiguration(new StringReader(yaml_string));
    }
}
