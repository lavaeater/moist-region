package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.mapperFor
import ktx.log.debug
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.FishMaxVelocity
import moist.core.GameConstants.FoodValue
import moist.ecs.components.FishGender.Companion.genders

class CreatureStats : Component, Poolable {
    var matingCount = 0
    var id = getFishId()
    var isMoving = false
    var gender = genders.random()
    val direction = vec2()
    var targetTile: SeaTile? = null
    var targetFish: Entity? = null
    var fishPlayScore = (0.1f..0.9f).random().toDouble()
    var growthValue = 75f
    var energy = fishStartEnergy()
        set(value) {
            if(value > growthValue) {
                size *= 1.1f
                debug { "This creature grew to size $size " }
                field = value / 2f
            } else {
                field = value
            }
            field = MathUtils.clamp(field, -10f, FishMaxEnergy)
        }
    var availableForMating = false
    var canDie = true
    var fishMaxVelocity = FishMaxVelocity
    var size = (0.5f..1.25f).random()
    val foodValue get() = size * FoodValue

    private fun fishStartEnergy() =
        ((FishMaxEnergy / 5)..(FishMaxEnergy / 3)).random()

    val fishColor = Color(0f, 1f, 0f, 1f)
    override fun reset() {
        size = (0.5f..2.5f).random()
        fishMaxVelocity = FishMaxVelocity
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
        private val mapper = mapperFor<CreatureStats>()
        private var Id = 0
        fun getFishId():Int {
            Id += 1
            return Id
        }

        fun has(entity: Entity): Boolean {
            return mapper.has(entity)
        }

        fun get(entity:Entity): CreatureStats {
            return mapper.get(entity)
        }
    }
}