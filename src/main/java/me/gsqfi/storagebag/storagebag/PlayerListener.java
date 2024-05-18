package me.gsqfi.storagebag.storagebag;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler
    public void onCopyItem(InventoryCreativeEvent e){
        if (BagGui.isBag(e.getCursor()) == null) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onOpenInv(PlayerInteractEvent e){
        if (e.getAction().name().toLowerCase().contains("left")) {
            return;
        }
        Player player = e.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (BagGui.isBag(itemStack) == null) {
            return;
        }
        e.setCancelled(true);
        BagGui bagGui = new BagGui(player, itemStack, player.getInventory().getHeldItemSlot());
        Inventory inventory = bagGui.getInventory();
        player.openInventory(inventory);
    }
}
