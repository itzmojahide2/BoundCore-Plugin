package me.itzmojahide.boundcore;

import me.itzmojahide.boundcore.listeners.InventoryManagementListener;
import me.itzmojahide.boundcore.listeners.PlayerConnectionListener;
import me.itzmojahide.boundcore.listeners.PlayerInteractionListener;
import me.itzmojahide.boundcore.listeners.PlayerStatusListener;
import me.itzmojahide.boundcore.managers.BoundManager;
import me.itzmojahide.boundcore.managers.CooldownManager;
import me.itzmojahide.boundcore.managers.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoundCore extends JavaPlugin {

    private DataManager dataManager;
    private CooldownManager cooldownManager;
    private BoundManager boundManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.dataManager = new DataManager(this);
        this.cooldownManager = new CooldownManager();
        this.boundManager = new BoundManager(this);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStatusListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryManagementListener(this), this);

        getLogger().info("BoundCore has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BoundCore has been disabled.");
    }

    public DataManager getDataManager() { return dataManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public BoundManager getBoundManager() { return boundManager; }
}
