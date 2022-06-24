package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.allOf
import ktx.math.times
import moist.core.GameConstants.CurrentsMagnitude
import moist.core.GameConstants.TileSize
import moist.ecs.components.Box
import moist.ecs.components.City
import moist.ecs.components.Tile
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
                val target = tile.neighbours.minByOrNull { it.waterTemp }!!
                if (target.waterTemp < tile.waterTemp) {
                    /*
                    Now we create a force vector pointing towards the target, and
                    also, the magnitude depends on the difference, maybe
                     */
                    tile.currentForce.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat()).nor()
                } else {
                    tile.currentForce.setZero()
                }
            }
    }
}

class ForcesOnCitySystem(private val seaManager: SeaManager) : IteratingSystem(allOf(Box::class, City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val currentTile = seaManager.getTileAt(body.tileX(), body.tileY())

        body.applyForceToCenter(currentTile.currentForce * CurrentsMagnitude, true)
    }
}

fun Body.tileX(): Int {
    return this.position.tileX()
}
fun Body.tileY(): Int {
    return this.position.tileY()
}

fun Vector2.tileX(): Int {
    return ((this.x) / TileSize).toInt()
}

fun Vector2.tileY(): Int {
    return ((this.y) / TileSize).toInt()
}