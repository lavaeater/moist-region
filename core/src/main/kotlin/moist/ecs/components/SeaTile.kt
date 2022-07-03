package moist.ecs.components

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import eater.world.Tile
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