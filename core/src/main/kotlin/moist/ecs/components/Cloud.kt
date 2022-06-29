package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool

class Cloud: Component, Pool.Poolable {
    val cloudPuffs = mutableListOf<Circle>()
    val cloudDirection = Vector2.X.cpy()
    override fun reset() {
        cloudPuffs.clear()
        cloudDirection.set(Vector2.X)
    }
}