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

    public PlayerInteractionListener(BoundCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // We only care about main hand clicks (left clicks)
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Ensure it's a left click action
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        // Player must have an empty hand to use abilities
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) return;

        // Prevent the player from breaking blocks by accident when left-clicking
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
        }

        // --- THIS IS THE FIXED LINE ---
        // This line actually runs the ability from the BoundManager
        plugin.getBoundManager().executeAbility(event.getPlayer(), event.getPlayer().isSneaking());
    }
}
