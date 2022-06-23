package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Pool
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMaxEnergy
import moist.ecs.components.FishGender.Companion.genders

sealed class FishGender(val name: String) {
    object Blork: FishGender("Blork")
    object Spork: FishGender("Spork")
    companion object {
        val genders = listOf(Blork, Spork)
    }
}

class Fish : Component, Pool.Poolable {
    var gender = genders.random()
    val direction = vec2()
    var targetTile: Tile? = null
    var fishHideScore = (0.1f..0.6f).random().toDouble()
    var energy = ((FishMaxEnergy / 3)..FishMaxEnergy).random()
    override fun reset() {
        gender = genders.random()
        var fishHideScore = (0.1f..0.9f).random().toDouble()
        targetTile = null
        direction.setZero()
        energy = ((FishMaxEnergy / 3)..FishMaxEnergy).random()
    }
}