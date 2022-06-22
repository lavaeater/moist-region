package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMaxEnergy

class Fish : Component, Pool.Poolable {
    val direction = vec2()
    var targetTile: Tile? = null
    var fishHideScore = (0.1f..0.9f).random().toDouble()
    var energy = FishMaxEnergy
    override fun reset() {
        var fishHideScore = (0.1f..0.9f).random().toDouble()
        targetTile = null
        direction.setZero()
        energy = FishMaxEnergy
    }
}