package me.gsqfi.storagebag.storagebag;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Main plugin;

    @Override
    public void onEnable() {
        plugin = this;
        reloadConfig();
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