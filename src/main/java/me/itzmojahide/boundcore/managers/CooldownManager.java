package me.itzmojahide.boundcore.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public void setCooldown(Player player, String key, long seconds) {
        if (seconds <= 0) return;
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, System.currentTimeMillis() + (seconds * 1000));
    }

    private long getRemainingCooldown(Player player, String key) {
        long expiryTime = cooldowns.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(key, 0L);
        if (System.currentTimeMillis() > expiryTime) return 0;
        return TimeUnit.MILLISECONDS.toSeconds(expiryTime - System.currentTimeMillis()) + 1;
    }

    public boolean isOnCooldown(Player player, String key) { return getRemainingCooldown(player, key) > 0; }

    public void sendCooldownMessage(Player player, String key) {
        player.sendActionBar(Component.text("Ability on cooldown for " + getRemainingCooldown(player, key) + "s", NamedTextColor.RED));
    }
}
