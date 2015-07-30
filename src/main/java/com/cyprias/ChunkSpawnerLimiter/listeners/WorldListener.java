package com.cyprias.ChunkSpawnerLimiter.listeners;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.cyprias.ChunkSpawnerLimiter.ChunkSpawnerLimiterPlugin;

public class WorldListener implements Listener {

	private final ChunkSpawnerLimiterPlugin plugin;
	private final HashMap<Chunk, BukkitTask> chunkTasks;

	public WorldListener(ChunkSpawnerLimiterPlugin plugin) {
		this.plugin = plugin;
		this.chunkTasks = new HashMap<Chunk, BukkitTask>();
	}

	private class InspectTask extends BukkitRunnable {
		private final Chunk chunk;

		public InspectTask(Chunk chunk) {
			this.chunk = chunk;
		}

		@Override
		public void run() {
			plugin.debug("Active check " + chunk.getX() + " " + chunk.getZ());
			if (!chunk.isLoaded()) {
				chunkTasks.remove(chunk);
				this.cancel();
				return;
			}
			plugin.checkChunk(chunk, null);
		}
	}

	@EventHandler
	public void onChunkLoadEvent(final ChunkLoadEvent event) {
		plugin.debug("ChunkLoadEvent " + event.getChunk().getX() + " " + event.getChunk().getZ());
		if (plugin.getConfig().getBoolean("properties.active-inspections")) {
			BukkitTask task = new InspectTask(event.getChunk()).runTaskTimer(plugin, 0,
					plugin.getConfig().getInt("properties.inspection-frequency") * 20L);

			chunkTasks.put(event.getChunk(), task);
		}

		if (plugin.getConfig().getBoolean("properties.check-chunk-load")) {
			plugin.checkChunk(event.getChunk(), null);
		}
	}

	@EventHandler
	public void onChunkUnloadEvent(final ChunkUnloadEvent event) {
		plugin.debug("ChunkUnloadEvent " + event.getChunk().getX() + " " + event.getChunk().getZ());

		if (chunkTasks.containsKey(event.getChunk())) {
			chunkTasks.remove(event.getChunk()).cancel();
		}

		if (plugin.getConfig().getBoolean("properties.check-chunk-unload")) {
			plugin.checkChunk(event.getChunk(), null);
		}
	}

}
