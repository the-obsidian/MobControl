package gg.obsidian.mobcontrol

import org.bukkit.Material
import org.bukkit.entity.Ageable
import org.bukkit.entity.Sheep
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
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
}
