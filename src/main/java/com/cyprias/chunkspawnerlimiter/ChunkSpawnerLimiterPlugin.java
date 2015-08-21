package com.cyprias.chunkspawnerlimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

import com.cyprias.chunkspawnerlimiter.listeners.EntityListener;
import com.cyprias.chunkspawnerlimiter.listeners.WorldListener;

public class ChunkSpawnerLimiterPlugin extends JavaPlugin {

	@Override
	public void onEnable() {

		// Save default config if it does not exist.
		saveDefaultConfig();

		// Warn console if config is missing properties.
		checkForMissingProperties();

		// Register our event listener.
		if (getConfig().getBoolean("properties.watch-creature-spawns")) {
			getServer().getPluginManager().registerEvents(new EntityListener(this), this);
		}
		if (getConfig().getBoolean("properties.active-inspections")
				|| getConfig().getBoolean("properties.check-chunk-load")) {
			getServer().getPluginManager().registerEvents(new WorldListener(this), this);
		}

		// Disable if no listeners are enabled.
		if (!getConfig().getBoolean("properties.watch-creature-spawns")
				&& !getConfig().getBoolean("properties.active-inspections")
				&& !getConfig().getBoolean("properties.check-chunk-load")) {
			getLogger().severe("No listeners are enabled, the plugin will do nothing!");
			getLogger().severe("Enable creature spawn monitoring, active inspections, or chunk load inspections.");
			getServer().getPluginManager().disablePlugin(this);
		}

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
			if (!diskConfig.contains(property)) {
				getLogger().warning(property + " is missing from your config.yml, using default.");
			}
		}
	}

	public boolean checkChunk(Chunk chunk, Entity entity) {
		// Stop processing quickly if this world is excluded from limits.
		if (getConfig().getStringList("excluded-worlds").contains(chunk.getWorld().getName())) {
			return false;
		}

		Entity[] entities = chunk.getEntities();
		HashMap<String, ArrayList<Entity>> types = new HashMap<String, ArrayList<Entity>>();

		for (int i = entities.length - 1; i >= 0; i--) {

			String eType = entities[i].getType().name();
			String eGroup = getMobGroup(entities[i]);

			if (getConfig().contains("entities." + eType)) {
				if (!types.containsKey(eType)) {
					types.put(eType, new ArrayList<Entity>());
				}
				types.get(eType).add(entities[i]);
			}

			if (getConfig().contains("entities." + eGroup)) {
				if (!types.containsKey(eGroup)) {
					types.put(eGroup, new ArrayList<Entity>());
				}
				types.get(eGroup).add(entities[i]);
			}
		}

		if (entity != null) {

			String eType = entity.getType().name();

			if (getConfig().contains("entities." + eType)) {
				int typeCount;
				if (types.containsKey(eType)) {
					typeCount = types.get(eType).size() + 1;
				} else {
					typeCount = 1;
				}
				if (typeCount > getConfig().getInt("entities." + eType)) {
					return true;
				}
			}

			String eGroup = getMobGroup(entity);

			if (getConfig().contains("entities." + eGroup)) {
				int typeCount;
				if (types.containsKey(eGroup)) {
					typeCount = types.get(eGroup).size() + 1;
				} else {
					typeCount = 1;
				}
				return typeCount > getConfig().getInt("entities." + eGroup);
			}

		}

		for (Entry<String, ArrayList<Entity>> entry : types.entrySet()) {

			String eType = entry.getKey();
			int limit = getConfig().getInt("entities." + eType);

			if (entry.getValue().size() < limit) {
				continue;
			}

			debug("Removing " + (entry.getValue().size() - limit) + " " + eType + " @ "
					+ chunk.getX() + " " + chunk.getZ());

			if (getConfig().getBoolean("properties.notify-players")) {
				String notification = String.format(ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("messages.removedEntites")),
						entry.getValue().size() - limit, eType);
				for (int i = entities.length - 1; i >= 0; i--) {
					if (entities[i] instanceof Player) {
						Player p = (Player) entities[i];
						p.sendMessage(notification);
					}
				}
			}

			boolean skipNamed = getConfig().getBoolean("properties.preserve-named-entities");
			int toRemove = entry.getValue().size() - limit;
			int index = entry.getValue().size() - 1;
			while (toRemove > 0 && index >= 0) {
				Entity toCheck = entry.getValue().get(index);
				if (!skipNamed || toCheck.getCustomName() == null) {
					toCheck.remove();
					--toRemove;
				}
				--index;
			}
			if (toRemove == 0) {
				continue;
			}
			index = entry.getValue().size() - toRemove - 1;
			for (; index < entry.getValue().size(); index++) {
				// don't remove players
				if (entry.getValue().get(index) instanceof HumanEntity) {
					continue;
				}
				entry.getValue().get(index).remove();
			}
		}

		return false;
	}

	public void debug(String mess) {
		if (getConfig().getBoolean("properties.debug-messages")) {
			getLogger().info("[Debug] " + mess);
		}
	}

	public static String getMobGroup(Entity entity) {
		// Determine the general group this mob belongs to.
		if (entity instanceof Animals) {
			// Chicken, Cow, MushroomCow, Ocelot, Pig, Sheep, Wolf
			return "ANIMAL";
		}

		if (entity instanceof Monster) {
			// Blaze, CaveSpider, Creeper, Enderman, Giant, PigZombie, Silverfish, Skeleton, Spider,
			// Witch, Wither, Zombie
			return "MONSTER";
		}

		if (entity instanceof Ambient) {
			// Bat
			return "AMBIENT";
		}

		if (entity instanceof WaterMob) {
			// Squid
			return "WATER_MOB";
		}

		if (entity instanceof NPC) {
			// Villager
			return "NPC";
		}

		// Anything else.
		return "OTHER";
	}

}
