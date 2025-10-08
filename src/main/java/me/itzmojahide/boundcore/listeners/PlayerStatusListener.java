package me.itzmojahide.boundcore.listeners;

import me.itzmojahide.boundcore.BoundCore;
import me.itzmojahide.boundcore.objects.Bound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerStatusListener implements Listener {

    private final BoundCore plugin;
    private final Random random = new Random();

    public PlayerStatusListener(BoundCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Bound bound = plugin.getDataManager().getPlayerBound(player);
        if (bound == null) return;

        switch (bound) {
            case WIND:
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    event.setDamage(event.getDamage() * 1.5);
                }
                break;
            case INFERNO:
                // --- THIS IS THE FIXED SECTION ---
                // We check for water and drowning damage separately.
                if (player.isInWater() || event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                    event.setDamage(event.getDamage() * 2.0);
                }
                break;
            case FROST:
                EntityDamageEvent.DamageCause cause = event.getCause();
                if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.LAVA) {
                    event.setDamage(event.getDamage() * 2.0);
                }
                break;
            case STONE:
                if (!player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
                }
                break;
            case AQUA:
                if (!player.isInWater() && !player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
                } else if (player.isInWater() && player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                    player.removePotionEffect(PotionEffectType.SLOWNESS);
                }
                break;
        }
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Bound bound = plugin.getDataManager().getPlayerBound(player);
        if (bound == null) return;

        if (bound == Bound.THUNDER) {
            if (random.nextDouble() < 0.15) { // 15% chance
                event.getEntity().getWorld().strikeLightning(event.getEntity().getLocation());
                if (random.nextDouble() < 0.10) { // 10% chance self-zap
                    player.damage(2.0);
                    player.getWorld().strikeLightning(player.getLocation());
                }
            }
        }
    }
        }
