package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class Shark: Component, Pool.Poolable {
    override fun reset() {
    }

    companion object {
        private val mapper = mapperFor<Shark>()
        fun has(entity: Entity):Boolean {
            return mapper.has(entity)
        }
        fun get(entity:Entity): Shark {
            return mapper.get(entity)
        }
    }
}