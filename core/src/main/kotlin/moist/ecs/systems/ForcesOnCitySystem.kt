package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.math.times
import moist.core.GameConstants
import moist.core.GameConstants.WindMagnitude
import moist.ecs.components.Box
import moist.ecs.components.City
import moist.world.SeaManager


class ForcesOnCitySystem(private val seaManager: SeaManager) : IteratingSystem(allOf(Box::class, City::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val city = entity.city()
        val currentTile = seaManager.getTileAt(body.tileX(), body.tileY())


        val wind = currentTile.wind
        val sail = city.sailVector


//        https://www.reddit.com/r/gamedev/comments/6fftbz/help_with_simplifying_sailing_physics/
        //Now for some wind stuff. But we need a sail on the boat
        //dotProduct(windDirection, sailDirection) + 1) / 2 * sailDirection * windForce
        val someValue = (Vector2.dot(wind.x, wind.y, sail.x, sail.y) + 1f) / 2f
        val forceOnBoat = sail * WindMagnitude * someValue
        city.currentForce.set(currentTile.current * GameConstants.CurrentsMagnitude)
        body.applyForceToCenter(city.currentForce, true)
        city.windForce.set(forceOnBoat)
        body.applyForceToCenter(city.windForce, true)

        city.drag.set(body.linearVelocity * -0.5f)
        body.applyForceToCenter(city.drag, true)
    }
}