package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import moist.core.GameConstants
import moist.ecs.components.Tile

/**
 * Waves can also be emitted from special entities. Later
 */
class SeaWavesSystem : EntitySystem() {
    val baseCoolDown = 0.1f
    var waveCoolDown = baseCoolDown
    var currentColumn = 0 - GameConstants.MaxTiles / 2
    var update = true

    override fun update(deltaTime: Float) {
        waveCoolDown -= deltaTime
        if (waveCoolDown < 0f) {
            waveCoolDown = baseCoolDown
            currentColumn++
            if (currentColumn >= GameConstants.MaxTiles / 2) {
                currentColumn = 0 - GameConstants.MaxTiles / 2
            }
            update = true
        }
        if(update) {
            super.update(deltaTime)
            update = false
        }
    }
//
//    override fun processEntity(entity: Entity, deltaTime: Float) {
//        val tile = entity.tile()
//        val entityX = tile.x
//        tile.depth = tile.originalDepth
//        if ((entityX - 1) == currentColumn || (entityX + 1) == currentColumn) {
//            tile.depth = MathUtils.clamp(tile.depth + 0.05f, 0f, 1f)
//        } else if (entityX == currentColumn) {
//            tile.depth = MathUtils.clamp(tile.depth + 0.1f, 0f, 1f)
//        }
//    }
}