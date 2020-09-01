package com.github.zwarunek.timemachine.util;

import com.github.zwarunek.timemachine.TimeMachine;
import com.github.zwarunek.timemachine.commands.Backup;
import com.github.zwarunek.timemachine.commands.Restore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Date;

public class TimeMachineCommand implements CommandExecutor {

    private final TimeMachine plugin;
    public TimeMachineCommand(final TimeMachine instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        File backupFile;
        if(!sender.hasPermission("timemachine")){
            sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " You do no have permission to use this command");

            return true;
        }
        if(args.length == 0){
            sender.sendMessage(ChatColor.DARK_AQUA + "---===###-Time Machine-###===---\n " + ChatColor.RESET +
                    "/tm backup : Starts a backup of the server\n" +
                    "/tm restore server <backup> : restores all server files\n" +
                    "/tm restore world <world:all> <backup> : restores selected world files\n" +
                    "/tm restore player <player:all> <backup> : restores selected player's save file\n" +
                    "/tm restore chunk <world> <x,z|x,z|...> <backup> : Restores chunks to backup\n" + ChatColor.GRAY +
                    "/tm autosave <enable:disable> <nM,H,D> : Enables or disables the autosave feature **Not supported yet");
            return true;
        }
        if(args[0].equalsIgnoreCase("backup")){
            if(plugin.isBackingUp){
                sender.sendMessage(ChatColor.YELLOW + "[WARNING]" + ChatColor.DARK_AQUA + " Server is already backing up");
                return true;
            }
            try {
                sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + "Backup Started at " + plugin.dateFormat.format(new Date()));
                plugin.isBackingUp = true;
                Backup.backup(plugin, sender);

                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if(!plugin.isBackingUp){
                            sender.sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Server was backed up!");
                            Bukkit.getScheduler().cancelTask(Backup.taskIndex);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(plugin, 20, 5);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Failed to backup server. stack trace printed in console");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("restore")){

            if(args[1].equalsIgnoreCase("server")){
                backupFile = new File(plugin.backups.getAbsolutePath() + File.separator + args[2]);
                if(backupFile.exists()){
                    try {
                        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Server is restoring to " + backupFile.getName());
                        Restore.server(plugin, backupFile);
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Server was restored to " + backupFile.getName());
                        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Server restarting in 5 seconds...");
                        plugin.restartServer();
                    }catch(Exception e){
                        sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Restore failed. stack trace printed in console");
                        Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "[ERROR]" + ChatColor.DARK_AQUA + " cannot find file: " + backupFile.getName());
                }
            }
            else if(args[1].equalsIgnoreCase("world")){
                String world = args[2];
                if(Bukkit.getWorld(world) == null){
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " World not found");
                    return true;
                }
                backupFile = new File(plugin.backups.getAbsolutePath() + File.separator + args[3]);
                if(backupFile.exists()){
                    try{
                        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Restore World: restoring " + world + " to " + backupFile.getName());
                        Restore.world(plugin, backupFile, world);
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Restore World: restored " + world + " to " + backupFile.getName());
                        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Server restarting in 5 seconds...");
                        plugin.restartServer();
                    }catch (Exception e){
                        sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Restore failed. Stack trace printed in console");
                        Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
                    }
                }

            }
            else if(args[1].equalsIgnoreCase("player")){
                String player = args[2];
                String playerUUID = "";
                String part = args[3];
                backupFile = new File(plugin.backups.getAbsolutePath() + File.separator + args[4]);
                boolean playerExists = false;
                for(OfflinePlayer p : Bukkit.getOfflinePlayers())
                    if(p.getName() != null && p.getName().equalsIgnoreCase(player)){
                        playerExists = true;
                        playerUUID = p.getUniqueId().toString();
                    }

                if(!playerExists){
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Player not found");
                    return true;
                }
                if(backupFile.exists()){
                    try{
                        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Restore Player: restoring " + player + " to " + backupFile.getName());
                        Restore.player(plugin, backupFile, playerUUID, part);
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Restore Player: restored " + player + " to " + backupFile.getName());
                    }catch (Exception e){
                        sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Restore failed. Stack trace printed in console");
                        Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
                        return true;
                    }
                }

            }
            else if(args[1].equalsIgnoreCase("chunk")){
                if(args.length != 5) {
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Not a valid input");
                    return true;
                }
                backupFile = new File(plugin.backups.getAbsolutePath() + File.separator + args[4]);
                if(!backupFile.exists()){
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Backup not found");
                    return true;
                }
                String world = args[2];
                if(Bukkit.getWorld(world) == null){
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " World not found");
                    return true;
                }
                int[][] chunks;
                String[] tempChunks = args[3].split("\\|");
                chunks = new int[tempChunks.length][2];
                try{
                    for(int i = 0; i < tempChunks.length; i++){
                        String[] temp = tempChunks[i].split(",");
                        for(int j = 0; j < 2; j++){

                            chunks[i][j] = Integer.parseInt(temp[j]);
                        }
                    }
                    Restore.chunk(plugin, backupFile, world, chunks);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Restore Chunks: restored to " + backupFile.getName());
                    Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Server restarting in 5 seconds...");
                    plugin.restartServer();
                }catch (Exception e){
                    sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Not a valid chunk input. Must be in this format: x,z|x,z|x,z...");
                    Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
                    return true;
                }


//                backupFile = new File(plugin.backups.getAbsolutePath() + File.separator + args[4]);
//                if(backupFile.exists()){
//                    try{
//                        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Restore Chunk: restoring chunks in " + world + " to " + backupFile.getName());
//                        Restore.chunk(plugin, backupFile, world, );
//                        sender.sendMessage(ChatColor.GREEN + "[SUCCESS]" + ChatColor.DARK_AQUA + " Restore Chunk: restored chunks in " + world + " to " + backupFile.getName());
//                        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Server restarting in 5 seconds...");
//                        plugin.restartServer();
//                    }catch (Exception e){
//                        sender.sendMessage(ChatColor.RED + "[FAILED]" + ChatColor.DARK_AQUA + " Restore failed. Stack trace printed in console");
//                        Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
//                    }
//                }

            }
            return true;
        }
        sender.sendMessage(ChatColor.AQUA + "[Time Machine]" + ChatColor.DARK_AQUA + " Unknown command. /tm for available commands");
        return true;
    }
}
