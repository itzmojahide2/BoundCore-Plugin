package me.itzmojahide.boundcore.listeners;

import me.itzmojahide.boundcore.BoundCore;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractionListener implements Listener {
    private final BoundCore plugin;
    public PlayerInteractionListener(BoundCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;
        
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) event.setCancelled(true);
        // This part needs to call the ability execution logic from BoundManager
        // plugin.getBoundManager().executeAbility(event.getPlayer(), event.getPlayer().isSneaking());
    }
}
