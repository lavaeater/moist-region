package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool


class Renderable: Component, Pool.Poolable {
    var renderType: RenderType = RenderType.RenderableSprite

    override fun reset() {
        renderType = RenderType.RenderableSprite
    }
}