package com.cyprias.chunkspawnerlimiter;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;


public class Config {
	private static Configuration config;

	static int surroundingRadius;
	static Boolean checkSurroundingChunks, onlyLimitSpawners, notifyOpsOfNewVersion, debuggingMode, checkNewVersionOnStartup, removeOldest;
	static  List<String> excludedWorlds;

	public static class mobInfo {
		int totalPerChunk;

		public mobInfo(int totalPerChunk) {
			this.totalPerChunk = totalPerChunk;
		}
	}

	static public HashMap<String, mobInfo> watchedMobs = new HashMap<String, mobInfo>();
	public Config(JavaPlugin plugin) {
		config = plugin.getConfig().getRoot();

		if (!(new File(plugin.getDataFolder(), "config.yml").exists())){
			config.options().copyDefaults(true);
			plugin.saveConfig();
		}

		checkSurroundingChunks = config.getBoolean("checkSurroundingChunks");
		surroundingRadius = config.getInt("surroundingRadius");
		onlyLimitSpawners = config.getBoolean("onlyLimitSpawners");

		notifyOpsOfNewVersion= config.getBoolean("notifyOpsOfNewVersion");

		excludedWorlds = config.getStringList("excludedWorlds");

		debuggingMode = config.getBoolean("debuggingMode");

		checkNewVersionOnStartup = config.getBoolean("checkNewVersionOnStartup");

        removeOldest = config.getBoolean("removeOldest", true);

		for (String mob : config.getConfigurationSection("mobs").getKeys(false)) {
			watchedMobs.put(
				mob,
				new mobInfo(config.getConfigurationSection("mobs").getConfigurationSection(mob).getInt("totalPerChunk"))
			);
		}
	}
}
