package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.math.vec2

data class Tile(var x: Int,
                    var y:Int,
                    var depth: Float = 0f,
                    var waterTemp: Float = 10f,
                    var airTemp:Float = 15f) {
    var originalDepth: Float = 0f
    val currentForce = vec2()
    val neighbours = mutableListOf<Tile>()
}