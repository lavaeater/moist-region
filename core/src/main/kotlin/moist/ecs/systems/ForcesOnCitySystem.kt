package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import ktx.math.times
import moist.core.GameConstants
import moist.core.GameConstants.WindMagnitude
import moist.ecs.components.City
import moist.world.SeaManager
import moist.world.tileX
import moist.world.tileY


class ForcesOnCitySystem(private val seaManager: SeaManager) : IteratingSystem(allOf(Box2d::class, City::class).get()) {
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
        city.currentForce.lerp(currentTile.current * GameConstants.CurrentsMagnitude, 0.1f)
        body.applyForceToCenter(city.currentForce, true)
        city.windForce.lerp(forceOnBoat, 0.1f)
        body.applyForceToCenter(city.windForce, true)

        city.drag.set(body.linearVelocity * -0.5f)
        body.applyForceToCenter(city.drag, true)
    }
}