package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.math.vec2

class Fish : Component, Pool.Poolable {
    val direction = vec2()
    override fun reset() {
    }
}