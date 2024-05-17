package me.gsqfi.storagebag.storagebag;

import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Main extends JavaPlugin {
    public static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        reloadConfig();
        ItemStackNBT.init();
        PluginCommand command = getCommand(this.getDescription().getName().toLowerCase());
        Cmd executor = new Cmd();
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        getServer().getPluginManager().registerEvents(new PlayerListener(),this);
        getLogger().info("§aPlugin Loaded!");
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        Cmd.help = getConfig().getStringList("msg.help").stream().map(s->s.replace('&','§'))
                .toArray(String[]::new);
    }
}