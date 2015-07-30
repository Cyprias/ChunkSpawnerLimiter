package com.cyprias.ChunkSpawnerLimiter.listeners;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.cyprias.ChunkSpawnerLimiter.ChunkSpawnerLimiterPlugin;

public class EntityListener implements Listener {

	private final ChunkSpawnerLimiterPlugin plugin;

	public EntityListener(ChunkSpawnerLimiterPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawnEvent(CreatureSpawnEvent e) {

		String reason = e.getSpawnReason().toString();

		if (!plugin.getConfig().getBoolean("spawn-reasons." + reason)
				|| !plugin.getConfig().getBoolean("spawn-reasons." + reason)) {
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
