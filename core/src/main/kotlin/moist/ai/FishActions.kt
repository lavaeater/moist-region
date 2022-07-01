package moist.ai

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import ktx.ashley.allOf
import ktx.log.debug
import ktx.math.minus
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants
import moist.core.fish
import moist.ecs.components.Fish
import moist.ecs.components.someTileAt
import moist.ecs.systems.body
import moist.ecs.systems.currentTile
import moist.ecs.systems.fish
import moist.ecs.systems.hasBody
import moist.injection.Context
import moist.world.SeaManager
import moist.world.engine

object FishActions {
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
        val fish = it.fish()
        if (fish.canMate) {
            val score = MathUtils.norm(0f, GameConstants.FishMaxEnergy, fish.energy)
            val matingScore = Interpolation.exp10In.apply(
                MathUtils.norm(
                    0f,
                    GameConstants.MaxFishMatings.toFloat(),
                    GameConstants.MaxFishMatings.toFloat() - fish.matingCount.toFloat()
                )
            )
            val newScore = Interpolation.exp10Out.apply((score + matingScore) / 2f)
            newScore.toDouble()
        } else 0.0
    }, {
        it.fish().targetTile = null
        it.fish().targetFish = null
    }, { entity, deltaTime ->
        val fish = entity.fish()
        if (fish.canMate) {
            val body = entity.body()
            val currentTile = body.currentTile()
            if (fish.targetFish != null && fish.targetFish!!.hasBody() && fish.targetTile != currentTile) {
                fish.targetTile = fish.targetFish!!.body().currentTile()
                debug { "${fish.id} is going to mate at ${fish.targetTile}" }
                //We are going somewhere and we are not there yet.
                fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
            } else if (fish.targetFish != null && !fish.targetFish!!.hasBody()) {
                fish.targetFish = null
            } else if (fish.targetTile == currentTile) {
                debug { "${fish.id} is going to mate at ${fish.targetTile} with ${fish.targetFish?.fish()?.id}" }
                if (allTheFish.count() < GameConstants.MaxFishCount) {
                    //MATE! - otherwise just let this repeat itself!
                    val numberOfFish = (1..3).random()
                    debug { "$numberOfFish were born" }
                    AiCounter.eventCounter["Births"] = AiCounter.eventCounter["Births"]!! + numberOfFish
                    for (i in 0 until numberOfFish) {
                        fish(body.position)
                    }
                }
                fish.energy = 5f
                fish.matingCount++
                fish.targetTile = null
                fish.targetFish = null
            } else if (fish.targetTile == null && fish.targetFish == null) {
                //1. Are there fish within mating distance that also wish to mate?
                val closestFish =
                    (allTheFish - entity).filter { it.fish().canMate }
                        .minByOrNull { it.body().position.dst(body.position) }
                if (closestFish != null) {
                    debug { "${fish.id} is trying to mate with ${closestFish.fish().id}" }
                    fish.targetTile = closestFish.body().currentTile()
                    fish.targetFish = closestFish
                }
            }
        }
    })

    val fishFoodAction = GenericAction("Fish Food",
        {
            val score = 1f - MathUtils.norm(0f, GameConstants.FishMaxEnergy, it.fish().energy)
            val newScore = Interpolation.pow2Out.apply(score)
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
}