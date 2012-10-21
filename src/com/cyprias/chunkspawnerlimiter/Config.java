package com.cyprias.chunkspawnerlimiter;

import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;


public class Config {
	private ChunkSpawnerLimiter plugin;
	private static Configuration config;
	
	static int surroundingRadius;
	static Boolean checkSurroundingChunks, onlyLimitSpawners, notifyOpsOfNewVersion, debuggingMode;
	static  List<String> excludedWorlds;
	
	public static class mobInfo {
		int totalPerChunk;
	//	boolean removeOldest;

		public mobInfo(int totalPerChunk) {
			this.totalPerChunk = totalPerChunk;
			//this.removeOldest = removeOldest;
		}
	}
	static public HashMap<String, mobInfo> watchedMobs = new HashMap<String, mobInfo>();
	public Config(ChunkSpawnerLimiter plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		checkSurroundingChunks = config.getBoolean("checkSurroundingChunks");
		surroundingRadius = config.getInt("surroundingRadius");
		onlyLimitSpawners = config.getBoolean("onlyLimitSpawners"); 
		
		notifyOpsOfNewVersion= config.getBoolean("notifyOpsOfNewVersion");
		
		excludedWorlds = config.getStringList("excludedWorlds");
		
		debuggingMode = config.getBoolean("debuggingMode");
		
		
		String value;
		ConfigurationSection info;
		for (String mob : config.getConfigurationSection("mobs").getKeys(false)) {
			watchedMobs.put(
				mob, 
				new mobInfo(config.getConfigurationSection("mobs").getConfigurationSection(mob).getInt("totalPerChunk"))
			);
		}
	}
}
