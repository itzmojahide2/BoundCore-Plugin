package me.itzmojahide.boundcore.managers;

import me.itzmojahide.boundcore.BoundCore;
import me.itzmojahide.boundcore.objects.Bound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    private final BoundCore plugin;
    private final Map<UUID, Bound> playerBounds = new HashMap<>();
    private File customConfigFile;
    private FileConfiguration customConfig;

    public DataManager(BoundCore plugin) {
        this.plugin = plugin;
        createCustomConfig();
        loadPlayerData();
    }

    public void setPlayerBound(Player player, Bound bound) {
        playerBounds.put(player.getUniqueId(), bound);
        savePlayerData(player.getUniqueId(), bound);
    }

    public Bound getPlayerBound(Player player) { return playerBounds.get(player.getUniqueId()); }
    public boolean hasBound(Player player) { return playerBounds.containsKey(player.getUniqueId()); }

    private void loadPlayerData() {
        if (customConfig.getConfigurationSection("players") == null) return;
        for (String uuidString : customConfig.getConfigurationSection("players").getKeys(false)) {
            playerBounds.put(UUID.fromString(uuidString), Bound.fromString(customConfig.getString("players." + uuidString)));
        }
    }

    private void savePlayerData(UUID uuid, Bound bound) {
        customConfig.set("players." + uuid.toString(), bound.getName());
        try { customConfig.save(customConfigFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private void createCustomConfig() {
        customConfigFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!customConfigFile.exists()) {
            try { customConfigFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }
}
