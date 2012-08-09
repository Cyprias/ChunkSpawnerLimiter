package com.cyprias.chunkspawnerlimiter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkSpawnerLimiter extends JavaPlugin {
	public static File folder = new File("plugins/ChunkSpawnerLimiter");
	public static String chatPrefix = "§f[§aCSL§f] ";
	public Events events;
	public Config config;

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
		queueVersionCheck(null);
		info(String.format(stPluginEnabled, pluginName, this.getDescription().getVersion()));
	}

	public void info(String msg) {
		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
	}

	List<Player> versionRequested = new ArrayList<Player>();
	public void queueVersionCheck(Player requester) {

		if (requester != null) {
			versionRequested.add(requester);
		}

		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
			public void run() {
				versionCheck();
			}
		}, 0L);// asap

	}

	public String getLatestVersion() {
		/* This function pulls the latest version from the dev.bukkit.org (Curse) website. 
			It's my belief this automated request doesn't violate the Curse Terms of Service (http://www.curse.com/terms). */

		String latestVersion = getDescription().getVersion();

		final String address = "http://dev.bukkit.org/server-mods/chunkspawnerlimiter/files/";
		final URL url;
		URLConnection connection = null;
		BufferedReader bufferedReader = null;
		try {
			url = new URL(address.replace(" ", "%20"));
			connection = url.openConnection();

			connection.setConnectTimeout(8000);
			connection.setReadTimeout(15000);
			connection.setRequestProperty("User-agent", pluginName + getDescription().getVersion());

			bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			// info("versionCheck1: " + bufferedReader.readLine());

			String str;
			Pattern titleFinder = Pattern.compile("<td[^>]*><a[^>]*>(.*?)</a></td>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher regexMatcher;
			while ((str = bufferedReader.readLine()) != null) {
				str = str.trim();

				
				regexMatcher = titleFinder.matcher(str);
				if (regexMatcher.find()) {
					// info("found match: "+regexMatcher.group(1));
					latestVersion = regexMatcher.group(1);
					break;
				}
			}

			bufferedReader.close();
			connection.getInputStream().close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return latestVersion;
	}
	
	public String latestVersion = null;
	public void versionCheck() {
		// "<td class=\"col-file\"><a href=\"/server-mods/monarchy/files/2-0-0-2/\">0.0.2</a></td>";

		if (latestVersion == null) {
			// double start = getUnixTime();
			latestVersion = getLatestVersion();
			// double end = getUnixTime();
			// info("Took " + (end - start) +
			// " seconds to get latest verison.");
		}

		String msg = null;
		String curVersion = getDescription().getVersion();
		if (latestVersion != null) {
			int compare = curVersion.compareTo(latestVersion);

			if (compare < 0) {
				msg = String.format("&7We're running version v%s&7, v%s&7 is available.", ChatColor.RED + curVersion, ChatColor.GREEN + latestVersion);
			}
		}

		if (msg != null){
			if (versionRequested.size() > 0) {
				for (int i = 0; i < versionRequested.size(); i++) {
					versionRequested.get(i).sendMessage(msg);
				}
				versionRequested.clear();
			} else {
				info(msg);
	
			}
		}
	}
	
}
