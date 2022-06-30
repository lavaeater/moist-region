package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.math.vec2

class City : Component, Poolable {
    val drag = vec2()
    val sailVector: Vector2 = Vector2.Y
    var population = 100f
    var food = 100f//FoodMax / 2

    val currentForce = vec2()
    val windForce = vec2()

    val potentialCatches = mutableMapOf<Entity, Float>()

    override fun reset() {
        population = 100f
        currentForce.setZero()
        windForce.setZero()
        food = 100f// FoodMax / 2
        potentialCatches.clear()
    }
}


