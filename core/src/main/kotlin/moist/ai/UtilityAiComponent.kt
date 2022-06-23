package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import ktx.log.debug
import ktx.math.minus
import moist.core.GameConstants.FishEatingPace
import moist.core.GameConstants.FishMatingEnergyRequirement
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.TileMaxFood
import moist.ecs.components.Tile
import moist.ecs.components.areaAround
import moist.ecs.systems.body
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish

class UtilityAiComponent : Component, Pool.Poolable {
    val actions = defaultActions.toMutableList()
    private var currentAction: AiAction? = null
    fun topAction(entity: Entity): AiAction? {
        val potentialAction = actions.maxByOrNull { it.score(entity) }
        if (currentAction != potentialAction) {
            currentAction = potentialAction
        }
        return currentAction
    }

    override fun reset() {
        actions.clear()
        actions.addAll(defaultActions)
        currentAction = null
    }

    companion object {

        private val fishHidingAction = GenericAction("Fish Hide", {
            it.fish().fishHideScore
        }, {}, { entity, deltaTime ->
            val body = entity.body()
            val fish = entity.fish()
            when (fish.targetTile) {
                null -> {
                    val currentTile = body.currentTile()
                    fish.targetTile = currentTile.areaAround().minByOrNull { it.waterTemp }!!
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                }
                body.currentTile() -> {
                    fish.targetTile = null
                }
                else -> {
                    debug { "Hiding at ${fish.targetTile}, energy: ${fish.energy}" }
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                }
            }
        })

        private val fishMatingAction = GenericAction("Fish Mating", {
            if (it.fish().energy > FishMatingEnergyRequirement) 1.0 else 0.0
        }, {

        }, { entity, deltaTime ->
            //1. Is there a fish of opposite sex in this tile?

        })

        private val fishFoodAction = GenericAction("Fish Food",
            {
                (1f - MathUtils.norm(0f, FishMaxEnergy, it.fish().energy)).toDouble()
            },
            {},
            { entity, deltaTime ->
                //1. Check the current tile for food
                val body = entity.body()
                val fish = entity.fish()
                val currentTile = body.currentTile()

                if (fish.targetTile != null && fish.targetTile != currentTile) {
                    //We are going somewhere and we are not there yet.
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                } else if (fish.targetTile == currentTile) {
                    fish.targetTile = null
                    if (currentTile.currentFood > 0f) {
                        val eatAmount = deltaTime * FishEatingPace
                        fish.direction.set(currentTile.worldCenter - body.worldCenter).nor()
                        fish.energy += eatAmount
                        fish.energy = MathUtils.clamp(fish.energy, 0f, FishMaxEnergy)
                        currentTile.currentFood -= eatAmount
                        currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, TileMaxFood)
                        debug { "Ate $eatAmount, energy: ${fish.energy}, food left: ${currentTile.currentFood}" }
                        // We should eat here
                    } else {
                        /*
                        We need to find somewhere else to eat
                        If a neighbouring tile has food, go there,
                        otherwise, go to a random tile
                         */
                        val foodTiles = currentTile.neighbours.filter { it.currentFood > 0f }

                        if (foodTiles.any())
                            fish.targetTile = foodTiles.random()
                        else {
                            fish.targetTile = currentTile.areaAround().filter { it.currentFood > 0 }.random()
                        }
                        debug { "No food at ${currentTile.x}, ${currentTile.y}, going to ${fish.targetTile} instead" }
                    }
                } else if (fish.targetTile == null) {
                    if (currentTile.currentFood > 0f) {
                        val eatAmount = deltaTime * FishEatingPace
                        fish.direction.set(currentTile.worldCenter - body.worldCenter).nor()
                        fish.energy += eatAmount
                        fish.energy = MathUtils.clamp(fish.energy, 0f, FishMaxEnergy)
                        currentTile.currentFood -= eatAmount
                        currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, TileMaxFood)
                        debug { "Ate $eatAmount, energy: ${fish.energy}, food left: ${currentTile.currentFood}" }
                        // We should eat here
                    } else {
                        /*
                        We need to find somewhere else to eat
                        If a neighbouring tile has food, go there,
                        otherwise, go to a random tile
                         */
                        var foodTiles = currentTile.neighbours.filter { it.currentFood > 0f }

                        if (foodTiles.any())
                            fish.targetTile = foodTiles.random()
                        else {
                            var radius = 15
                            while (foodTiles.isEmpty()) {
                                foodTiles = currentTile.areaAround(radius++).filter { it.currentFood > 0 }
                            }
                            fish.targetTile = foodTiles.random()
                        }
                        debug { "No food at ${currentTile.x}, ${currentTile.y}, going to ${fish.targetTile} instead" }
                    }
                }
            }
        )

        val defaultActions = setOf(fishHidingAction, fishFoodAction)
    }
}