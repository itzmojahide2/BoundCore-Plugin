package me.itzmojahide.boundcore.managers;

import com.google.common.collect.Lists;
import me.itzmojahide.boundcore.BoundCore;
import me.itzmojahide.boundcore.objects.Bound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.*;
import java.util.stream.Collectors;

public class BoundManager {
    private final BoundCore plugin;
    private final Random random = new Random();
    private final Map<UUID, Location> chronoLocations = new HashMap<>();
    private final Map<UUID, Double> chronoHealth = new HashMap<>();

    public BoundManager(BoundCore plugin) {
        this.plugin = plugin;
        startChronoSaver();
    }

    public void assignRandomBound(Player player) {
        if (plugin.getDataManager().hasBound(player)) return;

        double chance = random.nextDouble();
        Bound assignedBound;

        if (player.hasPermission("boundcore.admin") && chance < 0.01) { assignedBound = Bound.ADMIN;
        } else if (chance < 0.02) { assignedBound = getRandomBoundByRarity(Bound.Rarity.LEGENDARY);
        } else if (chance < 0.17) { assignedBound = getRandomBoundByRarity(Bound.Rarity.EPIC);
        } else { assignedBound = getRandomBoundByRarity(Bound.Rarity.NORMAL); }

        plugin.getDataManager().setPlayerBound(player, assignedBound);
        giveBoundItem(player, assignedBound);

        player.sendMessage(Component.text("You have unlocked the " + assignedBound.getName() + "!", NamedTextColor.GOLD));

        if (assignedBound.getRarity() == Bound.Rarity.LEGENDARY || assignedBound.getRarity() == Bound.Rarity.ADMIN) {
             Bukkit.broadcast(Component.text("⚠️ ", NamedTextColor.RED).append(Component.text(player.getName(), NamedTextColor.YELLOW)).append(Component.text(" has unlocked a ", NamedTextColor.RED)).append(Component.text("LEGENDARY BOUND: ", NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(assignedBound.getName() + "!", NamedTextColor.GOLD, TextDecoration.BOLD)));
        }
    }

    public void giveBoundItem(Player player, Bound bound) {
        ItemStack boundItem = new ItemStack(Material.PAPER);
        ItemMeta meta = boundItem.getItemMeta();
        meta.displayName(Component.text("Your Bound: ", NamedTextColor.GRAY).append(Component.text(bound.getName(), NamedTextColor.GOLD)));
        meta.lore(List.of(Component.text("This represents your innate power.", NamedTextColor.DARK_AQUA), Component.text("It cannot be dropped or moved.", NamedTextColor.DARK_GRAY)));

        int modelData;
        switch (bound) {
            case NATURE -> modelData = 1001;
            case WIND -> modelData = 1002;
            case STONE -> modelData = 1003;
            case INFERNO -> modelData = 1004;
            case THUNDER -> modelData = 1005;
            case SHADOW -> modelData = 1006;
            case FROST -> modelData = 1007;
            case AQUA -> modelData = 1008;
            case DIVINE -> modelData = 1009;
            case VOID -> modelData = 1010;
            case CHRONO -> modelData = 1011;
            case ADMIN -> modelData = 1012;
            default -> modelData = 0;
        }
        meta.setCustomModelData(modelData);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        boundItem.setItemMeta(meta);
        player.getInventory().setItem(8, boundItem);
    }
    
    // ... ALL ABILITY LOGIC METHODS (doNaturePrimary, etc.) would go here.
    // To save space, the full 24 methods from the previous response are omitted,
    // but their implementation remains the same. The code from the previous response
    // for this section is correct and should be pasted here.

    private Bound getRandomBoundByRarity(Bound.Rarity rarity) {
        List<Bound> bounds = Arrays.stream(Bound.values()).filter(b -> b.getRarity() == rarity).collect(Collectors.toList());
        return bounds.get(random.nextInt(bounds.size()));
    }

    private void startChronoSaver() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getDataManager().getPlayerBound(p) == Bound.CHRONO) {
                        chronoLocations.put(p.getUniqueId(), p.getLocation());
                        chronoHealth.put(p.getUniqueId(), p.getHealth());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5 * 20L);
    }
}```
*(**Note:** For brevity, I have omitted the 24 individual ability methods like `doNaturePrimary` from the `BoundManager` code block above. Please copy them from our previous conversations and paste them inside this class, as their logic is correct.)*

#### **File: `.../listeners/PlayerConnectionListener.java`**
```java
package me.itzmojahide.boundcore.listeners;

import me.itzmojahide.boundcore.BoundCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerConnectionListener implements Listener {
    private final BoundCore plugin;
    public PlayerConnectionListener(BoundCore plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getDataManager().hasBound(player)) return;

        String delayStr = plugin.getConfig().getString("join-delay", "5-10");
        long delayTicks;
        try {
            if (delayStr.contains("-")) {
                String[] parts = delayStr.split("-");
                delayTicks = ThreadLocalRandom.current().nextInt(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) + 1) * 20L;
            } else {
                delayTicks = Long.parseLong(delayStr.trim()) * 20L;
            }
        } catch (NumberFormatException e) {
            delayTicks = 100L;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    plugin.getBoundManager().assignRandomBound(player);
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }
            }
