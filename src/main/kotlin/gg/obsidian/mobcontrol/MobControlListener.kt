package gg.obsidian.mobcontrol

import org.bukkit.Material
import org.bukkit.entity.Ageable
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Sheep
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.inventory.ItemStack

class MobControlListener(private val plugin: MobControl) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChunkUnload(e: ChunkUnloadEvent) {
        plugin.mobs.removeFromChunk(e.chunk)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCreatureSpawnEvent(event: CreatureSpawnEvent) {
        plugin.mobs.limitCreatureSpawn(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onEntityDeath(event: EntityDeathEvent) {
        handleBuffDrops(event)
        handleBuffExp(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerShearEvent(event: EntityDeathEvent) {
        if (plugin.config.BUFF_SHEAR_DROPS <= 1 || event.entity !is Sheep) {
            return
        }

        val entity = event.entity as Sheep
        val l = entity.location

        val count = (1 + (3 * Math.random()).toInt()) * (plugin.config.BUFF_SHEAR_DROPS - 1)
        l.world.dropItemNaturally(l, ItemStack(Material.WOOL, count, entity.color.ordinal.toByte().toShort()))
    }

    private fun handleBuffDrops(event: EntityDeathEvent) {
        if (plugin.config.BUFF_DROPS <= 1 || event.entity !is Ageable) {
            return
        }

        val items = event.drops
        val l = event.entity.location

        for (a in items) {
            if (plugin.config.BUFF_DISABLED_ITEMS.contains(a.typeId)) {
                continue
            }

            for (i in 1..plugin.config.BUFF_DROPS - 1) {
                l.world.dropItemNaturally(l, a)
            }
        }
    }

    private fun handleBuffExp(event: EntityDeathEvent) {
        val target = event.entity
        val attacker = getAttacker(target.lastDamageCause)
        val xp = event.droppedExp

        if (event is PlayerDeathEvent) return
        if (attacker == null) return

        handleMonsterDeath(event, xp)
    }

    private fun handleMonsterDeath(event: EntityDeathEvent, exp: Int) {
        if (plugin.config.BUFF_EXP_MULTIPLIER > 0) {
            event.droppedExp = exp * plugin.config.BUFF_EXP_MULTIPLIER
        }
    }

    private fun getAttacker(attacker: EntityDamageEvent?): Player? {
        if (attacker == null || attacker !is EntityDamageByEntityEvent) {
            return null
        }

        val damager = attacker.damager

        if (damager is Projectile && damager.shooter is Player) {
            return damager.shooter as Player
        } else if (damager is Player) {
            return damager
        }

        return null
    }
}
