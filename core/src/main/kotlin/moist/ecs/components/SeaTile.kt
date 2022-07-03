package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import eater.injection.Context.inject
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.TileSize
import moist.core.GameConstants.TileStartFood
import moist.world.SeaManager

data class SeaTile(
    override val x: Int,
    override val y: Int,
    var depth: Float = 0f,
    var waterTemp: Float = 10f,
    var airTemp: Float = 15f,
    var currentFood: Float = (25f..TileStartFood).random()
) : Tile {
    val neighbours = mutableListOf<SeaTile>()
    val color = Color(1f, 0f, 1f, 0.7f)

    var originalDepth: Float = 0f
    val current = vec2()
    val wind = vec2()
    val worldCenter: Vector2 = vec2()
        get() {
            field.set((x * TileSize + TileSize / 2), (y * TileSize + TileSize / 2))
            return field
        }
}

fun SeaTile.someTileAt(distance: Int, directionX: Int, directionY: Int): SeaTile {
    val targetX = this.x + directionX * distance
    val targetY = this.y + directionY * distance
    val seaManager = inject<SeaManager>()
    return seaManager.getTileAt(targetX, targetY)
}

sealed class TileDirection(val x: Int, val y: Int) {
    object West : TileDirection(-1, 0)
    object NorthWest : TileDirection(-1, -1)
    object North : TileDirection(0, -1)
    object NorthEast : TileDirection(1, -1)
    object East : TileDirection(1, 0)
    object SouthEast : TileDirection(1, 1)
    object South : TileDirection(0, 1)
    object SouthWest : TileDirection(-1, 1)

    companion object {
        val directions = listOf(West, NorthWest, North, NorthEast, East, SouthEast, South, SouthWest)
    }
}

fun SeaTile.someAreaAt(distance: Int, direction: TileDirection, radius: Int): List<SeaTile> {
    val targetX = this.x + direction.x * distance
    val targetY = this.y + direction.y * distance

    val minX = targetX - radius
    val minY = targetY - radius
    val maxX = targetX + radius
    val maxY = targetY + radius

    val seaManager = inject<SeaManager>()
    return (minX..maxX).map { x -> (minY..maxY).map { y -> seaManager.getTileAt(x, y) } }.flatten()
}

fun SeaTile.areaAhead(
    directionX: Int,
    directionY: Int,
    distance: Int,
    width: Int = 3,
    excludeSelf: Boolean = true
): List<SeaTile> {
    val widthOffset = (width - 1) / 2
    val keys = mutableListOf<Pair<Int, Int>>()
    for (wOff in -widthOffset..widthOffset) {
        for (x in 0..(directionX * distance))
            for (y in 0..(directionY * distance)) {
                keys.add(Pair(this.x + x + wOff * directionX, this.y + y + wOff * directionY))
            }
    }

    val seaManager = inject<SeaManager>()
    val tiles = keys.map { k -> seaManager.getTileAt(k.first, k.second) }

    return if (excludeSelf) tiles - this else tiles
}

fun SeaTile.areaAround(distance: Int = 5, excludeSelf: Boolean = true): List<SeaTile> {
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