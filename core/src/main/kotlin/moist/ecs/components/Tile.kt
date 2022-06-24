package moist.ecs.components

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.TileSize
import moist.core.GameConstants.TileStartFood
import moist.injection.Context.inject
import moist.world.ChunkKey
import moist.world.SeaManager

data class Tile(
    var x: Int,
    var y: Int,
    var depth: Float = 0f,
    var waterTemp: Float = 10f,
    var airTemp: Float = 15f
) {
    val neighbours = mutableListOf<Tile>()
    var currentFood = (0f..TileStartFood).random()
    var originalDepth: Float = 0f
    val currentForce = vec2()
    val worldCenter: Vector2 = vec2()
        get() {
            field.set((x * TileSize + TileSize / 2), (y * TileSize + TileSize / 2))
            return field
        }
}

fun Tile.areaAround(radius: Int = 5, excludeSelf: Boolean = true): List<Tile> {
    val seaManager = inject<SeaManager>()
    val minX = MathUtils.clamp(this.x - radius, 0, MaxTilesPerSide - 1)
    val maxX = MathUtils.clamp(this.x + radius, 0, MaxTilesPerSide - 1)
    val xRange = minX..maxX
    val minY = MathUtils.clamp(this.y - radius, 0, MaxTilesPerSide - 1)
    val maxY = MathUtils.clamp(this.y + radius, 0, MaxTilesPerSide - 1)
    val yRange = minY..maxY
    val tiles = (xRange).map { x -> (yRange).map { y -> seaManager.getTileAt(x, y) } }.flatten()

    return if (excludeSelf) tiles - this else tiles
}