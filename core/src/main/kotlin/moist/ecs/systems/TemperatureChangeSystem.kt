package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.math.MathUtils
import moist.core.GameConstants
import moist.core.GameConstants.MaxWaterTemp
import moist.core.GameConstants.MinWaterTemp
import moist.world.SeaManager
import kotlin.math.absoluteValue

class TemperatureChangeSystem(private val seaManager: SeaManager) : IntervalSystem(2f) {
    override fun updateInterval() {
        for (tile in seaManager.allTiles) {
            if (tile.neighbours.filter { it.waterTemp > tile.waterTemp }.count() > 4) {
                if ((1..100).random() > 55) {
                    tile.waterTemp = MathUtils.lerp(tile.waterTemp, tile.neighbours.maxOf { it.waterTemp }, 0.5f)
                }
            } else if (tile.neighbours.filter { it.waterTemp < tile.waterTemp }.count() > 4) {
                if ((1..100).random() > 55) {
                    tile.waterTemp = MathUtils.lerp(tile.waterTemp, tile.neighbours.minOf { it.waterTemp }, 0.5f)
                }
            }
            val y = if (tile.y == 0) 1 else tile.y
            val factor = (1f - 1f / y.absoluteValue.toFloat()) * 100

            if ((1..100).random() < factor) {
                tile.waterTemp - 1f
            } else {
                tile.waterTemp + 1f
            }

            tile.waterTemp = MathUtils.clamp(tile.waterTemp, MinWaterTemp, MaxWaterTemp)

        }
    }
}