package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMatingEnergyRequirement
import moist.core.GameConstants.FishMaxEnergy
import moist.ecs.components.FishGender.Companion.genders

sealed class FishGender(val name: String) {
    object Blork: FishGender("Blork")
    object Spork: FishGender("Spork")
    companion object {
        val genders = listOf(Blork, Spork)
    }
}

class Cloud: Component, Poolable {
    val cloudPuffs = mutableListOf<Circle>()
    val cloudDirection = Vector2.X.cpy()
    override fun reset() {
        cloudPuffs.clear()
        cloudDirection.set(Vector2.X)
    }
}

class Fish : Component, Poolable {
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
        isMoving = false
        canDie = true
        hasMated = false
        gender = genders.random()
        fishPlayScore = (0.1f..0.9f).random().toDouble()
        targetTile = null
        direction.setZero()
        energy = fishStartEnergy()
    }
}