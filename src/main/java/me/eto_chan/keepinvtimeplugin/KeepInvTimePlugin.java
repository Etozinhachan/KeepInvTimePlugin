package me.eto_chan.keepinvtimeplugin;

import me.eto_chan.keepinvtimeplugin.listeners.DeathListener;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.UUID;

public final class KeepInvTimePlugin extends JavaPlugin {


    public long protectionTimeTicks = 20 * (60 * 5);
    public long deleteTimeTicks = (long) (20 * (60 * 0.2));


    @Override
    public void onEnable() {
        // Register event listener when the plugin is enabled
        Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);

        // Start the keepInventory scheduler to toggle it based on real-world time
        startKeepInventoryScheduler();
    }

    // Scheduler to check real-world time and toggle the keepInventory game rule
    private void startKeepInventoryScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get the current time from the server's system clock
                LocalTime now = LocalTime.now();

                // If the time is between 10:00 AM and 12:00 AM (midnight), enable keepInventory
                if (now.isAfter(LocalTime.of(10, 0)) && now.isBefore(LocalTime.of(23, 59))) {
                    Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.KEEP_INVENTORY, true));
                } else {
                    // If the time is outside of this range, disable keepInventory
                    Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.KEEP_INVENTORY, false));
                }
            }
            // Run this check every 5 minutes (5 * 60 seconds = 300 ticks)
        }.runTaskTimer(this, 0, 20 * 60 * 5);
    }
}
