package gg.obsidian.mobcontrol

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.*
import org.bukkit.plugin.java.JavaPlugin

import java.io.File

public class MobControl() : JavaPlugin() {

    private val listener = MobControlListener(this)
    val config = Configuration(this)
    val mobs = MobManager(this)

    override fun onEnable() {
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            getConfig().options().copyDefaults(true)
            saveConfig()
        }

        config.load()

        server.pluginManager.registerEvents(listener, this)
    }

    override fun onDisable() {
        mobs.removeFromAllChunks()
    }

    override fun onCommand(sender: CommandSender?, cmd: Command?, label: String?, args: Array<String>?): Boolean {
        if (!cmd!!.name.equals("mobcontrol", ignoreCase = true)) {
            return true
        }

        if (args!!.size() == 1 && args[0].equals("cull", ignoreCase = true) && sender!!.hasPermission("mobcontrol.cull")) {
            sender!!.sendMessage("[MobControl] Culling mobs in all loaded chunks")
            mobs.removeFromAllChunks()
            return true
        }

        if (args.size == 1 && args[0].equals("check", ignoreCase = true) && sender!!.hasPermission("mobcontrol.check") && sender is Player) {
            val living = getTarget(sender as Player?) as LivingEntity
            if (living == null) {
                sender.sendMessage("[MobLimiter] No mob in sight")
                return true
            }

            val animalOrMonster = (living is Animals) || (living is Monster)
            if (!mobs.isSpecialMob(living) && animalOrMonster) {
                sender.sendMessage("[MobLimiter] Mob will be culled")
            } else {
                sender.sendMessage("[MobLimiter] Mob will not be culled")
            }

            return true
        }

        return true
    }

    private fun getTarget(player: Player?): Entity? {
        assert(player != null)
        var target: Entity? = null
        var targetDistanceSquared = 0.0
        val radiusSquared = 1.0
        val l = player!!.eyeLocation.toVector()
        val n = player.location.direction.normalize()
        val cos45 = Math.cos(Math.PI / 4)

        for (other in player.world.getEntitiesByClass(LivingEntity::class.java)) {
            if (other === player) {
                continue
            }

            if ((target == null) or (targetDistanceSquared > other.location.distanceSquared(player.location))) {
                val t = other.location.add(0.0, 1.0, 0.0).toVector().subtract(l)
                if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos45) {
                    target = other
                    targetDistanceSquared = target!!.location.distanceSquared(player.location)
                }
            }
        }

        return target
    }
}
