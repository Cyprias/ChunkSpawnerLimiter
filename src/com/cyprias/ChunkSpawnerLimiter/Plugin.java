package com.cyprias.ChunkSpawnerLimiter;

import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import org.mcstats.Metrics;

import com.cyprias.ChunkSpawnerLimiter.listeners.EntityListener;
import com.cyprias.ChunkSpawnerLimiter.listeners.WorldListener;

public class Plugin extends JavaPlugin {

	private static Plugin instance = null;
	public static String chatPrefix = "&4[&bCSL&4]&r ";

	@Override
	public void onEnable() {
		instance = this;

		// Save default config if it does not exist.
		saveDefaultConfig();

		// Check if the config on disk is missing any settings, tell console if so.
		try {
			Config.checkForMissingProperties();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

		// Register our event listener.
		getServer().getPluginManager().registerEvents(new EntityListener(), this);
		getServer().getPluginManager().registerEvents(new WorldListener(), this);

		// Start the Metrics.
		if (Config.getBoolean("properties.use-metrics")) {
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

	public static final Plugin getInstance() {
		return instance;
	}

	public static int scheduleSyncRepeatingTask(Runnable run, long delay) {
		return scheduleSyncRepeatingTask(run, delay, delay);
	}

	public static int scheduleSyncRepeatingTask(Runnable run, long start, long delay) {
		return instance.getServer().getScheduler()
				.scheduleSyncRepeatingTask(instance, run, start, delay);
	}

	public static void cancelTask(int taskID) {
		instance.getServer().getScheduler().cancelTask(taskID);
	}

}
