package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import moist.world.SeaManager

class WindSystem(private val seaManager: SeaManager) : IntervalSystem(1f) {
    override fun updateInterval() {
        for (tile in seaManager.getCurrentTiles()) {
            val target = seaManager.areaAround(tile, 10).maxByOrNull { it.waterTemp }
            if (target != null && target.waterTemp > tile.waterTemp) {
                /*
                Now we create a force vector pointing towards the target, and
                also, the magnitude depends on the difference, maybe
                 */
                tile.wind.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat()).nor()
            } else {
                tile.wind.setZero()
            }
        }
    }
}