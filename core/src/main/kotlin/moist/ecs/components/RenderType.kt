package moist.ecs.components

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

sealed class RenderType(val layer: Int) {
    object Sea: RenderType(0)
    object Sprite: RenderType(1)
    class SelfRender(private val renderFunc: (batch: PolygonSpriteBatch, deltaTime: Float) -> Unit): RenderType(1) {
        fun render(batch: PolygonSpriteBatch, deltaTime: Float) {
            renderFunc(batch, deltaTime)
        }
    }

}