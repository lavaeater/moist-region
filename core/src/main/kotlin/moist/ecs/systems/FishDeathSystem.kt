package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.log.debug
import moist.core.GameConstants
import moist.ecs.components.Fish
import moist.ecs.components.fish
import moist.ecs.components.randomFish
import moist.world.world

class FishDeathSystem: IteratingSystem(allOf(Fish::class).get()) {
    val fishFamily = allOf(Fish::class).get()
    val allFish get()= engine.getEntitiesFor(fishFamily)
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val fish = entity.fish()
        if(fish.energy < 0f) {
            debug { "Fish Died, mate!"}
            val body = entity.body()
            world().destroyBody(body)
            engine.removeEntity(entity)
            if(allFish.count() < GameConstants.StartFishCount) {
                randomFish()
                debug { "Compensatory fish added" }
            }
        }
    }
}

