package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import eater.ai.AiComponent
import ktx.ashley.mapperFor

class Cloud: Component, Pool.Poolable {
    val cloudPuffs = mutableListOf<Circle>()
    val cloudDirection = Vector2.X.cpy()
    override fun reset() {
        cloudPuffs.clear()
        cloudDirection.set(Vector2.X)
    }

    companion object {
        private val mapper = mapperFor<Cloud>()
        fun get(entity: Entity): Cloud {
            return mapper.get(entity)
        }
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
    }
}