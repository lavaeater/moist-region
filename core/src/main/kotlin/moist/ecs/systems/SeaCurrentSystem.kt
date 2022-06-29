package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import moist.core.GameConstants.TileSize
import moist.ecs.components.Tile
import moist.injection.Context.inject
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
                val target = tile.neighbours.minByOrNull { it.waterTemp }
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

fun Body.currentTile() : Tile {
    return inject<SeaManager>().getTileAt(this.tileX(), this.tileY())
}

fun Body.tileX(): Int {
    return this.position.tileX()
}
fun Body.tileY(): Int {
    return this.position.tileY()
}

fun Vector2.tileX(): Int {
    return MathUtils.floor(this.x / TileSize)
}

fun Vector2.tileY(): Int {
    return MathUtils.floor(this.y / TileSize)
}