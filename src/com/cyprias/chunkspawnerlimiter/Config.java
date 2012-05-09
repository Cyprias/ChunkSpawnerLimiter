package com.cyprias.chunkspawnerlimiter;

import org.bukkit.configuration.Configuration;


public class Config {
	private ChunkSpawnerLimiter plugin;
	private static Configuration config;
	
	int maxmobperchunk = 0;
	
	public Config(ChunkSpawnerLimiter plugin) {
		this.plugin = plugin;
		config = plugin.getConfig().getRoot();
		config.options().copyDefaults(true);
		plugin.saveConfig();
		maxmobperchunk = config.getInt("maxmobperchunk");
	}
}
