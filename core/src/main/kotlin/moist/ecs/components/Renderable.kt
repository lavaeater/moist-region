package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor


class Renderable: Component, Pool.Poolable {
    var renderType: RenderType = RenderType.NotReallyRelevant

    override fun reset() {
        renderType = RenderType.NotReallyRelevant
    }

    companion object {
        private val mapper = mapperFor<Renderable>()
        fun get(entity: Entity): Renderable {
            return mapper.get(entity)
        }
        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }
    }
}