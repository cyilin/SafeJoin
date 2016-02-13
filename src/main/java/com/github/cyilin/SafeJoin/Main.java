package com.github.cyilin.SafeJoin;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.ess3.api.IEssentials;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

    private HashMap<String, Long> timestamp = new HashMap();
    private List<String> worlds;
    private String command;
    private IEssentials ess;
    private int mode;
    private boolean enable;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String act = (args.length == 0 ? "" : args[0].toLowerCase());
        sender.sendMessage(ChatColor.GOLD + "[SafeJoin] " + ChatColor.BLUE + "==SafeJoin "
                + getServer().getPluginManager().getPlugin("SafeJoin").getDescription().getVersion() + "==");
        sender.sendMessage(ChatColor.GOLD + "[SafeJoin] " + ChatColor.WHITE + "/safejoin reload");
        sender.sendMessage(ChatColor.GOLD + "[SafeJoin] " + ChatColor.WHITE + "by cylin https://github.com/cyilin");
        if (act.equals("reload") && sender.hasPermission("safejoin.admin")) {
            sender.sendMessage(ChatColor.GOLD + "[SafeJoin] " + ChatColor.GREEN + "reload...");
            reload();
            sender.sendMessage(ChatColor.GOLD + "[SafeJoin] " + ChatColor.GREEN + "done.");
        }
        return true;
    }

    @Override
    public void onDisable() {
        PlayerJoinEvent.getHandlerList().unregister((JavaPlugin) this);
        this.worlds = null;
        this.timestamp = new HashMap();
        this.ess = null;
    }

    public void reload() {
        onDisable();
        onEnable();
    }

    public Date stringToDate(String s) {
        Date date = new Date();
        DateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = d.parse(s);
        } catch (Exception e) {
        }
        return date;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            getConfig().load(new File(getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        getCommand("safejoin").setExecutor(this);
        this.enable = getConfig().getBoolean("enable");
        if (this.enable) {
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            return;
        }
        this.mode = getConfig().getInt("mode");
        this.worlds = getConfig().getStringList("worlds");
        this.command = getConfig().getString("command");
        this.load();
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!this.enable) {
            return;
        }
        Player player = event.getPlayer();
        Long logout = ess.getUser(player).getLastLogout();
        String world_name = player.getLocation().getWorld().getName();
        if (worlds.contains(world_name)) {
            if (logout < this.timestamp.get(world_name)) {
                if (this.mode == 1) {
                    player.setNoDamageTicks(100);
                    getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{world_name,player.getName(), this.command});
                    getServer().dispatchCommand(player, this.command);
                } else if (this.mode == 2) {
                    player.setNoDamageTicks(100);
                    String c = this.command.replace("{player}", player.getName());
                    getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{world_name,player.getName(), c});
                    getServer().dispatchCommand(getServer().getConsoleSender(), c);
                }
            }
        }
    }

    public void load() {
        ess = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
        FileConfiguration data = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "data.yml"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time = System.currentTimeMillis();
        String d = format.format(time);
        Date date = null;
        try {
            date = format.parse(d);
        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Iterator<World> world = getServer().getWorlds().iterator(); world.hasNext();) {
            String world_name = world.next().getName();
            if (!worlds.contains(world_name)) {
                continue;
            }
            Long world_seed = data.getLong(world_name + ".seed");
            if (!world_seed.equals(getServer().getWorld(world_name).getSeed())) {
                data.set(world_name + ".seed", getServer().getWorld(world_name).getSeed());
                data.set(world_name + ".date", d);
                this.timestamp.put(world_name, time);
            } else {
                this.timestamp.put(world_name, this.stringToDate(data.getString(world_name + ".date")).getTime());
            }

        }
        try {
            data.save(new File(getDataFolder(), "data.yml"));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
