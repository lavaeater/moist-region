package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import moist.core.GameConstants.FoodMax
import moist.core.GameConstants.FoodMin
import moist.core.GameConstants.PopulationMax
import moist.core.GameConstants.PopulationMin
import moist.ecs.components.City

class CityHungerSystem: IteratingSystem(allOf(City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val city = entity.city()
        city.food -= city.population / 20f * deltaTime
        city.food = MathUtils.clamp(city.food, FoodMin, FoodMax)
        if(city.food < city.population) {
            city.population -= ((city.population / 10f) * deltaTime)
        }
        if(city.food > city.population) {
            city.population += ((city.population / 20f) * deltaTime)
        }
        city.population = MathUtils.clamp(city.population, PopulationMin, PopulationMax)
    }

}