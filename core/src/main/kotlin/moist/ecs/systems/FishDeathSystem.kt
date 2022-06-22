package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import moist.ecs.components.Fish
import moist.world.world

class FishDeathSystem: IteratingSystem(allOf(Fish::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val fish = entity.fish()
        if(fish.energy < 0f) {
            val body = entity.body()
            world().destroyBody(body)
            engine.removeEntity(entity)
        }
    }
}

