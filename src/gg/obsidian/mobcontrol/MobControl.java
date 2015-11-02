package gg.obsidian.mobcontrol;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;

public class MobControl extends JavaPlugin implements Listener {

	private MobControlListener listener = new MobControlListener(this);
	public final Configuration config = new Configuration(this);
	public final MobManager mobs = new MobManager(this);

	public void onEnable() {
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			getConfig().options().copyDefaults(true);
			saveConfig();
		}

		config.load();

		getServer().getPluginManager().registerEvents(listener, this);
	}

	public void onDisable() {
		mobs.removeFromAllChunks();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase("mobcontrol")) {
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("cull") && sender.hasPermission("mobcontrol.cull")) {
			sender.sendMessage("[MobControl] Culling mobs in all loaded chunks");
			mobs.removeFromAllChunks();
			return true;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("check") && sender.hasPermission("mobcontrol.check") && sender instanceof Player) {
			LivingEntity living = (LivingEntity) getTarget((Player) sender);
			if (living == null) {
				sender.sendMessage("[MobLimiter] No mob in sight");
				return true;
			}

			boolean animalOrMonster = (living instanceof Animals) || (living instanceof Monster);
			if (!mobs.isSpecialMob(living) && animalOrMonster) {
				sender.sendMessage("[MobLimiter] Mob will be culled");
			} else {
				sender.sendMessage("[MobLimiter] Mob will not be culled");
			}

			return true;
		}

		return true;
	}

	private static Entity getTarget(final Player player) {
		assert player != null;
		Entity target = null;
		double targetDistanceSquared = 0;
		final double radiusSquared = 1;
		final Vector l = player.getEyeLocation().toVector();
		final Vector n = player.getLocation().getDirection().normalize();
		final double cos45 = Math.cos(Math.PI / 4);

		for (final LivingEntity other : player.getWorld().getEntitiesByClass(LivingEntity.class)) {
			if (other == player) {
				continue;
			}

			if (target == null | targetDistanceSquared > other.getLocation().distanceSquared(player.getLocation())) {
				final Vector t = other.getLocation().add(0, 1, 0).toVector().subtract(l);
				if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos45) {
					target = other;
					targetDistanceSquared = target.getLocation().distanceSquared(player.getLocation());
				}
			}
		}

		return target;
	}
}
