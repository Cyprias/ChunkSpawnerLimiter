package com.cyprias.chunkspawnerlimiter;

import java.util.*;

import com.cyprias.chunkspawnerlimiter.compare.EntityCompare;
import com.cyprias.chunkspawnerlimiter.compare.EntityTypeCompare;
import com.cyprias.chunkspawnerlimiter.compare.MobGroupCompare;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import com.cyprias.chunkspawnerlimiter.VersionChecker.VersionCheckerEvent;

public class Events implements Listener {

    // Inner class for sorting a collection of entities by age.
    public class CompareEntityAge implements Comparator<Entity> {
        @Override
        public int compare(Entity o1, Entity o2) {
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

    // Reference back to the actual plugin.
    private ChunkSpawnerLimiter plugin;

    // Used to make sure the same chunk is not checked multiple times during the same operation.
    private HashMap<Chunk, Boolean> checkedChunks = new HashMap<Chunk, Boolean>();

    // Constructor
	public Events(ChunkSpawnerLimiter plugin) {
		this.plugin = plugin;
	}

	@EventHandler
    // Handle creature spawns.
	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
		// If something else has already cancelled the event, then do nothing.
        if (event.isCancelled())
			return;

        // Capture the entity
        LivingEntity entity = event.getEntity();
		if (Config.debuggingMode == true){
			plugin.info("CreatureSpawnEvent eType: " + entity.getType().toString() + " " + event.getSpawnReason());
		}

        // Stop processing quickly if this spawn type is not valid.
        if (Config.onlyLimitSpawners == true && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        // Stop processing quickly if this world is excluded from limits.
		if (Config.excludedWorlds.contains(event.getLocation().getWorld().getName())) {
            return;
        }

        // Get string representation of the Mob type and mob group.
        String mobLabel = entity.getType().toString();
        String mobGroup = MobGroupCompare.getMobGroup(entity);

        // TODO: There would be some performance benefit to doing some of this work up front instead of on each event.
        //       An Explicit list of supported mobs and their group would need to be maintained, though.

        // Get the appropriate limit. The type of limit in use will dictate exactly how we will compare
        // existing entities against the spawned entity.
        int limit;
        EntityCompare entityComparator;

        // See if we have a configuration under the mob label.
        if (Config.watchedMobs.containsKey(mobLabel)) {
            // Record the limit.
            limit = Config.watchedMobs.get(mobLabel).totalPerChunk;

            // If we ALSO have a mob group limit, then we need to use the smaller of the two limits.
            if (Config.watchedMobs.containsKey(mobGroup) && Config.watchedMobs.get(mobGroup).totalPerChunk < limit) {
                // The mob group limit is lower, we will use that.
                limit = Config.watchedMobs.get(mobGroup).totalPerChunk;

                // We will make comparisons based on the mob group.
                entityComparator = new MobGroupCompare(mobGroup);
            }
            else {
                // Mob group limit doesn't exist or doesn't apply. Either way, we will be using the
                // actual entity type to for comparisons.
                entityComparator = new EntityTypeCompare(entity.getType());
            }

        }
        // No mob-specific limit. Check for a mob group limit.
        else if (Config.watchedMobs.containsKey(mobGroup)) {
            // Use the mob group limit and plan to compare based on mob group.
            limit = Config.watchedMobs.get(mobGroup).totalPerChunk;
            entityComparator = new MobGroupCompare(mobGroup);
        }
        else {
            // No relevant limit. Bail.
            return;
        }

//        plugin.info("limit: " + limit);

        // If our limit is configured to zero (or less), then we should automatically avoid the spawn.
		if (limit < 1) {
            // Cancel the spawn.
            event.setCancelled(true);
			return;
        }

        // There is a limit so we need to round up all the mobs in the configured area around the
        // spawn chunk.
        List<Entity> collectedMobs = new ArrayList<Entity>();

        collectedMobs = collectMobs(entity.getLocation().getChunk(),
            entityComparator,
            Config.checkSurroundingChunks ? Config.surroundingRadius : 0,
            (Config.removeOldest ? -1 : limit));

        // See if we are under the limit.
        if (collectedMobs.size() < limit) {
            // We are under the limit... okay to spawn.
//            plugin.info("Okay to spawn");
            return;
        }

        // We are NOT under the limit.  Decide how to handle the new spawn.
        if (Config.removeOldest) {
            // We are going to remove the oldest mob(s) to make room for the new one.

            // Sort all the mobs by age.
            CompareEntityAge comparator = new CompareEntityAge();
			Collections.sort(collectedMobs, comparator);

            // Remove all excess mobs.  The goal is to have one less than the limit so that there is
            // room for the new one.
            for (Entity oldMob: collectedMobs.subList(limit - 1, collectedMobs.size() - 1)) {
//                plugin.info("removed " + oldMob.toString());
                oldMob.remove();
            }
        }
        else {
            // Simply stop the spawn.
//            plugin.info("prevented spawn");
            event.setCancelled(true);
            return;
        }
	}

    /**
     * Finds all mobs in and around the specified center-chunk and returns them in a list.
     *
     * @param centerChunk      Chunk to start collecting mobs from.
     * @param entityComparator Only entities identified as "similar" by this will be collected.
     * @param maxRadius        The number of chunks outward from the centerChunk to look in when collecting Mobs.
     * @param haltAfter        Stop counting if/when this value is exceeded. A value of -1 will collect all mobs. Since mobs
     *                         are gathered per chunk, the number gathered may be larger than this value by an indeterminate amount.
     * @return
     */
    public List<Entity> collectMobs(Chunk centerChunk, EntityCompare entityComparator, int maxRadius, int haltAfter)
    {
        World world = centerChunk.getWorld();

        Integer minX, maxX, minZ, maxZ, currentX, currentZ;
        List<Entity> allMobs = new ArrayList<Entity>();

        // Check the center chunk.
        allMobs.addAll(getChunkMobs(world.getChunkAt(centerChunk.getX(), centerChunk.getZ()), entityComparator));
//        plugin.info("Checking center: " + centerChunk.getX() + " " + centerChunk.getZ());

        int currentRadius = 1;
        collectionLoop:
        while (maxRadius >= currentRadius && (haltAfter == -1 || allMobs.size() < haltAfter)) {

//            plugin.info("radius:" + currentRadius + "/" + maxRadius);

            // Upper left -- start here.
            currentX = minX = (centerChunk.getX() - currentRadius);
            currentZ = minZ = (centerChunk.getZ() - currentRadius);

            // Bottom right.
            maxX = centerChunk.getX() + currentRadius;
            maxZ = centerChunk.getZ() + currentRadius;

            // Top - increase X, not Y
            for (; currentX <= maxX && (haltAfter == -1 || allMobs.size() < haltAfter); currentX++) {
                // get mobs for chunk @ currentX,currentY
                allMobs.addAll(getChunkMobs(world.getChunkAt(currentX, currentZ), entityComparator));
//                plugin.info("checking:" + currentX + " " + currentZ);
            }

            // Right - increase Z, not X
            currentX = maxX;
            for (currentZ += 1; currentZ <= maxZ && (haltAfter == -1 || allMobs.size() < haltAfter); currentZ++) {
                // get mobs for chunk @ currentX,currentY
                allMobs.addAll(getChunkMobs(world.getChunkAt(currentX, currentZ), entityComparator));
//                plugin.info("checking:" + currentX + " " + currentZ + "(" + allMobs.size() + ")");
            }

            // Bottom - decrease X, not Z
            currentZ = maxZ;
            for (currentX -= 1; currentX >= minX && (haltAfter == -1 || allMobs.size() < haltAfter); currentX--) {
                // get mobs for chunk @ currentX,currentY
                allMobs.addAll(getChunkMobs(world.getChunkAt(currentX, currentZ), entityComparator));
//                plugin.info("checking:" + currentX + " " + currentZ + "(" + allMobs.size() + ")");
            }


            // Left - decrease Z, not X
            // Increase Z to prevent duplication of the other block.
            // Don't go all the way back to minZ -- we counted the chunk at the beginning.
            currentX = minX;
            for (currentZ -= 1; currentZ > minZ && (haltAfter == -1 || allMobs.size() < haltAfter); currentZ--) {
                // get mobs for chunk @ currentX,currentY
                allMobs.addAll(getChunkMobs(world.getChunkAt(currentX, currentZ), entityComparator));
//                plugin.info("checking:" + currentX + " " + currentZ + "(" + allMobs.size() + ")");
            }

            currentRadius++;
        }

//        plugin.info("matching mob count:" + allMobs.size());
        return allMobs;
    }

	public List<Entity> getChunkMobs(Chunk chunk, EntityCompare entityComparator) {
		List<Entity> chunkEntities = new ArrayList<Entity>();

		Entity[] entities = chunk.getEntities();
		for (int i = entities.length - 1; i >= 0; i--) {
            if (entityComparator.isSimilar(entities[i])) {
				chunkEntities.add(entities[i]);
			}
		}

		return chunkEntities;
	}


	@EventHandler(priority = EventPriority.NORMAL)
	public void onVersionCheckerEvent(VersionCheckerEvent event) {

		if (event.getPluginName() == plugin.getName()) {
			com.cyprias.chunkspawnerlimiter.VersionChecker.versionInfo info = event.getVersionInfo(0);
			Object[] args = event.getArgs();

			String curVersion = plugin.getDescription().getVersion();

			if (args.length == 0) {

				int compare = plugin.versionChecker.compareVersions(curVersion, info.getTitle());
				// plugin.info("curVersion: " + curVersion +", title: " +
				// info.getTitle() + ", compare: " + compare);
				if (compare < 0) {
					plugin.info("We're running v" + curVersion + ", v" + info.getTitle() + " is available");
					plugin.info(info.getLink());
				}

				return;
			}
		}
	}
}
