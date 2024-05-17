package me.gsqfi.storagebag.storagebag;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Cmd implements TabExecutor {
    List<String> sub = Lists.newArrayList(
            "help","reload","settitle","settype","give"
    );
    public static String[] help;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        int length = args.length;
        if (length > 0) {
            String lowerCase = args[0].toLowerCase();
            if (sub.contains(lowerCase)) {
                Player player = null;
                if (commandSender instanceof Player) {
                    player = (Player) commandSender;
                }
                switch (lowerCase){
                    case "help":{
                        commandSender.sendMessage(help);
                        break;
                    }
                    case "reload":{
                        Main.plugin.reloadConfig();
                        commandSender.sendMessage("§aReloaded!");
                        break;
                    }
                    case "settype":
                    case "settitle":{
                        if (player == null) {
                            commandSender.sendMessage("§cYou are not a player");
                            return false;
                        }
                        if (args.length < 2){
                            commandSender.sendMessage(help);
                            return false;
                        }
                        PlayerInventory playerInventory = player.getInventory();
                        ItemStack itemStack = playerInventory.getItemInMainHand();
                        YamlConfiguration bag = BagGui.isBag(itemStack);
                        if (bag == null) {
                            commandSender.sendMessage("§c你手上不是背包!");
                            return false;
                        }
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (lowerCase.equalsIgnoreCase("settype")){
                            //设置类型
                            int i;
                            try {
                                i = Integer.parseInt(args[1]);
                                if (i < 1 || i > 6){
                                    commandSender.sendMessage(help);
                                    return false;
                                }
                                bag.set("gui_type",i);
                            } catch (NumberFormatException e) {
                                commandSender.sendMessage(help);
                                return false;
                            }
                        }else{
                            //设置标题
                            String title = args[1].replace('&','§');
                            bag.set("gui_title",title);
                            itemMeta.setDisplayName(title);
                        }
                        List<String> lore = itemMeta.getLore();
                        lore.remove(lore.size()-1);
                        lore.add(bag.saveToString());
                        itemMeta.setLore(lore);
                        itemStack.setItemMeta(itemMeta);
                        playerInventory.setItemInMainHand(itemStack);
                        commandSender.sendMessage("§a已经修改手上背包");
                        break;
                    }
                    case "give":{
                        if (args.length < 3) {
                            commandSender.sendMessage(help);
                            return false;
                        }
                        player = Bukkit.getPlayer(args[1]);
                        int type;
                        try {
                            type = Integer.parseInt(args[2]);
                            if (type < 1 || type > 6) {
                                commandSender.sendMessage(help);
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            commandSender.sendMessage(help);
                            return false;
                        }
                        if (player == null) {
                            commandSender.sendMessage("§c玩家不存在!");
                            return false;
                        }
                        ItemStack itemStack = new ItemStack(Material.getMaterial(Main.plugin.getConfig().getString("type")));
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.setDisplayName("§6A Bag");
                        ArrayList<String> list = new ArrayList<>();
                        list.add("yaml");
                        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new StringReader(""));
                        yaml.set("uuid", UUID.randomUUID().toString());
                        yaml.set("gui_title","§6A Bag");
                        yaml.set("gui_type",type);
                        list.add(yaml.saveToString());
                        itemMeta.setLore(list);
                        itemStack.setItemMeta(itemMeta);
                        Location location = player.getLocation();
                        location.setY(location.getY()+0.25);
                        Item item = player.getWorld().dropItem(location, itemStack);
                        item.setPickupDelay(0);
                        item.setGlowing(true);
                        item.setGravity(false);
                        item.setCustomName(itemMeta.getDisplayName());
                        item.setCustomNameVisible(true);
                        item.setFireTicks(Integer.MAX_VALUE);
                        item.setInvulnerable(true);
                        commandSender.sendMessage("§a已给与!");
                    }
                }
                return false;
            }
        }
        commandSender.sendMessage(help);
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        int length = args.length;
        if (length < 1) {
            return sub;
        }
        String lowerCase = args[0].toLowerCase();
        if (length == 1) {
            return sub.stream().filter(s-> s.startsWith(lowerCase)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
