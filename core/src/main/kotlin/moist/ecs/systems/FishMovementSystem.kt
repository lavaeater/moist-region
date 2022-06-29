package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.times
import ktx.math.vec2
import moist.core.GameConstants.FishEnergyExpenditurePerSecond
import moist.core.GameConstants.FishMaxVelocity
import moist.ecs.components.Box
import moist.ecs.components.Fish

class FishMovementSystem : IteratingSystem(
    allOf(
        Fish::class,
        Box::class
    ).get()
) {
    private val separationRange = 10f
    private val fishFamily = allOf(Fish::class).get()
    private val allTheFishInTheSea get() = engine.getEntitiesFor(fishFamily)

    private val alignment = vec2()
    private val cohesion = vec2()
    private val separation = vec2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.body()
        val fish = entity.fish()
        fixFlocking(body, fish)
        moveEnemy(body, fish, deltaTime)
    }

    private fun fixFlocking(body: Body, thisFish: Fish) {
        computeFlocking(body, thisFish)
    }

    private fun computeFlocking(thisBody: Body, thisFish: Fish) {
        val sep = vec2()
        val coh = vec2()
        val ali = vec2()
        var count = 0
        for (fish in allTheFishInTheSea) {
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

    private fun moveEnemy(body: Body, fish: Fish, deltaTime: Float) {
        val currentTile = body.currentTile()
        val currentVelocity = body.linearVelocity

        if(fish.direction != Vector2.Zero) {
            fish.direction
                .add(cohesion.scl(1.5f))
                .add(separation.scl(1.5f))
                .add(alignment.scl(1.0f))
                .nor()
                .scl(FishMaxVelocity)
        }

        val velocityChange = fish.direction - currentVelocity
        val impulse = velocityChange * body.mass
        body.applyLinearImpulse(impulse, body.worldCenter, true)
        body.applyLinearImpulse(currentTile.current, body.worldCenter, true)

        if (body.linearVelocity.len2() > 1f) {
            fish.energy -= FishEnergyExpenditurePerSecond * deltaTime
            fish.isMoving = true
        } else {
            fish.energy -= FishEnergyExpenditurePerSecond * deltaTime / 2
            fish.isMoving = false
        }

//        body.applyForceToCenter(fish.direction * FishMagnitude, true)
//
//
//
//        if(body.linearVelocity.len() > FishMaxVelocity)
//            body.linearVelocity.setLength(FishMaxVelocity)
    }

}

