package gg.obsidian.mobcontrol;

import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Configuration {

	public final MobControl plugin;

	// Configuration Options
	public Map<String, Integer> MOB_LIMITS = new HashMap<String, Integer>();
	public HashSet<EntityType> SPAWN_LIMITED_ENTITY_TYPES = new HashSet<EntityType>();

	public int AGE_CAP_BABY = -1;
	public int AGE_CAP_BREED = -1;

	public boolean LIMIT_NATURAL_SPAWN;
	public boolean LIMIT_SPAWNER_SPAWN;
	public int BUFF_DROPS;
	public int BUFF_SHEAR_DROPS;
	public List<Integer> BUFF_DISABLED_ITEMS;

	public boolean DEBUG;


	public Configuration(MobControl instance) {
		plugin = instance;
	}

	public void load() {
		plugin.reloadConfig();

		for (Map.Entry<String, Object> entry : plugin.getConfig().getConfigurationSection("limits").getValues(false).entrySet()) {
			if (entry.getKey() != null && entry.getValue() != null && entry.getValue() instanceof Integer) {
				String name = entry.getKey().toLowerCase();
				int value = (Integer) entry.getValue();

				if (value >= 0) {
					MOB_LIMITS.put(name, value);
					if (DEBUG) {
						plugin.getLogger().info(name + " limit: " + value);
					}
				}
			}
		}

		if (plugin.getConfig().isList("settings.spawn-limited-entities")) {
			for (String entityTypeName : plugin.getConfig().getStringList("settings.spawn-limited-entites")) {
				EntityType entityType = EntityType.valueOf(entityTypeName.toUpperCase());
				if (entityType == null) {
					plugin.getLogger().severe("Invalid entity type for spawn limiting: " + entityTypeName);
				} else {
					SPAWN_LIMITED_ENTITY_TYPES.add(entityType);
				}
			}
		}

		AGE_CAP_BABY = plugin.getConfig().getInt("age-cap.baby", -1);
		AGE_CAP_BREED = plugin.getConfig().getInt("age-cap.breed", -1);

		LIMIT_NATURAL_SPAWN = plugin.getConfig().getBoolean("settings.limit-natural-spawn");
		LIMIT_SPAWNER_SPAWN = plugin.getConfig().getBoolean("settings.limit-spawner-spawn");
		BUFF_DROPS = plugin.getConfig().getInt("buff.drops");
		BUFF_SHEAR_DROPS = plugin.getConfig().getInt("buff.shear-drops");
		BUFF_DISABLED_ITEMS = plugin.getConfig().getIntegerList("buff.disabled-items");

		DEBUG = plugin.getConfig().getBoolean("settings.debug");
	}
}
