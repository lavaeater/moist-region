package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool

class Tile: Component, Pool.Poolable {
    var x = 0
    var y = 0
    var originalDepth = 0f
    var depth: Float = 0f
    var waterTemp: Float = 10f
    var airTemp:Float = 15f
    override fun reset() {
        x = 0
        y = 0
        depth = 0f
        waterTemp = 10f
        airTemp = 15f
    }
}