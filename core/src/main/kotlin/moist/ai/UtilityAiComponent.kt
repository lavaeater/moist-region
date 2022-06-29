package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.math.minus
import moist.core.GameConstants.FishEatingPace
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.MaxFishCount
import moist.core.GameConstants.TileMaxFood
import moist.ecs.components.Fish
import moist.ecs.components.areaAround
import moist.ecs.components.fish
import moist.ecs.components.someTileAt
import moist.ecs.systems.body
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish
import moist.injection.Context.inject
import moist.world.SeaManager
import moist.world.engine

class UtilityAiComponent : Component, Pool.Poolable {
    val actions = defaultActions.toMutableList()
    private var currentAction: AiAction? = null

    fun updateAction(entity: Entity) {
        actions.sortByDescending { it.score(entity) }
    }

    fun topAction(entity: Entity): AiAction? {
        val potentialAction = actions.first()// actions.maxByOrNull { it.score(entity) }
        if (currentAction != potentialAction) {
            if (currentAction != null)
                AiCounter.actionCounter[currentAction!!] = AiCounter.actionCounter[currentAction]!! - 1
            AiCounter.actionCounter[potentialAction] = AiCounter.actionCounter[potentialAction]!! + 1
            currentAction?.abort(entity)
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
        val mapper = mapperFor<UtilityAiComponent>()
        fun get(entity: Entity): UtilityAiComponent {
            return mapper.get(entity)
        }

        private val fishFamily = allOf(Fish::class).get()
        private val allTheFish get() = engine().getEntitiesFor(fishFamily)

        private val seaManager = inject<SeaManager>()
        private val slowThenFast = Interpolation.pow4In
        private val fastThenSlow = Interpolation.fastSlow

        val fishPlayAction = GenericAction("Fish Playing", {
            slowThenFast.apply(MathUtils.norm(0f, 100f, it.fish().fishPlayScore.toFloat() * it.fish().energy)).toDouble()
        }, {
            it.fish().targetTile = null
        }, { entity, deltaTime ->
            val body = entity.body()
            val fish = entity.fish()
            when (fish.targetTile) {
                null -> {
                    fish.targetTile = seaManager.getCurrentTiles().random()
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                }
                body.currentTile() -> {
                    fish.targetTile = null
                    fish.direction.setZero()
                }
                else -> {
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                }
            }
        })

        val fishMatingAction = GenericAction("Fish Mating", {
            if (it.fish().hasMated) 0.0 else {
                val score = MathUtils.norm(0f, FishMaxEnergy, it.fish().energy)
                val newScore = fastThenSlow.apply(0f, 1f, score)
                newScore.toDouble()
            }
        }, {
            it.fish().targetTile = null
        }, { entity, deltaTime ->
            val fish = entity.fish()
            val body = entity.body()
            val currentTile = entity.body().currentTile()
            if (fish.targetTile != null && fish.targetTile != currentTile) {
                //We are going somewhere and we are not there yet.
                fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
            } else if (fish.targetTile == currentTile) {
                fish.targetTile = null
                fish.direction.setZero()// .set(currentTile.worldCenter - body.worldCenter).nor()
                val closestFish = (allTheFish - entity).minByOrNull { it.body().position.dst(body.position) }!!
                if (closestFish.body().currentTile() == currentTile) {
                    if (allTheFish.count() < MaxFishCount) {
                        //MATE! - otherwise just let this repeat itself!
                        val numberOfFish = (1..3).random()
                        AiCounter.eventCounter["Births"] = AiCounter.eventCounter["Births"]!! + numberOfFish
                        for (i in 0 until numberOfFish) {
                            fish(body.position)
                        }
                    }
                    fish.energy = 15f
                    fish.hasMated = true
                }
            } else if (fish.targetTile == null) {
                //1. Are there fish within mating distance that also wish to mate?
                val closestFish = (allTheFish - entity).minByOrNull { it.body().position.dst(body.position) }!!
                fish.targetTile = closestFish.body().currentTile()
            }
        })

        val fishFoodAction = GenericAction("Fish Food",
            {
                val score = 1f - MathUtils.norm(0f, FishMaxEnergy, it.fish().energy)
                val newScore = fastThenSlow.apply(0f, 1f, score)
                newScore.toDouble()
            },
            {
                it.fish().targetTile = null
            },
            { entity, deltaTime ->
                //1. Check the current tile for food
                val body = entity.body()
                val fish = entity.fish()
                val currentTile = body.currentTile()
                val directionRange = -1..1

                if (fish.targetTile != null && fish.targetTile != currentTile) {
                    fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
                } else if (fish.targetTile == currentTile) {
                    if (currentTile.currentFood > 0f) {
                        val eatAmount = deltaTime * FishEatingPace
//                        fish.direction.setZero()
                        fish.energy += eatAmount
                        fish.energy = MathUtils.clamp(fish.energy, 0f, FishMaxEnergy)
                        currentTile.currentFood -= eatAmount
                        currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, TileMaxFood)
                    } else {
                        val foodTiles = currentTile.neighbours.filter { it.currentFood > 0f }

                        if (foodTiles.any())
                            fish.targetTile = foodTiles.random()
                        else {
                            var distance = 2
                            var directionX = directionRange.random()
                            var directionY = directionRange.random()
                            var keepSearching = true
                            var foodTile = currentTile
                            var iterations = 0
                            while (keepSearching) {
                                foodTile = currentTile.someTileAt(distance++, directionX, directionY)
                                keepSearching = foodTile.currentFood <= 0f
                                if(keepSearching && distance > 20 && iterations > 3) {
                                    foodTile = seaManager.allTiles.filter { it.currentFood > 0 }.random()
                                }
                                else if(keepSearching && distance > 10) {
                                    iterations++
                                    distance = 2
                                    directionX = directionRange.random()
                                    directionY = directionRange.random()
                                }
                            }
                            fish.targetTile = foodTile
                        }
                    }
                } else if (fish.targetTile == null) {
                    if (currentTile.currentFood > 0f) {
                        val eatAmount = deltaTime * FishEatingPace
                  //      fish.direction.setZero()
                        fish.energy += eatAmount
                        fish.energy = MathUtils.clamp(fish.energy, 0f, FishMaxEnergy)
                        currentTile.currentFood -= eatAmount
                        currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, TileMaxFood)
                    } else {
                        val foodTiles = currentTile.neighbours.filter { it.currentFood > 0f }

                        if (foodTiles.any())
                            fish.targetTile = foodTiles.random()
                        else {
                            var distance = 2
                            var directionX = directionRange.random()
                            var directionY = directionRange.random()
                            var keepSearching = true
                            var foodTile = currentTile
                            var iterations = 0
                            while (keepSearching) {
                                foodTile = currentTile.someTileAt(distance++, directionX, directionY)
                                keepSearching = foodTile.currentFood <= 0f
                                if(keepSearching && distance > 20 && iterations > 3) {
                                    foodTile = seaManager.allTiles.filter { it.currentFood > 0 }.random()
                                }
                                else if(keepSearching && distance > 10) {
                                    distance = 2
                                    directionX = directionRange.random()
                                    directionY = directionRange.random()
                                    iterations++
                                }
                            }
                            fish.targetTile = foodTile
                        }
                    }
                }
            }
        )

        val defaultActions = setOf(fishPlayAction, fishFoodAction, fishMatingAction)
    }
}