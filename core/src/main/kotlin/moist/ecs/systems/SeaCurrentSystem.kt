package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.ashley.systems.IteratingSystem
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
class SeaCurrentSystem: IntervalSystem(10f) {
    override fun updateInterval() {
        for(column in SeaManager.tiles)
            for(tile in column) {
                val target = tile.neighbours.minByOrNull { it.waterTemp }!!
                if(target.waterTemp < tile.waterTemp) {
                    /*
                    Now we create a force vector pointing towards the target, and
                    also, the magnitude depends on the difference, maybe
                     */
                    tile.currentForce.set((target.x - tile.x).toFloat(), (target.y - tile.y).toFloat())
                } else {
                    tile.currentForce.setZero()
                }
            }
    }
}

class ForcesOnCitySystem: IteratingSystem(allOf(Box::class, City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val currentTile = body.currentTile()

        body.applyForceToCenter(currentTile.currentForce * CurrentsMagnitude, true)
    }
}

fun Body.currentTile() : Tile {
    return SeaManager.tiles[this.position.tileX()][this.position.tileY()]
}

fun Vector2.tileX() : Int {
    return ((this.x + TileSize / 2) / TileSize).toInt()
}

fun Vector2.tileY(): Int {
    return ((this.y + TileSize / 2) / TileSize).toInt()
}