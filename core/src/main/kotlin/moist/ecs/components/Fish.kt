package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMatingEnergyRequirement
import moist.core.GameConstants.FishMaxEnergy
import moist.ecs.components.FishGender.Companion.genders

class Fish : Component, Poolable {
    var matingCount = 0
    var id = getFishId()
    var isMoving = false
    var gender = genders.random()
    val direction = vec2()
    var targetTile: Tile? = null
    var targetFish: Entity? = null
    var fishPlayScore = (0.1f..0.9f).random().toDouble()
    var energy = fishStartEnergy()
    val canMate get() = energy > FishMatingEnergyRequirement
    var canDie = true

    private fun fishStartEnergy() =
        ((FishMaxEnergy / 5)..(FishMaxEnergy / 3)).random()

    val fishColor = Color(0f, 1f, 0f, 1f)
    override fun reset() {
        id = getFishId()
        isMoving = false
        canDie = true
        gender = genders.random()
        fishPlayScore = (0.1f..0.9f).random().toDouble()
        targetTile = null
        direction.setZero()
        energy = fishStartEnergy()
    }
    companion object {
        private var Id = 0
        fun getFishId():Int {
            Id += 1
            return Id
        }
    }
}