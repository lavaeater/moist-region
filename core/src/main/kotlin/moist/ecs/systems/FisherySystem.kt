package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.log.debug
import moist.core.GameConstants
import moist.core.GameStats
import moist.core.randomFish
import moist.ecs.components.City
import moist.ecs.components.Fish
import moist.world.world

class FisherySystem : IteratingSystem(allOf(City::class).get()) {

    val fishFamily = allOf(Fish::class).get()
    val allFish get()= engine.getEntitiesFor(fishFamily)
    private val toRemove = mutableListOf<Entity>()
    private var checkFishCount = false
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val city = entity.city()
        for (fishEntity in city.potentialCatches.keys) {
            city.potentialCatches[fishEntity] = city.potentialCatches[fishEntity]!! - deltaTime
            if (city.potentialCatches[fishEntity]!! < 0f)
                toRemove.add(fishEntity)
        }
        for (fishEntity in toRemove) {
            city.potentialCatches.remove(fishEntity)
            city.food += fishEntity.fish().energy
            GameStats.caughtFish++
            world().destroyBody(fishEntity.body())
            engine.removeEntity(fishEntity)
            checkFishCount = true
        }
        toRemove.clear()
        if(checkFishCount && allFish.count() < GameConstants.StartFishCount) {
            checkFishCount = false
            randomFish()
            debug { "Compensatory fish added" }
        }
    }

}