package moist.ecs.components

sealed class RenderType(val layer: Int) {
    object Sea: RenderType(0)
    object Sprite: RenderType(1)
}