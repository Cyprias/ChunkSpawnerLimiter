package com.cyprias.chunkspawnerlimiter;

import org.bukkit.configuration.Configuration;


public class Config {
	private ChunkSpawnerLimiter plugin;
	private static Configuration config;
	
	static int totalMobTypePerChunk, totalMobsPerChunk, surroundingRadius;
	static Boolean checkSurroundingChunks, onlyLimitSpawners;
	
	public Config(ChunkSpawnerLimiter plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		totalMobTypePerChunk = config.getInt("totalMobTypePerChunk");
		totalMobsPerChunk = config.getInt("totalMobsPerChunk");
		checkSurroundingChunks = config.getBoolean("checkSurroundingChunks");
		surroundingRadius = config.getInt("surroundingRadius");
		onlyLimitSpawners = config.getBoolean("onlyLimitSpawners"); 
	}
}
