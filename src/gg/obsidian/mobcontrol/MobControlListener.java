package gg.obsidian.mobcontrol;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MobControlListener implements Listener {

	private final MobControl plugin;

	public MobControlListener(MobControl instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onChunkUnload(final ChunkUnloadEvent e) {
		plugin.mobs.removeFromChunk(e.getChunk());
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onCreatureSpawnEvent(final CreatureSpawnEvent event) {
		plugin.mobs.limitCreatureSpawn(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		if (plugin.config.BUFF_DROPS <= 1 || !(event.getEntity() instanceof Ageable)) {
			return;
		}

		List<ItemStack> items = event.getDrops();
		Location l = event.getEntity().getLocation();

		for (ItemStack a : items) {
			if (plugin.config.BUFF_DISABLED_ITEMS.contains(a.getTypeId())) {
				continue;
			}

			for (int i = 1; i < plugin.config.BUFF_DROPS; i++) {
				l.getWorld().dropItemNaturally(l, a);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerShearEvent(EntityDeathEvent event) {
		if (plugin.config.BUFF_SHEAR_DROPS <= 1 || !(event.getEntity() instanceof Sheep)) {
			return;
		}

		Sheep entity = (Sheep) event.getEntity();
		Location l = entity.getLocation();

		int count = (1 + (int) (3 * Math.random())) * (plugin.config.BUFF_SHEAR_DROPS - 1);
		l.getWorld().dropItemNaturally(l, new ItemStack(Material.WOOL, count, (byte) entity.getColor().ordinal()));
	}
}
