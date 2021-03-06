package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.core.world
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.math.times
import moist.core.GameConstants.CloudMagnitude
import moist.core.GameConstants.MinClouds
import moist.core.cloud
import moist.ecs.components.Cloud
import moist.world.SeaManager
import moist.world.currentTile

class CloudSystem: IteratingSystem(allOf(Cloud::class, Box2d::class).get()) {
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
            entity.remove<Box2d>()
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