package moist.ecs.systems

import com.badlogic.ashley.systems.IntervalSystem
import com.badlogic.gdx.math.MathUtils
import moist.core.GameConstants
import moist.world.SeaManager

class TileFoodSystem: IntervalSystem(1f) {
    override fun updateInterval() {
        for(tile in SeaManager.flattened) {
            if(GameConstants.FoodTempRange.contains(tile.waterTemp)) {
                tile.currentFood += 1f
            }
            if(tile.waterTemp > GameConstants.FoodTempDeath) {
                tile.currentFood -= 1f
            }
            tile.currentFood = MathUtils.clamp(tile.currentFood, 0f, GameConstants.TileMaxFood)
        }
    }
}