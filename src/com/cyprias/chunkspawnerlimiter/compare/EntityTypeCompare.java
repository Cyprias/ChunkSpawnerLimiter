package com.cyprias.chunkspawnerlimiter.compare;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class EntityTypeCompare implements EntityCompare {
        private EntityType entityType;

        public EntityTypeCompare(EntityType entityType)
        {
                this.entityType = entityType;
        }

        @Override
        public boolean isSimilar(Entity entity) {
                return entity.getType().equals(this.entityType);
        }
}
