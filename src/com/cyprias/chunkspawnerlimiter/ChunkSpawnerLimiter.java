package com.cyprias.chunkspawnerlimiter;

import java.io.File;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkSpawnerLimiter extends JavaPlugin {
	public static File folder = new File("plugins/ChunkSpawnerLimiter");
	public static String chatPrefix = "§f[§aCSL§f] ";
	public Events events;
	public Config config;
	public VersionChecker versionChecker;
	
	private String stPluginEnabled = "§f%s §7v§f%s §7is enabled.";
	String pluginName;
	public void onEnable() {
		config = new Config(this);
		events = new Events(this);
		getServer().getPluginManager().registerEvents(events, this);
		pluginName = getDescription().getName();

		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {}
		
		this.versionChecker = new VersionChecker(this, "http://dev.bukkit.org/server-mods/chunkspawnerlimiter/files.rss");
		
		if (Config.checkNewVersionOnStartup == true)
			this.versionChecker.retreiveVersionInfo();
		
		info(String.format(stPluginEnabled, pluginName, this.getDescription().getVersion()));
	}

	public void info(String msg) {
		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
	}

}
