package gg.obsidian.mobcontrol;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MobManager {
	public final MobControl plugin;

	public MobManager(MobControl instance) {
		plugin = instance;
	}

	public boolean hasCap(Entity entity) {
		return getCap(getCapKey(entity)) >= 0;
	}

	public int getCap(String key) {
		Integer cap = plugin.config.MOB_LIMITS.get(key);
		return (cap != null) ? cap : -1;
	}

	public String getCapKey(Entity entity) {
		StringBuilder key = new StringBuilder(entity.getType().name());

		if (entity instanceof Sheep) {
			key.append(((Sheep) entity).getColor().name());
		}

		return key.toString().toLowerCase();
	}

	public String getMobDescription(Entity entity) {
		String key = getCapKey(entity);
		Location loc = entity.getLocation();
		return String.format("%s at (%s,%d,%d,%d)", key,
			loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public boolean isSpecialMob(LivingEntity entity) {
		if (entity.getCustomName() != null) {
			return true;
		}

		if (entity instanceof Tameable) {
			Tameable tameable = (Tameable) entity;
			if (tameable.isTamed()) {
				return true;
			}
		}

		if (entity.getType() == EntityType.GUARDIAN && ((Guardian) entity).isElder()) {
			return true;
		}

		EntityEquipment equipment = entity.getEquipment();
		for (ItemStack armor : equipment.getArmorContents()) {
			if (armor != null && armor.getType() != Material.AIR) {
				return true;
			}
		}

		return false;
	}

	public void removeFromChunk(Chunk chunk) {
		Map<String, Integer> count = new HashMap<String, Integer>();

		for (Entity entity : chunk.getEntities()) {
			if (entity.isDead() || !(entity instanceof Animals || entity instanceof Monster)) {
				continue;
			}

			// Do not remove special mobs
			boolean special = isSpecialMob((LivingEntity) entity);
			if (special) {
				if (plugin.config.DEBUG) {
					plugin.getLogger().info("Special mob exempted from removal: " + getMobDescription(entity));
				}
				continue;
			}

			String key = getCapKey(entity);

			Integer oldCount = count.get(key);
			int mobCount = (oldCount == null) ? 1 : oldCount + 1;
			count.put(key, mobCount);

			int cap = getCap(key);
			if (cap >= 0 && mobCount > cap) {
				entity.remove();
				if (plugin.config.DEBUG) {
					plugin.getLogger().info("Removing mob " + getMobDescription(entity));
				}
			}
		}
	}

	public void removeFromAllChunks() {
		for (Chunk c : plugin.getServer().getWorlds().get(0).getLoadedChunks()) {
			removeFromChunk(c);
		}
	}

	public void applyAgeCap(Animals entity) {
		if (entity.getAgeLock() == true) {
			if (plugin.config.DEBUG) {
				plugin.getLogger().info("Age locked " + getMobDescription(entity) + " with age " + entity.getAge());
			}
			return;
		}

		if (plugin.config.AGE_CAP_BABY >= 0 && !entity.isAdult()) {
			entity.setAge(Math.max(entity.getAge(), -plugin.config.AGE_CAP_BABY));
		} else if (plugin.config.AGE_CAP_BREED >= 0 && entity.isAdult()) {
			entity.setAge(Math.min(entity.getAge(), plugin.config.AGE_CAP_BREED));
		}

		if (plugin.config.DEBUG) {
			plugin.getLogger().info("Age of " + getMobDescription(entity) + " capped to " + entity.getAge());
		}
	}

	public void limitCreatureSpawn(final CreatureSpawnEvent event) {
		CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

		if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING ||
			reason == CreatureSpawnEvent.SpawnReason.EGG ||
			reason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) &&
			isFarmAnimal(event.getEntity())) {

			applyAgeCap((Animals) event.getEntity());

			for (Entity entity : event.getEntity().getNearbyEntities(4, 4, 4)) {
				if (isFarmAnimal(entity)) {
					applyAgeCap((Animals) entity);
				}
			}

			return;
		}

		boolean shouldLimitNaturalSpawn = plugin.config.LIMIT_NATURAL_SPAWN &&
			(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING ||
				event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
				event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT);
		boolean shouldLimitSpawnerSpawn = plugin.config.LIMIT_SPAWNER_SPAWN &&
			event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER;

		if (!shouldLimitNaturalSpawn &&
			!shouldLimitSpawnerSpawn) {
			return;
		}

		if (!plugin.config.SPAWN_LIMITED_ENTITY_TYPES.contains(event.getEntityType()) ||
			!hasCap(event.getEntity())) {
			return;
		}

		int cap = getCap((getCapKey(event.getEntity())));
		int count = 0;

		for (Entity otherEntity : event.getLocation().getChunk().getEntities()) {
			if (otherEntity.getType() == event.getEntityType()) {
				count++;
				if (count >= cap) {
					break;
				}
			}
		}

		if (count >= cap) {
			if (plugin.config.DEBUG) {
				plugin.getLogger().info("Cancel spawn of " + getMobDescription((event.getEntity())) +
					" (reason = " + event.getSpawnReason().name().toLowerCase() + ", cap = " + cap + ")");
			}
			event.getEntity().remove();
		}
	}

	private boolean isFarmAnimal(Entity entity) {
		return (entity instanceof Animals) && !(entity instanceof Tameable);
	}
}
