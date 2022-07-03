package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import eater.ecs.components.Box2d
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.times
import ktx.math.vec2
import moist.core.GameConstants.SharkExpenditurePerSecond
import moist.ecs.components.CreatureStats
import moist.ecs.components.Shark
import moist.world.currentTile

class SharkMovementSystem : IteratingSystem(
    allOf(
        Shark::class,
        CreatureStats::class,
        Box2d::class
    ).get()
) {
    private val separationRange = 75f
    private val sharkFamily = allOf(CreatureStats::class, Shark::class).get()
    private val sharkies get() = engine.getEntitiesFor(sharkFamily)

    private val alignment = vec2()
    private val cohesion = vec2()
    private val separation = vec2()

    private val cohesionScale = 1.5f
    private val separationScale = 2.5f
    private val alignmentScale = 1.5f
    private val directionScale = 3f

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val fish = entity.creature()
        fixFlocking(body, fish)
        moveEnemy(body, fish, deltaTime)
    }

    private fun fixFlocking(body: Body, thisCreature: CreatureStats) {
        computeFlocking(body, thisCreature)
    }

    private fun computeFlocking(thisBody: Body, thisCreature: CreatureStats) {
        val sep = vec2()
        val coh = vec2()
        val ali = vec2()
        var count = 0
        for (fish in sharkies) {
            val otherBody = fish.body()
            val position = otherBody.worldCenter
            if (position.dst(thisBody.worldCenter) < separationRange) {
                sep.x += position.x - thisBody.worldCenter.x
                sep.y += position.y - thisBody.worldCenter.y
                coh.x += position.x
                coh.y += position.y
                val velocity = otherBody.linearVelocity
                ali.x += velocity.x
                ali.y += velocity.y
                count++
            }
        }
        if (count > 0) {
            sep.x /= count
            sep.y /= count
            sep.x *= -1
            sep.y *= -1
            separation.set(sep.nor())
            coh.x /= count
            coh.y /= count
            coh.set(coh.sub(thisBody.worldCenter))
            cohesion.set(coh.nor())
            ali.x /= count
            ali.y /= count
            alignment.set(ali.nor())
        }
    }

    private fun moveEnemy(body: Body, creature: CreatureStats, deltaTime: Float) {
        val currentTile = body.currentTile()
        val currentVelocity = body.linearVelocity

        if(creature.direction != Vector2.Zero) {
            creature.direction
                .scl(directionScale)
                .add(cohesion.scl(cohesionScale))
                .add(separation.scl(separationScale))
                .add(alignment.scl(alignmentScale))
                .nor()
                .scl(creature.fishMaxVelocity)
        }

        val velocityChange = creature.direction - currentVelocity
        val impulse = velocityChange * body.mass
        body.applyLinearImpulse(impulse, body.worldCenter, true)
        body.applyLinearImpulse(currentTile.current, body.worldCenter, true)

        if (body.linearVelocity.len() > 50f) {
            creature.energy -= SharkExpenditurePerSecond * creature.size * deltaTime
            creature.isMoving = true
        } else {
            creature.energy -= SharkExpenditurePerSecond * creature.size * deltaTime / 2
            creature.isMoving = false
        }

//        body.applyForceToCenter(fish.direction * FishMagnitude, true)
//
//
//
//        if(body.linearVelocity.len() > FishMaxVelocity)
//            body.linearVelocity.setLength(FishMaxVelocity)
    }

}

