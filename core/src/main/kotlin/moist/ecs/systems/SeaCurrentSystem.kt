package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import moist.world.SeaManager

/**
 * A system for sea currents. But how do sea currents work?
 * We need a water temperature system as well, of course.
 *
 *
 */
class SeaCurrentSystem(private val seaManager: SeaManager) : IntervalSystem(1f) {
    override fun updateInterval() {
            for (tile in seaManager.getCurrentTiles()) {
                val target = tile.seaNeighbours.minByOrNull { it.waterTemp }
                if (target != null && target.waterTemp < tile.waterTemp) {
                    /*
                    Now we create a force vector pointing towards the target, and
                    also, the magnitude depends on the difference, maybe
                     */
                    tile.current.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat()).nor()
                } else {
                    tile.current.setZero()
                }
            }
    }
}

