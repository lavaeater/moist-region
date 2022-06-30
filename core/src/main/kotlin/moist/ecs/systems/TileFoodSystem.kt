package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.math.MathUtils
import ktx.log.debug
import moist.core.GameConstants
import moist.world.SeaManager
import kotlin.math.max

class TileFoodSystem(private val seaManager: SeaManager): IntervalSystem(10f) {
    override fun updateInterval() {
        for(tile in seaManager.allTiles) {
            if(GameConstants.FoodTempRange.contains(tile.waterTemp)) {
                //debug { "Adding food to ${tile.x}:${tile.y}" }
                tile.currentFood += max(1f, tile.currentFood / 10f)
            }
            if(tile.waterTemp > GameConstants.FoodTempDeath) {
                debug { "Removing food from ${tile.x}:${tile.y}" }
                tile.currentFood -= 10f
            }
            tile.currentFood = MathUtils.clamp(tile.currentFood, 0f, GameConstants.TileMaxFood)
        }
    }
}