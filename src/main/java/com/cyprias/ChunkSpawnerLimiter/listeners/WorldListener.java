package com.cyprias.ChunkSpawnerLimiter.listeners;

import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.cyprias.ChunkSpawnerLimiter.Plugin;

public class WorldListener implements Listener {

	private final Plugin plugin;
	private final HashMap<Chunk, BukkitTask> chunkTasks;

	public WorldListener(Plugin plugin) {
		this.plugin = plugin;
		this.chunkTasks = new HashMap<>();
	}

	private class InspectTask extends BukkitRunnable {
		private final Chunk c;

		public InspectTask(Chunk c) {
			this.c = c;
		}

		@Override
		public void run() {
			plugin.debug("Active check " + c.getX() + " " + c.getZ());
			if (!c.isLoaded()) {
				chunkTasks.remove(c);
				this.cancel();
				return;
			}
			plugin.checkChunk(c);
		}
	}

	@EventHandler
	public void onChunkLoadEvent(final ChunkLoadEvent e) {
		plugin.debug("ChunkLoadEvent " + e.getChunk().getX() + " " + e.getChunk().getZ());
		if (plugin.getConfig().getBoolean("properties.active-inspections")) {
			BukkitTask task = new InspectTask(e.getChunk()).runTaskTimer(plugin, 0,
					plugin.getConfig().getInt("properties.inspection-frequency") * 20L);

			chunkTasks.put(e.getChunk(), task);
		}

		if (plugin.getConfig().getBoolean("properties.check-chunk-load")) {
			plugin.checkChunk(e.getChunk());
		}
	}

	@EventHandler
	public void onChunkUnloadEvent(final ChunkUnloadEvent e) {
		plugin.debug("ChunkUnloadEvent " + e.getChunk().getX() + " " + e.getChunk().getZ());

		if (chunkTasks.containsKey(e.getChunk())) {
			chunkTasks.remove(e).cancel();
		}

		if (plugin.getConfig().getBoolean("properties.check-chunk-unload")) {
			plugin.checkChunk(e.getChunk());
		}
	}

}
