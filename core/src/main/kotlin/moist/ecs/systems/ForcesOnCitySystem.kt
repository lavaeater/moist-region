package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.math.times
import moist.core.GameConstants
import moist.ecs.components.Box
import moist.ecs.components.City
import moist.world.SeaManager

class ForcesOnCitySystem(private val seaManager: SeaManager) : IteratingSystem(allOf(Box::class, City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val currentTile = seaManager.getTileAt(body.tileX(), body.tileY())

        body.applyForceToCenter(currentTile.current * GameConstants.CurrentsMagnitude, true)
    }
}