package com.cyprias.chunkspawnerlimiter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

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
		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
			EntityType eType = event.getEntityType();

			Chunk eChunk = event.getEntity().getLocation().getChunk();

			List<Entity> chunkEntities = new ArrayList<Entity>();
			chunkEntities.add(event.getEntity());

			Entity entity;
			List<Entity> entities = event.getEntity().getWorld().getEntities();
			for (int i = entities.size() - 1; i >= 0; i--) {
				entity = entities.get(i);
				if (entity.getType().equals(eType)) {
					if (entity.getLocation().getChunk().equals(eChunk)) {
						chunkEntities.add(entity);
					}
				}
			}

			if (chunkEntities.size() > plugin.config.maxmobperchunk) {
				CompareEntityAge comparator = new CompareEntityAge();
				Collections.sort(chunkEntities, comparator);
				for (int i = chunkEntities.size() - 1; i >= plugin.config.maxmobperchunk; i--) {
					entity = chunkEntities.get(i);
					entity.remove();
				}
			}
		}
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
