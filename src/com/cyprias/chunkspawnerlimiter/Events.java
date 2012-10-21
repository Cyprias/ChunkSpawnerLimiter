package com.cyprias.chunkspawnerlimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

public class Events implements Listener {
	private ChunkSpawnerLimiter plugin;

	public Events(ChunkSpawnerLimiter plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
		if (event.isCancelled()) {
			return;
		}

		// MobsGoneWild

		if (Config.excludedWorlds.contains(event.getLocation().getWorld().getName()))
			return;

		if (Config.onlyLimitSpawners == false || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			EntityType eType = event.getEntityType();

			//if (Config.debuggingMode == true) {
			//	plugin.info("CreatureSpawnEvent eType: " + eType.toString());
			//	plugin.info("watchedMobs: " + Config.watchedMobs.containsKey(eType.toString()));
			//}
			
			if (Config.watchedMobs.containsKey(eType.toString()) == false)
				return;

			Chunk eChunk = event.getEntity().getLocation().getChunk();
			checkChunk(event.getEntity(), eChunk, eType);
			checkedChunks.clear();
		}
	}

	HashMap<Chunk, Boolean> checkedChunks = new HashMap<Chunk, Boolean>();

	public boolean checkChunk(Entity spawnedEntity, Chunk chunk, EntityType eType, Integer loop) {
		if (checkedChunks.containsKey(chunk)) {
			//	plugin.info("checkChunk already checked. " + chunk);
			return false;
		}
		checkedChunks.put(chunk, true);

		// List<Entity> chunkTotalEntities = getChunkMobs(chunk);
		List<Entity> chunkEntities = getChunkMobs(chunk, eType);

		//	plugin.info("checkChunk " + chunk + ", loop: " + loop);

		Entity entity;
		
		if ((chunkEntities.size()) >= Config.watchedMobs.get(eType.toString()).totalPerChunk) {
			CompareEntityAge comparator = new CompareEntityAge();
			Collections.sort(chunkEntities, comparator);

			//if (Config.debuggingMode == true) 
			//	plugin.info(eType+ " @ "+chunk + ", count: " + chunkEntities.size());
			
			
			for (int i = chunkEntities.size() - 1; (i+1) >= Config.watchedMobs.get(eType.toString()).totalPerChunk; i--) {
				if (Config.debuggingMode == true) 
					plugin.info("Removing #" + i + " " +eType+ ", age: "+ chunkEntities.get(i).getTicksLived() +" @ "+chunkEntities.get(i).getLocation().getChunk());
				
				chunkEntities.get(i).remove();
			}
		}

		if (Config.checkSurroundingChunks == true && loop < Config.surroundingRadius) {
			int x = spawnedEntity.getLocation().getBlockX();
			int z = spawnedEntity.getLocation().getBlockZ();

			int chunkSize = 16;

			Chunk north = (new Location(spawnedEntity.getWorld(), x, 1, z - chunkSize)).getChunk();
			Chunk east = (new Location(spawnedEntity.getWorld(), x - chunkSize, 1, z)).getChunk();
			Chunk south = (new Location(spawnedEntity.getWorld(), x, 1, z + chunkSize)).getChunk();
			Chunk west = (new Location(spawnedEntity.getWorld(), x + chunkSize, 1, z)).getChunk();

			checkChunk(spawnedEntity, north, eType, loop + 1);
			checkChunk(spawnedEntity, east, eType, loop + 1);
			checkChunk(spawnedEntity, south, eType, loop + 1);
			checkChunk(spawnedEntity, west, eType, loop + 1);

		}

		return false;
	}

	public boolean checkChunk(Entity spawnedEntity, Chunk chunk, EntityType eType) {
		return checkChunk(spawnedEntity, chunk, eType, 0);
	}

	public List<Entity> getChunkMobs(Chunk chunk, EntityType mob) {
		List<Entity> chunkEntities = new ArrayList<Entity>();

		/*
		 * List<Entity> entities = chunk.getWorld().getEntities(); Entity
		 * entity; for (int i = entities.size() - 1; i >= 0; i--) { entity =
		 * entities.get(i); if (entity.getLocation().getChunk().equals(chunk)) {
		 * if (entity.getType() == mob) { chunkEntities.add(entity); } } }
		 */

		Entity[] entities = chunk.getEntities();
		for (int i = entities.length - 1; i >= 0; i--) {
			if (entities[i].getType() == mob) {
				chunkEntities.add(entities[i]);
			}
		}

		return chunkEntities;
	}

	// public List<Entity> getChunkMobs(Chunk chunk) {
	// return getChunkMobs(chunk, null);
	// }

	public class CompareEntityAge implements Comparator<Entity> {
		@Override
		public int compare(Entity o1, Entity o2) {
			// TODO Auto-generated method stub
			double age1 = o1.getTicksLived();
			double age2 = o2.getTicksLived();

			if (age1 > age2) {
				return +1;
			} else if (age1 < age2) {
				return -1;
			} else {
				return 0;
			}
		}

	}

}
