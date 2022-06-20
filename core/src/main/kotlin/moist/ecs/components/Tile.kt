package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import ktx.math.vec2
import moist.core.GameConstants.TileSize

data class Tile(var x: Int,
                    var y:Int,
                    var depth: Float = 0f,
                    var waterTemp: Float = 10f,
                    var airTemp:Float = 15f) {
    var originalDepth: Float = 0f
    val currentForce = vec2()
    val worldCenter: Vector2 = vec2()
        get() {
        field.set((x * TileSize + TileSize / 2),(y * TileSize + TileSize / 2))
        return field
    }
    val neighbours = mutableListOf<Tile>()
}