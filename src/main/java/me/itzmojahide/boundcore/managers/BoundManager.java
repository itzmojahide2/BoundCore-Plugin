package me.itzmojahide.boundcore.managers;

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

    // --- MAIN LOGIC ---

    public void assignRandomBound(Player player) {
        if (plugin.getDataManager().hasBound(player)) return;
        double chance = random.nextDouble();
        Bound assignedBound;
        if (player.hasPermission("boundcore.admin") && chance < 0.01) {
            assignedBound = Bound.ADMIN;
        } else if (chance < 0.02) {
            assignedBound = getRandomBoundByRarity(Bound.Rarity.LEGENDARY);
        } else if (chance < 0.17) {
            assignedBound = getRandomBoundByRarity(Bound.Rarity.EPIC);
        } else {
            assignedBound = getRandomBoundByRarity(Bound.Rarity.NORMAL);
        }
        plugin.getDataManager().setPlayerBound(player, assignedBound);
        giveBoundItem(player, assignedBound);
        player.sendMessage(Component.text("You have unlocked the " + assignedBound.getName() + "!", NamedTextColor.GOLD));
        if (assignedBound.getRarity() == Bound.Rarity.LEGENDARY || assignedBound.getRarity() == Bound.Rarity.ADMIN) {
            Bukkit.broadcast(Component.text("⚠️ ", NamedTextColor.RED).append(Component.text(player.getName(), NamedTextColor.YELLOW)).append(Component.text(" has unlocked a ", NamedTextColor.RED)).append(Component.text("LEGENDARY BOUND: ", NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(assignedBound.getName() + "!", NamedTextColor.GOLD, TextDecoration.BOLD)));
        }
    }

    public void executeAbility(Player player, boolean isShiftClick) {
        Bound bound = plugin.getDataManager().getPlayerBound(player);
        if (bound == null) return;
        CooldownManager cm = plugin.getCooldownManager();
        String abilityType = isShiftClick ? "shift_left_click" : "left_click";
        String cooldownKey = bound.getName() + "_" + abilityType.toUpperCase();
        if (!plugin.getConfig().getBoolean("bounds." + bound.getName() + "." + abilityType + ".enabled", true)) {
            player.sendActionBar(Component.text("This ability is disabled.", NamedTextColor.GRAY));
            return;
        }
        if (cm.isOnCooldown(player, cooldownKey)) {
            cm.sendCooldownMessage(player, cooldownKey);
            return;
        }
        boolean success = false;
        switch (bound) {
            case NATURE -> success = isShiftClick ? doNatureSecondary(player) : doNaturePrimary(player);
            case WIND -> success = isShiftClick ? doWindSecondary(player) : doWindPrimary(player);
            case STONE -> success = isShiftClick ? doStoneSecondary(player) : doStonePrimary(player);
            case INFERNO -> success = isShiftClick ? doInfernoSecondary(player) : doInfernoPrimary(player);
            case THUNDER -> success = isShiftClick ? doThunderSecondary(player) : doThunderPrimary(player);
            case SHADOW -> success = isShiftClick ? doShadowSecondary(player) : doShadowPrimary(player);
            case FROST -> success = isShiftClick ? doFrostSecondary(player) : doFrostPrimary(player);
            case AQUA -> success = isShiftClick ? doAquaSecondary(player) : doAquaPrimary(player);
            case DIVINE -> success = isShiftClick ? doDivineSecondary(player) : doDivinePrimary(player);
            case VOID -> success = isShiftClick ? doVoidSecondary(player) : doVoidPrimary(player);
            case CHRONO -> success = isShiftClick ? doChronoSecondary(player) : doChronoPrimary(player);
            case ADMIN -> success = isShiftClick ? doAdminSecondary(player) : doAdminPrimary(player);
        }
        if (success) {
            int cooldown = plugin.getConfig().getInt("bounds." + bound.getName() + "." + abilityType + ".cooldown", 20);
            cm.setCooldown(player, cooldownKey, cooldown);
        }
    }
    
    private List<LivingEntity> getNearbyEnemies(Player caster, double radius) {
        return caster.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !e.equals(caster))
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());
    }
    
    private List<LivingEntity> getNearbyMobs(Player caster, double radius) {
        return caster.getNearbyEntities(radius, radius, radius).stream()
                .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());
    }
    
    private List<Player> getNearbyPlayers(Player caster, double radius) {
        return caster.getWorld().getPlayers().stream()
                .filter(p -> !p.equals(caster) && p.getLocation().distanceSquared(caster.getLocation()) <= radius * radius)
                .collect(Collectors.toList());
    }

    private boolean doNaturePrimary(Player p) {
        Block block = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (block.getType() != Material.GRASS_BLOCK && !block.getType().name().contains("LEAVES") && !block.getType().name().contains("FLOWER")) {
            p.sendMessage(Component.text("🌿 You must be near nature to use this!", NamedTextColor.RED));
            return false;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 0));
        p.getWorld().spawnParticle(Particle.HEART, p.getEyeLocation(), 15, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.2f);
        p.sendMessage(Component.text("🌿 Nature flows through you!", NamedTextColor.GREEN));
        return true;
    }

    private boolean doNatureSecondary(Player p) {
        Block block = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (block.getType() != Material.GRASS_BLOCK && !block.getType().name().contains("LEAVES") && !block.getType().name().contains("FLOWER")) {
            p.sendMessage(Component.text("🌿 You must be near nature to use this!", NamedTextColor.RED));
            return false;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 0));
        p.getWorld().spawnParticle(Particle.COMPOSTER, p.getLocation().add(0, 1, 0), 30, 0.4, 0.4, 0.4, 0);
        p.playSound(p.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1.0f, 1.0f);
        p.sendMessage(Component.text("🌿 You move faster among life!", NamedTextColor.GREEN));
        return true;
    }

    private boolean doWindPrimary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10 * 20, 1));
        p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.01);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
        p.sendMessage(Component.text("💨 You soar on the wind!", NamedTextColor.AQUA));
        return true;
    }

    private boolean doWindSecondary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5 * 20, 0));
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);
        p.playSound(p.getLocation(), Sound.ITEM_ELYTRA_DEPLOY, 1.0f, 1.2f);
        p.sendMessage(Component.text("💨 You glide gracefully!", NamedTextColor.AQUA));
        return true;
    }

    private boolean doStonePrimary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10 * 20, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 15 * 20, 0));
        p.getWorld().spawnParticle(Particle.BLOCK_DUST, p.getLocation().add(0, 1, 0), 50, 0.3, 0.5, 0.3, Material.STONE.createBlockData());
        p.playSound(p.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);
        p.sendMessage(Component.text("🪨 You are as strong as stone!", NamedTextColor.GRAY));
        return true;
    }

    private boolean doStoneSecondary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.KNOCKBACK_RESISTANCE, 5 * 20, 9));
        p.getWorld().spawnParticle(Particle.CRIT, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 0.5f);
        p.sendMessage(Component.text("🪨 Your body resists all impact!", NamedTextColor.GRAY));
        return true;
    }

    private boolean doInfernoPrimary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 20, 0));
        getNearbyEnemies(p, 3.0).forEach(e -> e.setFireTicks(5 * 20));
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 50, 1, 1, 1, 0.05);
        p.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
        p.sendMessage(Component.text("🔥 Flames scorch your foes!", NamedTextColor.RED));
        return true;
    }

    private boolean doInfernoSecondary(Player p) {
        p.getWorld().spawnParticle(Particle.LAVA, p.getLocation().add(0, 1, 0), 100, 2.5, 1, 2.5, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
        p.sendMessage(Component.text("🔥 Fire erupts around you!", NamedTextColor.RED));
        getNearbyEnemies(p, 5.0).forEach(e -> e.damage(6.0, p));
        return true;
    }

    private boolean doThunderPrimary(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 8 * 20, 0));
        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p.getEyeLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        p.sendMessage(Component.text("⚡ Storm strikes your enemies!", NamedTextColor.YELLOW));
        return true;
    }

    private boolean doThunderSecondary(Player p) {
        p.getWorld().spawnParticle(Particle.FLASH, p.getLocation(), 1, 0, 0, 0, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.5f);
        getNearbyEnemies(p, 5.0).forEach(e -> e.getWorld().strikeLightning(e.getLocation()));
        p.sendMessage(Component.text("⚡ Lightning erupts around you!", NamedTextColor.YELLOW));
        return true;
    }

    private boolean doShadowPrimary(Player p) {
        if (!p.isSneaking()) {
            p.sendMessage(Component.text("🌑 You must be sneaking to vanish.", NamedTextColor.DARK_PURPLE));
            return false;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 0));
        p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation().add(0, 1, 0), 40, 0.5, 0.8, 0.5, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
        p.sendMessage(Component.text("🌑 You vanish into the shadows!", NamedTextColor.DARK_PURPLE));
        return true;
    }

    private boolean doShadowSecondary(Player p) {
        Location targetLoc = p.getEyeLocation().add(p.getLocation().getDirection().multiply(5));
        if (targetLoc.getBlock().isPassable()) {
            p.teleport(targetLoc);
            p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            p.sendMessage(Component.text("🌑 You dash through the darkness!", NamedTextColor.DARK_PURPLE));
            return true;
        } else {
            p.sendMessage(Component.text("🌑 Cannot dash through solid objects.", NamedTextColor.GRAY));
            return false;
        }
    }

    private boolean doFrostPrimary(Player p) {
        getNearbyMobs(p, 5.0).forEach(e -> {
            e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 4 * 20, 4));
            e.setFreezeTicks(8 * 20);
        });
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation().add(0, 1, 0), 100, 2.5, 1, 2.5, 0);
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        p.sendMessage(Component.text("❄️ Cold freezes your enemies!", NamedTextColor.BLUE));
        return true;
    }

    private boolean doFrostSecondary(Player p) {
        Location start = p.getLocation();
        Vector direction = p.getEyeLocation().getDirection().setY(0).normalize();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                Location wallLoc = start.clone().add(direction.clone().multiply(2)).add(direction.getZ() * -x, y, direction.getX() * x);
                if (wallLoc.getBlock().getType() == Material.AIR) {
                    wallLoc.getBlock().setType(Material.PACKED_ICE);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (wallLoc.getBlock().getType() == Material.PACKED_ICE) {
                                wallLoc.getBlock().setType(Material.AIR);
                            }
                        }
                    }.runTaskLater(plugin, 5 * 20L);
                }
            }
        }
        p.playSound(p.getLocation(), Sound.BLOCK_GLASS_PLACE, 1.0f, 1.0f);
        p.sendMessage(Component.text("❄️ An icy wall rises!", NamedTextColor.BLUE));
        return true;
    }

    private boolean doAquaPrimary(Player p) {
        if (!p.isInWater()) {
            p.sendMessage(Component.text("💧 You must be in water to use this.", NamedTextColor.RED));
            return false;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 30 * 20, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 20 * 20, 0));
        p.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, p.getLocation(), 30, 0.5, 1, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_DOLPHIN_SWIM, 1.0f, 1.0f);
        p.sendMessage(Component.text("💧 You flow with the ocean!", NamedTextColor.DARK_AQUA));
        return true;
    }

    private boolean doAquaSecondary(Player p) {
        Location center = p.getLocation();
        p.getWorld().spawnParticle(Particle.WATER_SPLASH, center.add(0, 1, 0), 100, 1.5, 1, 1.5, 0);
        p.playSound(center, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.0f, 1.0f);
        p.sendMessage(Component.text("💧 Waves push your enemies away!", NamedTextColor.DARK_AQUA));
        getNearbyEnemies(p, 3.0).forEach(e -> {
            Vector knockback = e.getLocation().toVector().subtract(center.toVector()).normalize().multiply(1.5).setY(0.5);
            e.setVelocity(knockback);
        });
        return true;
    }

    private boolean doDivinePrimary(Player p) {
        p.setInvulnerable(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getWorld().spawnParticle(Particle.TOTEM, p.getEyeLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        p.sendMessage(Component.text("🩵 You are untouchable!", NamedTextColor.WHITE));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setInvulnerable(false);
                if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }
        }.runTaskLater(plugin, 10 * 20L);
        return true;
    }

    private boolean doDivineSecondary(Player p) {
        p.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, p.getLocation(), 100, 5, 2, 5, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        p.sendMessage(Component.text("🩵 The heavens heal all nearby!", NamedTextColor.WHITE));
        getNearbyPlayers(p, 10.0).forEach(player -> player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + 10)));
        return true;
    }

    private boolean doVoidPrimary(Player p) {
        Location center = p.getEyeLocation().add(p.getLocation().getDirection().multiply(4));
        p.getWorld().spawnParticle(Particle.SQUID_INK, center, 200, 1, 1, 1, 0.1);
        p.playSound(center, Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.5f);
        p.sendMessage(Component.text("🕳️ Reality bends around you!", NamedTextColor.DARK_GRAY));
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 5 * 20) { this.cancel(); return; }
                getNearbyEnemies(p, 8.0).forEach(e -> {
                    Vector pull = center.toVector().subtract(e.getLocation().toVector()).normalize().multiply(0.5);
                    e.setVelocity(pull);
                });
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
        return true;
    }

    private boolean doVoidSecondary(Player p) {
        Location center = p.getLocation();
        p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, center, 1, 0, 0, 0, 0);
        p.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 0.8f);
        p.sendMessage(Component.text("🕳️ A force pushes foes away!", NamedTextColor.DARK_GRAY));
        getNearbyEnemies(p, 3.0).forEach(e -> {
            Vector push = e.getLocation().toVector().subtract(center.toVector()).normalize().multiply(2.0).setY(0.6);
            e.setVelocity(push);
        });
        return true;
    }

    private boolean doChronoPrimary(Player p) {
        Location pastLoc = chronoLocations.get(p.getUniqueId());
        Double pastHealth = chronoHealth.get(p.getUniqueId());
        if (pastLoc == null || pastHealth == null) {
            p.sendMessage(Component.text("🔮 Time has not been recorded yet.", NamedTextColor.GRAY));
            return false;
        }
        p.teleport(pastLoc);
        p.setHealth(pastHealth);
        p.getWorld().spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.5f);
        p.sendMessage(Component.text("🔮 Time bends to your will!", NamedTextColor.LIGHT_PURPLE));
        return true;
    }

    private boolean doChronoSecondary(Player p) {
        getNearbyMobs(p, 8.0).forEach(e -> e.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 3 * 20, 2)));
        p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getLocation().add(0, 1, 0), 100, 4, 1, 4, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
        p.sendMessage(Component.text("🔮 Time slows for your foes!", NamedTextColor.LIGHT_PURPLE));
        return true;
    }

    private boolean doAdminPrimary(Player p) {
        p.setInvulnerable(true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.getWorld().spawnParticle(Particle.WAX_ON, p.getEyeLocation(), 50, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.8f);
        p.sendMessage(Component.text("💠 You command the skies!", NamedTextColor.GOLD));
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setInvulnerable(false);
                if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }
        }.runTaskLater(plugin, 10 * 20L);
        return true;
    }

    private boolean doAdminSecondary(Player p) {
        p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        p.sendMessage(Component.text("💠 Lightning bends to your will!", NamedTextColor.GOLD));
        getNearbyEnemies(p, 15.0).forEach(e -> e.getWorld().strikeLightning(e.getLocation()));
        getNearbyPlayers(p, 15.0).forEach(player -> player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
        return true;
    }

    // --- MISC UTILITY METHODS ---

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
                        // THIS IS THE FIXED LINE
                        chronoLocations.put(p.getUniqueId(), p.getLocation());
                        chronoHealth.put(p.getUniqueId(), p.getHealth());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 5 * 20L);
    }
    }
