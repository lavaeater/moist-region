package moist.ai.utility

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.Fixture
import ktx.ashley.allOf
import ktx.box2d.RayCast
import ktx.box2d.rayCast
import ktx.log.debug
import ktx.math.vec2
import moist.ecs.components.Box
import moist.ecs.systems.body
import moist.world.engine
import kotlin.reflect.KClass

class CanISeeThisConsideration<ToLookFor : Component>(
    componentClass: KClass<ToLookFor>,
    private val stop: Boolean = true
) : Consideration("Can I See ") {
    private val entitiesToLookForFamily = allOf(componentClass, Box::class).get()
    private val engine by lazy { engine() }
    override fun normalizedScore(entity: Entity): Float {
        val inrangeEntities = engine.getEntitiesFor(entitiesToLookForFamily)
            .filter { it.body().position.dst(agentPosition) < agentProps.viewDistance }
            .filter {
                canISeeYouFromHere(
                    agentPosition,
                    agentProps.directionVector,
                    it.transform().position,
                    agentProps.fieldOfView
                )
            }
        debug { "LookForAndStore found ${inrangeEntities.size} entities in range and in the field of view" }
        var haveIseenSomething = false
        for (potential in inrangeEntities) {
            val entityPosition = potential.transform().position
            var lowestFraction = 1f
            var closestFixture: Fixture? = null
            val pointOfHit = vec2()
            val hitNormal = vec2()


            world().rayCast(
                agentPosition,
                entityPosition
            ) { fixture, point, normal, fraction ->
                if (fraction < lowestFraction) {
                    lowestFraction = fraction
                    closestFixture = fixture
                    pointOfHit.set(point)
                    hitNormal.set(normal)
                }
                RayCast.CONTINUE
            }

            if (closestFixture != null && closestFixture!!.isEntity() && inrangeEntities.contains(closestFixture!!.getEntity())) {
                debug { "LookForAndStore - entity at $entityPosition can be seen " }
                haveIseenSomething = true
                break
            }
        }
        return if (haveIseenSomething) 1.0f else 0f
    }
}