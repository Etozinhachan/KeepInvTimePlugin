package me.eto_chan.keepinvtimeplugin.listeners;

import io.papermc.paper.event.block.BlockBreakBlockEvent;
import me.eto_chan.keepinvtimeplugin.KeepInvTimePlugin;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DeathListener implements Listener {

    // Store protected chests with a timestamp for each player
    private final HashMap<Chest, HashMap<UUID, Long>> protectedChests = new HashMap<>();
    private final KeepInvTimePlugin _plugin;

    public DeathListener(KeepInvTimePlugin plugin){
        _plugin = plugin;
    }


    // Event handler for when a player dies
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        if (Boolean.FALSE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {

            if (event.getDrops().isEmpty()){
                return;
            }
            // If the player was not killed by another player

            // Get the location where the player died
            Location deathLocation = player.getLocation();

            // Get the block at the death location and set it to be a chest
            Block block = deathLocation.getBlock();
            block.setType(Material.CHEST);


            // Get the chest's inventory and transfer the player's dropped items into it
            Chest chest = (Chest) block.getState();
            Inventory chestInventory = chest.getInventory();

            System.out.println(Arrays.toString(chestInventory.getContents()));

            chestInventory.setContents(event.getDrops().toArray(new ItemStack[0]));

            // Clear the dropped items from the death event to prevent them from appearing on the ground
            event.getDrops().clear();

            if (player.getKiller() == null) {

                // Store the chest protection time using the player's UUID
                UUID playerUUID = player.getUniqueId();

                HashMap<UUID, Long> playerHashMap = new HashMap<>();
                playerHashMap.put(playerUUID, System.currentTimeMillis());

                protectedChests.put(chest, playerHashMap);

                // Protect the chest for 5 minutes (5 * 60 seconds)
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // After 5 minutes, remove the chest protection for the player
                        protectedChests.remove(chest);
                    }
                }.runTaskLater(_plugin, _plugin.protectionTimeTicks); // Delay of 5 minutes
            }

            new BukkitRunnable(){
                @Override
                public void run(){
                    chest.getBlock().setType(Material.AIR);
                }
            }.runTaskLater(_plugin, _plugin.deleteTimeTicks);
        }

    }


    @EventHandler
    public void onChestInteract(PlayerInteractEvent event){

        try {


            if (event.hasBlock()){
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
                    System.out.println(event.getMaterial());
                    System.out.println(Objects.requireNonNull(event.getClickedBlock()).getType());
                    System.out.println(protectedChests);

                    if (Objects.requireNonNull(event.getClickedBlock()).getType() == Material.CHEST) {

                        Player player = event.getPlayer();

                        Chest interactedChest = (Chest) Objects.requireNonNull(event.getClickedBlock()).getState();

                        HashMap<UUID, Long> playerHashMap = protectedChests.get(interactedChest);

                        if (interactedChest.getInventory().isEmpty()){
                            if (playerHashMap != null) {

                                protectedChests.remove(interactedChest);
                            }

                            interactedChest.getBlock().setType(Material.AIR);
                            return;
                        }

                        if (playerHashMap == null) {
                            return;
                        }

                        if (!playerHashMap.containsKey(player.getUniqueId())) {

                            UUID ownerUUID = (UUID) playerHashMap.keySet().toArray()[0];
                            long startTimeOfChestProtection = playerHashMap.get(ownerUUID);

                            event.setCancelled(true);
                            player.sendMessage("O bau que voce interagiu com esta protegido por " + (60 * 5 - ((System.currentTimeMillis() - startTimeOfChestProtection) / 1000)) + "s");

                        }

                    }
                }

            }


        }catch (NullPointerException ex){
            ex.printStackTrace();
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){

        if (Objects.requireNonNull(event.getBlock()).getType() == Material.CHEST) {
            Chest chest = (Chest) event.getBlock().getState();
            HashMap<UUID, Long> playerHashMap = protectedChests.get(chest);

            if (playerHashMap == null){
                return;
            }

            Player player = event.getPlayer();


            if (playerHashMap.containsKey(player.getUniqueId())) {
            //    event.setCancelled(true);
                return;
            }

            UUID ownerUUID = (UUID) playerHashMap.keySet().toArray()[0];
            long startTimeOfChestProtection = playerHashMap.get(ownerUUID);

            event.setCancelled(true);
            player.sendMessage("O bau que voce tentou destruir esta protegido por " + (20 * 60 * 5 - ((System.currentTimeMillis() - startTimeOfChestProtection) / 1000 * 20)) + "s");


        }


    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){

        Iterator<Block> it = event.blockList().iterator();

        while(it.hasNext()){
            Block nextBlock = it.next();
            if (nextBlock.getType() == Material.CHEST){
                Chest chest = (Chest) nextBlock.getState();

                HashMap<UUID, Long> playerHashMap = protectedChests.get(chest);

                if (playerHashMap == null){
                    return;
                }

                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){

        Iterator<Block> it = event.blockList().iterator();

        while(it.hasNext()){
            Block nextBlock = it.next();
            if (nextBlock.getType() == Material.CHEST){
                Chest chest = (Chest) nextBlock.getState();

                HashMap<UUID, Long> playerHashMap = protectedChests.get(chest);

                if (playerHashMap == null){
                    return;
                }

                event.setCancelled(true);
            }
        }

    }

}
