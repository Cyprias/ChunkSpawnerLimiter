package com.cyprias.ChunkSpawnerLimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

import com.cyprias.ChunkSpawnerLimiter.compare.MobGroupCompare;
import com.cyprias.ChunkSpawnerLimiter.listeners.EntityListener;
import com.cyprias.ChunkSpawnerLimiter.listeners.WorldListener;

public class Plugin extends JavaPlugin {

	@Override
	public void onEnable() {

		// Save default config if it does not exist.
		saveDefaultConfig();

		// Warn console if config is missing properties.
		checkForMissingProperties();

		// Register our event listener.
		getServer().getPluginManager().registerEvents(new EntityListener(this), this);
		getServer().getPluginManager().registerEvents(new WorldListener(this), this);

		// Start the Metrics.
		if (getConfig().getBoolean("properties.use-metrics")) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {}
		}

	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
	}

	private void checkForMissingProperties() {
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			getLogger().severe("Config file does not exist! Using defaults for all values.");
			return;
		}
		YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(configFile);
		BufferedReader buffered = new BufferedReader(new InputStreamReader(getResource("config.yml")));
		YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(buffered);
		try {
			buffered.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String property : defaultConfig.getKeys(true)) {
			if (!diskConfig.contains(property))
				getLogger().warning(property + " is missing from your config.yml, using default.");
		}
	}

	public void checkChunk(Chunk c) {
		// Stop processing quickly if this world is excluded from limits.
		if (getConfig().getStringList("excluded-worlds").contains(c.getWorld().getName())) {
			return;
		}

		Entity[] ents = c.getEntities();

		HashMap<String, ArrayList<Entity>> types = new HashMap<String, ArrayList<Entity>>();

		for (int i = ents.length - 1; i >= 0; i--) {
			// ents[i].getType();
			EntityType t = ents[i].getType();

			String eType = t.toString();
			String eGroup = MobGroupCompare.getMobGroup(ents[i]);

			if (getConfig().contains("entities." + eType)) {
				if (!types.containsKey(eType))
					types.put(eType, new ArrayList<Entity>());
				types.get(eType).add(ents[i]);
			}

			if (getConfig().contains("entities." + eGroup)) {
				if (!types.containsKey(eGroup))
					types.put(eGroup, new ArrayList<Entity>());
				types.get(eGroup).add(ents[i]);
			}
		}

		for (Entry<String, ArrayList<Entity>> entry : types.entrySet()) {
			String eType = entry.getKey();
			int limit = getConfig().getInt("entities." + eType);

			if (entry.getValue().size() > limit) {
				debug("Removing " + (entry.getValue().size() - limit) + " " + eType + " @ "
						+ c.getX() + " " + c.getZ());

				if (getConfig().getBoolean("properties.notify-players")) {
					for (int i = ents.length - 1; i >= 0; i--) {
						if (ents[i] instanceof Player) {
							Player p = (Player) ents[i];
							ChatUtils.send(p, String.format(getConfig().getString("messages.removedEntites"),
									entry.getValue().size() - limit, eType));
						}
					}
				}

				for (int i = entry.getValue().size() - 1; i >= limit; i--) {
					entry.getValue().get(i).remove();
				}
			}
		}
	}

	public void debug(String mess) {
		if (getConfig().getBoolean("properties.debug-messages"))
			getLogger().info(ChatUtils.cleanColorCodes("[Debug] " + mess));
	}

}
