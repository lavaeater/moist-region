package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import moist.ecs.components.City
import moist.world.world

class FisherySystem : IteratingSystem(allOf(City::class).get()) {

    private val toRemove = mutableListOf<Entity>()
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
            world().destroyBody(fishEntity.body())
            engine.removeEntity(fishEntity)
        }
        toRemove.clear()
    }

}