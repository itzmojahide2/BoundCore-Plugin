package me.itzmojahide.boundcore.listeners;

import me.itzmojahide.boundcore.BoundCore;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryManagementListener implements Listener {
    private final BoundCore plugin;
    public InventoryManagementListener(BoundCore plugin) { this.plugin = plugin; }

    private boolean isBoundItem(ItemStack item) {
        return item != null && item.getType() == Material.PAPER && item.hasItemMeta() && item.getItemMeta().hasCustomModelData();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
            if (event.getSlot() == 8 || isBoundItem(event.getCursor()) || isBoundItem(event.getCurrentItem())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isBoundItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getDataManager().hasBound(event.getPlayer())) {
             plugin.getBoundManager().giveBoundItem(event.getPlayer(), plugin.getDataManager().getPlayerBound(event.getPlayer()));
        }
    }
}
