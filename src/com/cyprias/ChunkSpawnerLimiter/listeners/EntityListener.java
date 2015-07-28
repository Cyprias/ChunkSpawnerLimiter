package com.cyprias.ChunkSpawnerLimiter.listeners;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.cyprias.ChunkSpawnerLimiter.Plugin;

public class EntityListener implements Listener {

	private final Plugin plugin;

	public EntityListener(Plugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawnEvent(CreatureSpawnEvent e) {

		if (plugin.getConfig().getBoolean("properties.watch-creature-spawns") == false) {
			return;
		}

		String reason = e.getSpawnReason().toString();
		
		if (!plugin.getConfig().getBoolean("spawn-reasons." + reason)
				|| plugin.getConfig().getBoolean("spawn-reasons." + reason) == false) {
			plugin.debug("Ignoring " + e.getEntity().getType().toString() + " due to spawnreason " + reason);
			return;
		}

		Chunk c = e.getLocation().getChunk();

		plugin.checkChunk(c);

		int surrounding = plugin.getConfig().getInt("properties.check-surrounding-chunks");

		if (surrounding > 0) {
			World w = e.getLocation().getWorld();
			for (int x = c.getX() + surrounding; x >= (c.getX() - surrounding); x--) {
				for (int z = c.getZ() + surrounding; z >= (c.getZ() - surrounding); z--) {
					// Logger.debug("Checking chunk " + x + " " +z);
					plugin.checkChunk(w.getChunkAt(x, z));
				}
			}
		}
	}

}
