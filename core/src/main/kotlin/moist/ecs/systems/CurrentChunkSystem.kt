package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import moist.ecs.components.CameraFollow
import moist.world.SeaManager

class CurrentChunkSystem(private val seaManager: SeaManager): IteratingSystem(allOf(CameraFollow::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val tileX = body.tileX()
        val tileY = body.tileY()
        seaManager.updateCurrentChunks(tileX, tileY)
    }
}