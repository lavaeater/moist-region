package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMaxEnergy
import moist.ecs.components.FishGender.Companion.genders

class Fish : Component, Poolable {
    var id = getFishId()
    var isMoving = false
    var gender = genders.random()
    val direction = vec2()
    var targetTile: Tile? = null
    var fishPlayScore = (0.1f..0.9f).random().toDouble()
    var hasMated = false
    var energy = fishStartEnergy()
    var canDie = true

    private fun fishStartEnergy() =
        ((FishMaxEnergy / 5)..(FishMaxEnergy / 3)).random()

    val fishColor = Color(0f, 1f, 0f, 1f)
    override fun reset() {
        id = getFishId()
        isMoving = false
        canDie = true
        hasMated = false
        gender = genders.random()
        fishPlayScore = (0.1f..0.9f).random().toDouble()
        targetTile = null
        direction.setZero()
        energy = fishStartEnergy()
    }
    companion object {
        var _fishId = 0
        fun getFishId():Int {
            _fishId++
            return _fishId
        }
    }
}