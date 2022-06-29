package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.log.debug
import moist.ai.AiCounter
import moist.core.GameConstants
import moist.ecs.components.Fish
import moist.ecs.components.randomFish
import moist.world.world

class FishDeathSystem: IteratingSystem(allOf(Fish::class).get()) {
    val fishFamily = allOf(Fish::class).get()
    val allFish get()= engine.getEntitiesFor(fishFamily)
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val fish = entity.fish()
        if(fish.energy < 0f && fish.canDie) {
            debug { "Fish Died, mate!"}
            val body = entity.body()
            world().destroyBody(body)
            val ai = AshleyMappers.ai.get(entity)
            val action = ai.topAction(entity)!!
            AiCounter.actionCounter[action] = AiCounter.actionCounter[action]!! - 1
            AiCounter.eventCounter["Deaths"] = AiCounter.eventCounter["Deaths"]!! + 1

            engine.removeEntity(entity)
            if(allFish.count() < GameConstants.StartFishCount) {
                randomFish()
                debug { "Compensatory fish added" }
            }
        } else if(fish.energy < 0f) {
            AiCounter.eventCounter["Should Have Died"] = if(AiCounter.eventCounter.containsKey("Should Have Died")) AiCounter.eventCounter["Should Have Died"]!! + 1 else 1
            fish.energy = 100f
        }
    }
}

