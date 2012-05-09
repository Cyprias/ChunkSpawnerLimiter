package com.cyprias.chunkspawnerlimiter;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

public class ChunkSpawnerLimiter extends JavaPlugin {
	public static File folder = new File("plugins/ChunkSpawnerLimiter");
	public static String chatPrefix = "§f[§aCSL§f] ";
	public Events events;
	public Config config;

	private String stPluginEnabled = "§f%s §7v§f%s §7is enabled.";

	public void onEnable() {
		config = new Config(this);
		events = new Events(this);
		getServer().getPluginManager().registerEvents(events, this);
		info(String.format(stPluginEnabled, this.getDescription().getName(), this.getDescription().getVersion()));
	}

	public void info(String msg) {
		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
	}

}
