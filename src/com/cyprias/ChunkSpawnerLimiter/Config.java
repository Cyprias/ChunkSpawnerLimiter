package com.cyprias.ChunkSpawnerLimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

	public static boolean getBoolean(String property) {
		return Plugin.getInstance().getConfig().getBoolean(property);
	}

	public static int getInt(String property) {
		return Plugin.getInstance().getConfig().getInt(property);
	}

	public static String getString(String property) {
		return Plugin.getInstance().getConfig().getString(property);
	}

	public static String getString(String property, Object... args) {
		return String.format(Plugin.getInstance().getConfig().getString(property), args);
	}

	public static boolean contains(String property) {
		return Plugin.getInstance().getConfig().contains(property);
	}

	public static List<String> getStringList(String property) {
		return Plugin.getInstance().getConfig().getStringList(property);
	}

	public static void checkForMissingProperties() throws IOException, InvalidConfigurationException {
		File configFile = new File(Plugin.getInstance().getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			throw new FileNotFoundException(configFile.getPath() + " does not exist!");
		}
		YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(new File(Plugin.getInstance().getDataFolder(), "config.yml"));
		BufferedReader buffered = new BufferedReader(new InputStreamReader(Plugin.getInstance().getResource("config.yml")));
		YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(buffered);
		buffered.close();

		for (String property : defaultConfig.getKeys(true)) {
			if (!diskConfig.contains(property))
				Logger.warning(Plugin.chatPrefix + property + " is missing from your config.yml, using default.");
		}
	}

}
