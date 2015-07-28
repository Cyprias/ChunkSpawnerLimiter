package com.cyprias.ChunkSpawnerLimiter;

import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;

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
		YML diskConfig = new YML(Plugin.getInstance().getDataFolder(), "config.yml");
		YML defaultConfig = new YML(Plugin.getInstance().getResource("config.yml"));

		for (String property : defaultConfig.getKeys(true)) {
			if (!diskConfig.contains(property))
				Logger.warning(Plugin.chatPrefix + property + " is missing from your config.yml, using default.");
		}
	}

}
