package gg.obsidian.mobcontrol

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent

import java.util.HashMap

class MobManager(val plugin: MobControl) {

  fun hasCap(entity: Entity): Boolean {
    return getCap(getCapKey(entity)) >= 0
  }

  fun getCap(key: String): Int {
    val cap = plugin.config.MOB_LIMITS[key]
    return if ((cap != null)) cap else -1
  }

  fun getCapKey(entity: Entity): String {
    val key = StringBuilder(entity.type.name)

    if (entity is Sheep) {
      key.append(entity.color.name)
    }

    return key.toString().toLowerCase()
  }

  fun getMobDescription(entity: Entity): String {
    val key = getCapKey(entity)
    val loc = entity.location
    return "%s at (%s,%d,%d,%d)".format(key, loc.world.name, loc.blockX, loc.blockY, loc.blockZ)
  }

  fun isSpecialMob(entity: LivingEntity): Boolean {
    if (entity.customName != null) {
      return true
    }

    if (entity is Tameable) {
      if (entity.isTamed) {
        return true
      }
    }

    if (entity.type == EntityType.GUARDIAN && (entity as Guardian).isElder) {
      return true
    }

    val equipment = entity.equipment
    for (armor in equipment.armorContents) {
      if (armor != null && armor.type != Material.AIR) {
        return true
      }
    }

    return false
  }

  fun removeFromChunk(chunk: Chunk) {
    val count = HashMap<String, Int>()

    for (entity in chunk.entities) {
      if (entity.isDead || !(entity is Animals || entity is Monster)) {
        continue
      }

      // Do not remove special mobs
      val special = isSpecialMob(entity as LivingEntity)
      if (special) {
        if (plugin.config.DEBUG) {
          plugin.logger.info("Special mob exempted from removal: " + getMobDescription(entity))
        }
        continue
      }

      val key = getCapKey(entity)

      val oldCount = count[key]
      val mobCount = if ((oldCount == null)) 1 else oldCount + 1
      count.put(key, mobCount)

      val cap = getCap(key)
      if (cap >= 0 && mobCount > cap) {
        entity.remove()
        if (plugin.config.DEBUG) {
          plugin.logger.info("Removing mob " + getMobDescription(entity))
        }
      }
    }
  }

  fun removeFromAllChunks() {
    for (c in plugin.server.worlds[0].loadedChunks) {
      removeFromChunk(c)
    }
  }

  fun applyAgeCap(entity: Animals) {
    if (entity.ageLock == true) {
      if (plugin.config.DEBUG) {
        plugin.logger.info("Age locked " + getMobDescription(entity) + " with age " + entity.age)
      }
      return
    }

    if (plugin.config.AGE_CAP_BABY >= 0 && !entity.isAdult) {
      entity.age = Math.max(entity.age, -plugin.config.AGE_CAP_BABY)
    } else if (plugin.config.AGE_CAP_BREED >= 0 && entity.isAdult) {
      entity.age = Math.min(entity.age, plugin.config.AGE_CAP_BREED)
    }

    if (plugin.config.DEBUG) {
      plugin.logger.info("Age of " + getMobDescription(entity) + " capped to " + entity.age)
    }
  }

  fun limitCreatureSpawn(event: CreatureSpawnEvent) {
    val reason = event.spawnReason

    if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING || reason == CreatureSpawnEvent.SpawnReason.EGG || reason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) && isFarmAnimal(event.entity)) {

      applyAgeCap(event.entity as Animals)

      for (entity in event.entity.getNearbyEntities(4.0, 4.0, 4.0)) {
        if (isFarmAnimal(entity)) {
          applyAgeCap(entity as Animals)
        }
      }

      return
    }

    val shouldLimitNaturalSpawn = plugin.config.LIMIT_NATURAL_SPAWN && (event.spawnReason == CreatureSpawnEvent.SpawnReason.BREEDING || event.spawnReason == CreatureSpawnEvent.SpawnReason.NATURAL || event.spawnReason == CreatureSpawnEvent.SpawnReason.DEFAULT)
    val shouldLimitSpawnerSpawn = plugin.config.LIMIT_SPAWNER_SPAWN && event.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER

    if (!shouldLimitNaturalSpawn && !shouldLimitSpawnerSpawn) {
      return
    }

    if (!plugin.config.SPAWN_LIMITED_ENTITY_TYPES.contains(event.entityType) || !hasCap(event.entity)) {
      return
    }

    val cap = getCap((getCapKey(event.entity)))
    var count = 0

    for (otherEntity in event.location.chunk.entities) {
      if (otherEntity.type == event.entityType) {
        count++
        if (count >= cap) {
          break
        }
      }
    }

    if (count >= cap) {
      if (plugin.config.DEBUG) {
        plugin.logger.info("Cancel spawn of " + getMobDescription((event.entity)) + " (reason = " + event.spawnReason.name.toLowerCase() + ", cap = " + cap + ")")
      }
      event.entity.remove()
    }
  }

  private fun isFarmAnimal(entity: Entity): Boolean {
    return (entity is Animals) && entity !is Tameable
  }
}
