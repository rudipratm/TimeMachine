package com.github.zwarunek.timemachine;

import com.github.zwarunek.timemachine.commands.Backup;
import com.github.zwarunek.timemachine.items.ChunkWand;
import com.github.zwarunek.timemachine.util.ItemListener;
import com.github.zwarunek.timemachine.util.TimeMachineCommand;
import com.github.zwarunek.timemachine.util.TimeMachineTabCompleter;
import com.github.zwarunek.timemachine.util.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeMachine extends JavaPlugin{

    public int autoBackupFrequency;
    public File backups;
    public File mainDir;
    public File playerDataDir;
    public File pluginDir;
    public String backupPath;
    public String backupNameFormat;
    public SimpleDateFormat dateFormat;
    public boolean isBackingUp = false;
    public boolean restorePlayerWithWorld;
    public boolean autosaveEnabled;
    public ChunkWand chunkWand;
    public List<String> backupFolderExceptions;
    public List<String> backupExtensionExceptions;
    public final String version = this.getDescription().getVersion();
    public final List<String> author = this.getDescription().getAuthors();


    @Override
    public void onEnable() {
        displayBanner();
        new UpdateChecker(this).checkForUpdate();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        File langFile = new File(this.getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", langFile, Arrays.asList("none"));
        } catch (IOException e) {
            this.getServer().getConsoleSender().sendMessage(e.getMessage());
        }
        this.reloadConfig();
        mainDir = getDataFolder().getAbsoluteFile().getParentFile().getParentFile();
        playerDataDir = new File(mainDir.getAbsolutePath() + File.separator + "world" + File.separator + "playerdata");
        pluginDir = new File(mainDir.getAbsolutePath() + File.separator + "plugins");
        backupPath =  getConfig().getString("backupFolderDirectory", "");
        backups = new File(mainDir + backupPath + File.separator + "backups" + File.separator);
        autosaveEnabled = getConfig().getBoolean("autoBackups");
        autoBackupFrequency = getConfig().getInt("autoBackupFrequency");
        backupNameFormat =getConfig().getString("backupNameFormat");
        dateFormat = new SimpleDateFormat(getConfig().getString("dateFormat"));
        restorePlayerWithWorld = getConfig().getBoolean("restorePlayerWithWorld");
        backupExtensionExceptions = (List<String>)getConfig().getList("backupExtensionExceptions");
        backupFolderExceptions = (List<String>)getConfig().getList("backupFolderExceptions");

        chunkWand = new ChunkWand();
        final TimeMachineCommand command = new TimeMachineCommand(this);
        final TimeMachineTabCompleter tabCompleter = new TimeMachineTabCompleter(this);
        ItemListener itemListener = new ItemListener(this);
        getServer().getPluginManager().registerEvents(itemListener, this);
        getCommand("timemachine").setExecutor(command);
        getCommand("timemachine").setTabCompleter(tabCompleter);
        if(!backups.exists())
            backups.mkdir();
        new Backup().autosave(this);
    }

    @Override
    public void onDisable() {
        if(chunkWand.isInUse){
            chunkWand.player.getInventory().remove(chunkWand.chunkWand);
        }
    }
    public void restartServer(){

        new BukkitRunnable(){

            @Override
            public void run() {
                Bukkit.spigot().restart();
            }
        }.runTaskLater(this, 100);
    }

    private void displayBanner(){

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + " _______             __  ___         __   _         ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "/_  __(_)_ _  ___   /  |/  /__ _____/ /  (_)__  ___ ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + " / / / /  ' \\/ -_) / /|_/ / _ `/ __/ _ \\/ / _ \\/ -_)");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_AQUA + "/_/ /_/_/_/_/\\__/ /_/  /_/\\_,_/\\__/_//_/_/_//_/\\__/ ");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY +      "        Version " + ChatColor.GOLD + version);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY +      "        Author  " + ChatColor.WHITE + author.get(0));
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.DARK_GRAY +      "*************************************************************" + ChatColor.WHITE + author.get(0));

    }
}
