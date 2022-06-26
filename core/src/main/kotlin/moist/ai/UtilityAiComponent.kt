package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import ktx.ashley.allOf
import ktx.log.debug
import ktx.math.minus
import moist.core.GameConstants.FishEatingPace
import moist.core.GameConstants.FishMatingEnergyRequirement
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.MaxFishCount
import moist.core.GameConstants.TileMaxFood
import moist.ecs.components.Fish
import moist.ecs.components.areaAround
import moist.ecs.components.fish
import moist.ecs.systems.body
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish
import moist.world.engine

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

        private val fishFamily = allOf(Fish::class).get()
        private val allTheFish get() = engine().getEntitiesFor(fishFamily)

        private val fishPlayAction = GenericAction("Fish Playing", {
            it.fish().fishPlayScore
        }, {
            it.fish().targetTile = null
            debug { "Aborted play"}
        }, { entity, deltaTime ->
            val body = entity.body()
            val fish = entity.fish()
            when (fish.targetTile) {
                null -> {
                    val currentTile = body.currentTile()
                    fish.targetTile = currentTile.areaAround(15).random()
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                    debug { "Going to play at ${fish.targetTile}"}
                }
                body.currentTile() -> {
                    debug { "Arrived at ${fish.targetTile} for playtime" }
                    fish.targetTile = null
                }
                else -> {
                    debug { "On my way to ${fish.targetTile}"}
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                }
            }
        })

        private val fishMatingAction = GenericAction("Fish Mating", {
            if (it.fish().energy > FishMatingEnergyRequirement) 1.0 else 0.0
        }, {
            it.fish().targetTile = null
            debug { "Mating aborted" }
        }, { entity, deltaTime ->
            val fish = entity.fish()
            val body = entity.body()
            val currentTile = entity.body().currentTile()
            if (fish.targetTile != null && fish.targetTile != currentTile) {
                //We are going somewhere and we are not there yet.
                fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
            } else if (fish.targetTile == currentTile) {
                fish.targetTile = null
                fish.direction.set(currentTile.worldCenter - body.worldCenter).nor()
                val closestFish = allTheFish.minByOrNull { it.body().position.dst(body.position) }!!
                if (closestFish.body().currentTile() == currentTile) {
                    if (allTheFish.count() < MaxFishCount) {
                        //MATE! - otherwise just let this repeat itself!
                        val numberOfFish = (1..5).random()
                        for (i in 0 until numberOfFish) {
                            fish(body.position)
                        }
                        debug { "$numberOfFish were born!" }
                        fish.energy = fish.energy / 3f
                    }
                }
            } else if (fish.targetTile == null) {
                //1. Are there fish within mating distance that also wish to mate?
                val closestFish = allTheFish.minByOrNull { it.body().position.dst(body.position) }!!
                fish.targetTile = closestFish.body().currentTile()
                debug { "Trying to find a mate"}
            }
        })

        private val fishFoodAction = GenericAction("Fish Food",
            {
                (1f - MathUtils.norm(0f, FishMaxEnergy, it.fish().energy)).toDouble()
            },
            {
                it.fish().targetTile = null
                debug { "Stopped eating, y'all" }
            },
            { entity, deltaTime ->
                //1. Check the current tile for food
                val body = entity.body()
                val fish = entity.fish()
                val currentTile = body.currentTile()

                if (fish.targetTile != null && fish.targetTile != currentTile) {
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                } else if (fish.targetTile == currentTile) {
                    if (currentTile.currentFood > 0f) {
                        debug { "Eating at $currentTile" }
                        val eatAmount = deltaTime * FishEatingPace
                        fish.direction.setZero()
                        fish.energy += eatAmount
                        fish.energy = MathUtils.clamp(fish.energy, 0f, FishMaxEnergy)
                        currentTile.currentFood -= eatAmount
                        currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, TileMaxFood)
                        //debug { "Ate $eatAmount, energy: ${fish.energy}, food left: ${currentTile.currentFood}" }
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
                            var radius = 5
                            while (foodTiles.isEmpty()) {
                                foodTiles = currentTile.areaAround(radius++).filter { it.currentFood > 0 }
                            }
                            fish.targetTile = foodTiles.random()
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
                            var radius = 5
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

        val defaultActions = setOf(fishPlayAction, fishFoodAction, fishMatingAction)
    }
}