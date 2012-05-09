package com.cyprias.chunkspawnerlimiter;

import java.io.File;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkSpawnerLimiter extends JavaPlugin {
	public static File folder = new File("plugins/ChunkSpawnerLimiter");
	public static String chatPrefix = "§f[§aCSL§f] ";
	public String name;
	public String version;
	public static Server server;
	public Events events;
	public Config config;

	private String stPluginEnabled = chatPrefix + "§f%s §7v§f%s §7is enabled.";

	public void onEnable() {
		server = getServer();

		config = new Config(this);

		events = new Events(this);
		server.getPluginManager().registerEvents(events, this);

		info(String.format(stPluginEnabled, name, version));
	}

	public void info(String msg) {
		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
	}

}
