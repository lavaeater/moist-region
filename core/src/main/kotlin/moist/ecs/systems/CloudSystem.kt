package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.math.times
import moist.core.GameConstants.CloudMagnitude
import moist.core.GameConstants.MaxCloudSpeed
import moist.core.GameConstants.MinClouds
import moist.ecs.components.Box
import moist.ecs.components.Cloud
import moist.ecs.components.cloud
import moist.injection.Context.inject
import moist.world.SeaManager
import moist.world.world

class CloudSystem: IteratingSystem(allOf(Cloud::class, Box::class).get()) {
    private val cloudFamily = allOf(Cloud::class).get()
    private val allClouds get() = engine.getEntitiesFor(cloudFamily)
    private val seaManager by lazy { inject<SeaManager>() }

    override fun update(deltaTime: Float) {
        if(allClouds.count() < MinClouds) {
            for(i in 1..(5..10).random()) {
                val newCloudPos = seaManager.getCurrentTiles().random()
                cloud(newCloudPos.worldCenter)
            }
        }
        super.update(deltaTime)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val currentTile = body.currentTile()
        if(seaManager.getCurrentTiles().all { it != currentTile }) {
            world().destroyBody(body)
            entity.remove<Box>()
            engine.removeEntity(entity)
        } else {
            val cloud = entity.cloud()
            cloud.cloudDirection.lerp(currentTile.wind * CloudMagnitude, 0.1f)
            body.applyForceToCenter(cloud.cloudDirection, true)
            val drag = body.linearVelocity * -0.5f
            body.applyForceToCenter(drag, true)
        }
    }
}