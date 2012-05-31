package com.cyprias.chunkspawnerlimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
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

		if (Config.onlyLimitSpawners == false || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			EntityType eType = event.getEntityType();
			Chunk eChunk = event.getEntity().getLocation().getChunk();
			checkChunk(event.getEntity(), eChunk, eType);
			checkedChunks.clear();
		}
	}

	HashMap<Chunk, Boolean> checkedChunks = new HashMap<Chunk, Boolean>();

	public boolean checkChunk(Entity spawnedEntity, Chunk chunk, EntityType eType, Integer loop) {
		if (checkedChunks.containsKey(chunk)) {
			// plugin.info("checkChunk already checked. " + chunk);
			return false;
		}
		checkedChunks.put(chunk, true);

		List<Entity> chunkTotalEntities = getChunkMobs(chunk);
		List<Entity> chunkEntities = getChunkMobs(chunk, eType);

		// plugin.info("checkChunk " + chunk + ", loop: " + loop);

		Entity entity;
		if (chunkEntities.size() > plugin.config.totalMobTypePerChunk) {
			CompareEntityAge comparator = new CompareEntityAge();
			Collections.sort(chunkEntities, comparator);
			for (int i = chunkEntities.size() - 1; i >= plugin.config.totalMobTypePerChunk; i--) {
				entity = chunkEntities.get(i);
				// plugin.info("Removing mob type. " + entity.getType());
				entity.remove();
			}
		}

		if (chunkTotalEntities.size() > plugin.config.totalMobsPerChunk) {
			CompareEntityAge comparator = new CompareEntityAge();
			Collections.sort(chunkTotalEntities, comparator);
			for (int i = chunkTotalEntities.size() - 1; i >= plugin.config.totalMobsPerChunk; i--) {
				entity = chunkTotalEntities.get(i);
				// plugin.info("Removing total mob." + entity.getType());
				entity.remove();
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
		List<Entity> entities = chunk.getWorld().getEntities();

		List<Entity> chunkEntities = new ArrayList<Entity>();

		Entity entity;
		for (int i = entities.size() - 1; i >= 0; i--) {
			entity = entities.get(i);

			if (entity.getLocation().getChunk().equals(chunk)) {
				if (mob == null) {
					if (entity.getType()  ==entity.getType().ZOMBIE || 
						entity.getType()  ==entity.getType().SKELETON || 
						entity.getType()  ==entity.getType().SPIDER || 
						entity.getType()  ==entity.getType().BLAZE || 
						entity.getType()  ==entity.getType().CAVE_SPIDER){
						chunkEntities.add(entity);
					}
				} else if (entity.getType() == mob) {

					chunkEntities.add(entity);
				}
			}
		}

		return chunkEntities;
	}

	public List<Entity> getChunkMobs(Chunk chunk) {
		return getChunkMobs(chunk, null);
	}

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
