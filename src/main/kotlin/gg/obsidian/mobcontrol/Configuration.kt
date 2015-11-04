package gg.obsidian.mobcontrol

import org.bukkit.entity.EntityType
import java.util.*

class Configuration(val plugin: MobControl) {

    // Configuration Options
    var MOB_LIMITS: MutableMap<String, Int> = HashMap()
    var SPAWN_LIMITED_ENTITY_TYPES = HashSet<EntityType>()

    var AGE_CAP_BABY = -1
    var AGE_CAP_BREED = -1

    var LIMIT_NATURAL_SPAWN: Boolean = false
    var LIMIT_SPAWNER_SPAWN: Boolean = false
    var BUFF_DROPS: Int = 0
    var BUFF_SHEAR_DROPS: Int = 0
    var BUFF_DISABLED_ITEMS: List<Int> = ArrayList<Int>()

    var DEBUG: Boolean = false

    fun load() {
        plugin.reloadConfig()

        for (entry in plugin.getConfig().getConfigurationSection("limits").getValues(false).entries) {
            if (entry.key != null && entry.value != null && entry.value is Int) {
                val name = entry.key.toLowerCase()
                val value = entry.value as Int

                if (value >= 0) {
                    MOB_LIMITS.put(name, value)
                    if (DEBUG) {
                        plugin.logger.info(name + " limit: " + value)
                    }
                }
            }
        }

        if (plugin.getConfig().isList("settings.spawn-limited-entities")) {
            for (entityTypeName in plugin.getConfig().getStringList("settings.spawn-limited-entites")) {
                val entityType = EntityType.valueOf(entityTypeName.toUpperCase())
                if (entityType == null) {
                    plugin.logger.severe("Invalid entity type for spawn limiting: " + entityTypeName)
                } else {
                    SPAWN_LIMITED_ENTITY_TYPES.add(entityType)
                }
            }
        }

        AGE_CAP_BABY = plugin.getConfig().getInt("age-cap.baby", -1)
        AGE_CAP_BREED = plugin.getConfig().getInt("age-cap.breed", -1)

        LIMIT_NATURAL_SPAWN = plugin.getConfig().getBoolean("settings.limit-natural-spawn")
        LIMIT_SPAWNER_SPAWN = plugin.getConfig().getBoolean("settings.limit-spawner-spawn")
        BUFF_DROPS = plugin.getConfig().getInt("buff.drops")
        BUFF_SHEAR_DROPS = plugin.getConfig().getInt("buff.shear-drops")
        BUFF_DISABLED_ITEMS = plugin.getConfig().getIntegerList("buff.disabled-items")

        DEBUG = plugin.getConfig().getBoolean("settings.debug")
    }
}
