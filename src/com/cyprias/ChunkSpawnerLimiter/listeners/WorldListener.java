package com.cyprias.ChunkSpawnerLimiter.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.cyprias.ChunkSpawnerLimiter.ChatUtils;
import com.cyprias.ChunkSpawnerLimiter.Config;
import com.cyprias.ChunkSpawnerLimiter.Logger;
import com.cyprias.ChunkSpawnerLimiter.Plugin;
import com.cyprias.ChunkSpawnerLimiter.compare.MobGroupCompare;

public class WorldListener implements Listener {
	HashMap<Chunk, Integer> chunkTasks = new HashMap<Chunk, Integer>();

	class inspectTask extends BukkitRunnable {
		Chunk c;

		public inspectTask(Chunk c) {
			this.c = c;
		}

		@Override
		public void run() {
			Logger.debug("Active check " + c.getX() + " " + c.getZ());
			if (!c.isLoaded()) {
				Plugin.cancelTask(taskID);
				return;
			}
			CheckChunk(c);
		}

		int taskID;
		public void setId(int taskID) {
			this.taskID = taskID;
		}
	}

	@EventHandler
	public void onChunkLoadEvent(final ChunkLoadEvent e) {
		Logger.debug("ChunkLoadEvent " + e.getChunk().getX() + " " + e.getChunk().getZ());
		if (Config.getBoolean("properties.active-inspections")) {
			inspectTask task = new inspectTask(e.getChunk());
			int taskID = Plugin.scheduleSyncRepeatingTask(task,
					Config.getInt("properties.inspection-frequency") * 20L);
			task.setId(taskID);

			chunkTasks.put(e.getChunk(), taskID);
		}

		if (Config.getBoolean("properties.check-chunk-load")) {
			CheckChunk(e.getChunk());
		}
	}

	@EventHandler
	public void onChunkUnloadEvent(final ChunkUnloadEvent e) {
		Logger.debug("ChunkUnloadEvent " + e.getChunk().getX() + " " + e.getChunk().getZ());

		if (chunkTasks.containsKey(e.getChunk())) {
			Plugin.getInstance().getServer().getScheduler()
					.cancelTask(chunkTasks.get(e.getChunk()));
			chunkTasks.remove(e.getChunk());
		}

		if (Config.getBoolean("properties.check-chunk-unload"))
			CheckChunk(e.getChunk());
	}

	public static void CheckChunk(Chunk c) {
		// Stop processing quickly if this world is excluded from limits.
		if (Config.getStringList("excluded-worlds").contains(c.getWorld().getName())) {
			return;
		}

		Entity[] ents = c.getEntities();

		HashMap<String, ArrayList<Entity>> types = new HashMap<String, ArrayList<Entity>>();

		for (int i = ents.length - 1; i >= 0; i--) {
			// ents[i].getType();
			EntityType t = ents[i].getType();

			String eType = t.toString();
			String eGroup = MobGroupCompare.getMobGroup(ents[i]);

			if (Config.contains("entities." + eType)) {
				if (!types.containsKey(eType))
					types.put(eType, new ArrayList<Entity>());
				types.get(eType).add(ents[i]);
			}

			if (Config.contains("entities." + eGroup)) {
				if (!types.containsKey(eGroup))
					types.put(eGroup, new ArrayList<Entity>());
				types.get(eGroup).add(ents[i]);
			}
		}

		for (Entry<String, ArrayList<Entity>> entry : types.entrySet()) {
			String eType = entry.getKey();
			int limit = Config.getInt("entities." + eType);

			if (entry.getValue().size() > limit) {
				Logger.debug("Removing " + (entry.getValue().size() - limit) + " " + eType + " @ "
						+ c.getX() + " " + c.getZ());

				if (Config.getBoolean("properties.notify-players")) {
					for (int i = ents.length - 1; i >= 0; i--) {
						if (ents[i] instanceof Player) {
							Player p = (Player) ents[i];
							ChatUtils.send(p, Config.getString("messages.removedEntites",
									entry.getValue().size() - limit, eType));
						}
					}
				}

				for (int i = entry.getValue().size() - 1; i >= limit; i--) {
					entry.getValue().get(i).remove();
				}
			}
		}
	}

}
