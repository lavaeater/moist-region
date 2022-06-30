package moist.ai

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants
import moist.ecs.components.Fish
import moist.ecs.components.fish
import moist.ecs.components.someTileAt
import moist.ecs.systems.body
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish
import moist.injection.Context
import moist.world.SeaManager
import moist.world.engine

object UtilityAiActions {
    private val fishFamily = allOf(Fish::class).get()
    private val allTheFish get() = engine().getEntitiesFor(fishFamily)

    private val seaManager = Context.inject<SeaManager>()

    val fishPlayAction = GenericAction("Fish Playing", {
        Interpolation.fastSlow.apply(MathUtils.norm(0f, 100f, it.fish().fishPlayScore.toFloat() * it.fish().energy))
            .toDouble()
    }, {
        it.fish().targetTile = null
    }, { entity, deltaTime ->
        val body = entity.body()
        val fish = entity.fish()
        when (fish.targetTile) {
            null -> {
                fish.targetTile = (seaManager.getCurrentTiles()).random()
                fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
            }
            body.currentTile() -> {
                fish.targetTile = null
            }
            else -> {
                fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
            }
        }
    })

    val fishMatingAction = GenericAction("Fish Mating", {
        if (it.fish().hasMated) 0.0 else {
            val score = MathUtils.norm(0f, GameConstants.FishMaxEnergy, it.fish().energy)
            val newScore = Interpolation.exp10Out.apply(score)
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

            val closestFish = (allTheFish - entity).minByOrNull { it.body().position.dst(body.position) }!!
            if (closestFish.body().currentTile() == currentTile) {
                if (allTheFish.count() < GameConstants.MaxFishCount) {
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
            val closestFish = (allTheFish - entity).minByOrNull { it.body().position.dst(body.position) }
            if (closestFish != null)
                fish.targetTile = closestFish.body().currentTile()
        }
    })

    val fishFoodAction = GenericAction("Fish Food",
        {
            val score = 1f - MathUtils.norm(0f, GameConstants.FishMaxEnergy, it.fish().energy)
            val newScore = Interpolation.pow4Out.apply(score)
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
                val offsetRange = -(GameConstants.TileSize / 2f)..(GameConstants.TileSize / 2f)

                fish.direction.lerp(
                    (fish.targetTile!!.worldCenter + vec2(
                        offsetRange.random(),
                        offsetRange.random()
                    )) - body.worldCenter, 0.1f
                ).nor()
                if (currentTile.currentFood > GameConstants.FishEatingPace) {
                    val eatAmount = deltaTime * GameConstants.FishEatingPace
                    fish.energy += eatAmount
                    fish.energy = MathUtils.clamp(fish.energy, 0f, GameConstants.FishMaxEnergy)
                    currentTile.currentFood -= eatAmount
                    currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, GameConstants.TileMaxFood)
                } else {
                    fish.targetTile = null
                }
            } else if (fish.targetTile == null) {
                var distance = 2
                var directionX = directionRange.random()
                var directionY = directionRange.random()
                if (directionX == 0 && directionY == 0) {
                    directionY = -1
                }
                var keepSearching = true
                var foodTile = currentTile
                var iterations = 0
                while (keepSearching) {
                    foodTile = currentTile.someTileAt(distance++, directionX, directionY)
                    keepSearching = foodTile.currentFood <= GameConstants.FishEatingPace
                    if (keepSearching && distance > 20 && iterations > 3) {
                        foodTile = seaManager.getCurrentTiles().filter { it.currentFood > GameConstants.FishEatingPace }
                            .random()
                    } else if (keepSearching && distance > 10) {
                        distance = 2
                        directionX = directionRange.random()
                        directionY = directionRange.random()
                        if (directionX == 0 && directionY == 0) {
                            directionX = -1
                        }
                        iterations++
                    }
                }
                fish.targetTile = foodTile
            }
        }
    )

    val defaultActions = setOf(fishPlayAction, fishFoodAction, fishMatingAction)
}