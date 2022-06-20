package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool


class Renderable: Component, Pool.Poolable {
    var renderType: RenderType = RenderType.Sprite

    override fun reset() {
        renderType = RenderType.Sprite
    }
}