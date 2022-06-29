package moist.ecs.components

import com.badlogic.gdx.math.Vector2
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.TileSize
import moist.core.GameConstants.TileStartFood
import moist.injection.Context.inject
import moist.world.SeaManager

data class Tile(
    var x: Int,
    var y: Int,
    var depth: Float = 0f,
    var waterTemp: Float = 10f,
    var airTemp: Float = 15f
) {
    val neighbours = mutableListOf<Tile>()
    var currentFood = (25f..TileStartFood).random()
    var originalDepth: Float = 0f
    val current = vec2()
    val wind = vec2()
    val worldCenter: Vector2 = vec2()
        get() {
            field.set((x * TileSize + TileSize / 2), (y * TileSize + TileSize / 2))
            return field
        }
}

fun Tile.someTileAt(distance:Int, directionX: Int, directionY: Int):Tile {
    val targetX = this.x + directionX * distance
    val targetY = this.y + directionY * distance
    val seaManager = inject<SeaManager>()
    val tile = seaManager.getTileAt(targetX, targetY)
    return tile
}

fun Tile.areaAhead(directionX: Int, directionY: Int, distance: Int, width:Int = 3, excludeSelf: Boolean = true): List<Tile> {
    val widthOffset = (width -1) / 2
    val keys = mutableListOf<Pair<Int,Int>>()
    for(wOff in -widthOffset..widthOffset) {
        for(x in 0..(directionX * distance))
            for(y in 0..(directionY * distance)) {
                keys.add(Pair(this.x + x + wOff * directionX, this.y + y + wOff * directionY))
            }
    }

    val seaManager = inject<SeaManager>()
    val tiles = keys.map { k -> seaManager.getTileAt(k.first, k.second) }

    return if (excludeSelf) tiles - this else tiles
}

fun Tile.areaAround(distance: Int = 5, excludeSelf: Boolean = true): List<Tile> {
    val seaManager = inject<SeaManager>()
    val minX = this.x - distance
    val maxX = this.x + distance
    val xRange = minX..maxX
    val minY = this.y - distance
    val maxY = this.y + distance
    val yRange = minY..maxY
    val tiles = (xRange).map { x -> (yRange).map { y -> seaManager.getTileAt(x, y) } }.flatten()

    return if (excludeSelf) tiles - this else tiles
}