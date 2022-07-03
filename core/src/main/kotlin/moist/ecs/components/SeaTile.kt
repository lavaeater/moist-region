package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import eater.ecs.components.Tile
import eater.injection.InjectionContext.Companion.inject
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
    val color = Color(1f, 0f, 1f, 0.7f)

    var originalDepth: Float = 0f
    val current = vec2()
    val wind = vec2()
    val worldCenter: Vector2 = vec2()
        get() {
            field.set((x * TileSize + TileSize / 2), (y * TileSize + TileSize / 2))
            return field
        }
    override val neighbours = mutableListOf<Tile>()
    val seaNeighbours: List<SeaTile> get() = neighbours.map { it as SeaTile }
}

fun Tile.someTileAt(distance: Int, directionX: Int, directionY: Int): Tile {
    val targetX = this.x + directionX * distance
    val targetY = this.y + directionY * distance
    val seaManager = inject<SeaManager>()
    return seaManager.getTileAt(targetX, targetY)
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