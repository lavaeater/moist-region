package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import eater.ai.AiComponent
import eater.core.world
import ktx.ashley.allOf
import ktx.log.debug
import moist.ai.AiCounter
import moist.core.GameConstants
import moist.core.GameStats
import moist.core.randomFish
import moist.core.randomShark
import moist.ecs.components.CreatureStats
import moist.ecs.components.Fish
import moist.ecs.components.Shark

class FishDeathSystem : IteratingSystem(allOf(CreatureStats::class).get()) {
    val fishFamily = allOf(Fish::class).get()
    val allFish get() = engine.getEntitiesFor(fishFamily)
    val sharkFamily = allOf(Shark::class).get()
    val allSharks get() = engine.getEntitiesFor(sharkFamily)
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val fish = entity.creature()
        if (fish.energy < 0f && fish.canDie) {
            debug { "Fish Died, mate!" }
            val ai = AiComponent.get(entity)
            val action = ai.topAction(entity)!!
            AiCounter.addToCounter(action, -1)
            AiCounter.eventCounter["Deaths"] = AiCounter.eventCounter["Deaths"]!! + 1

            if (entity.isFish())
                GameStats.deadFish += 1

            if (entity.isShark())
                GameStats.deadSharks += 1

            val body = entity.body()




            if (allFish.count() < GameConstants.StartFishCount) {
                randomFish()
                debug { "Compensatory fish added" }
            }
            if (allSharks.count() < GameConstants.StartSharkCount) {
                randomShark()
                debug { "Compensatory shark added" }
            }
            engine.removeEntity(entity)
            world().destroyBody(body)

        } else if (fish.energy < 0f) {
            AiCounter.eventCounter["Should Have Died"] =
                if (AiCounter.eventCounter.containsKey("Should Have Died")) AiCounter.eventCounter["Should Have Died"]!! + 1 else 1
            fish.energy = 100f
        }
    }
}

