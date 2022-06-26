package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import moist.core.GameConstants.FoodMax
import moist.core.GameConstants.FoodMin
import moist.core.GameConstants.PopulationMax
import moist.core.GameConstants.PopulationMin
import moist.core.GameStats
import moist.ecs.components.City
import java.lang.Float.max
import java.lang.Float.min

class CityHungerSystem : IteratingSystem(allOf(City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val city = entity.city()
        city.food -= city.population / 20f * deltaTime
        city.food = MathUtils.clamp(city.food, FoodMin, FoodMax)
        if (city.food < city.population) {
            if (city.food < 5)
                city.population -= (max(city.population / 10f, 20f) * deltaTime)
            else
                city.population -= (max(city.population / 10f, 5f) * deltaTime)
        }
        if (city.food > city.population) {
            city.population += (max(city.population / 20f, 5f) * deltaTime)
        }
        city.population = MathUtils.clamp(city.population, PopulationMin, PopulationMax)
        if (city.population > GameStats.maxPopulation)
            GameStats.maxPopulation = city.population.toInt()
    }

}