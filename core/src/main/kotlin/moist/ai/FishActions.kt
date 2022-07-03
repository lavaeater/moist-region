package moist.ai

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import eater.ai.GenericAction
import eater.core.engine
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.math.minus
import ktx.math.plus
import ktx.math.random
import ktx.math.vec2
import moist.core.GameConstants
import moist.core.fish
import moist.ecs.components.Fish
import moist.ecs.systems.body
import moist.ecs.systems.creature
import moist.ecs.systems.hasBody
import moist.world.SeaManager
import moist.world.currentTile

object FishActions {
    private val fishFamily = allOf(Fish::class).get()
    private val allTheFish get() = engine().getEntitiesFor(fishFamily)

    private val seaManager = inject<SeaManager>()

    val fishPlayAction = GenericAction("Fish Playing", {
        Interpolation.fastSlow.apply(
            MathUtils.norm(
                0f,
                100f,
                it.creature().fishPlayScore.toFloat() * it.creature().energy
            )
        )
            .toDouble()
    }, {
        it.creature().targetTile = null
    }, { entity, deltaTime ->
        val body = entity.body()
        val creature = entity.creature()
        when (creature.targetTile) {
            null -> {
                creature.targetTile = (seaManager.getCurrentTiles()).random()
                creature.direction.set(creature.targetTile!!.worldCenter - body.worldCenter).nor()
            }
            body.currentTile() -> {
                creature.targetTile = null
            }
            else -> {
                creature.direction.set(creature.targetTile!!.worldCenter - body.worldCenter).nor()
            }
        }
    })

    val fishMatingAction = GenericAction("Fish Mating", {
        val creature = it.creature()
        if (creature.availableForMating) {
            val score = MathUtils.norm(0f, GameConstants.FishMaxEnergy, creature.energy)
            val matingScore = Interpolation.exp10In.apply(
                MathUtils.norm(
                    0f,
                    GameConstants.MaxFishMatings.toFloat(),
                    GameConstants.MaxFishMatings.toFloat() - creature.matingCount.toFloat()
                )
            )
            val newScore = Interpolation.pow2Out.apply((score + matingScore) / 2f)
            newScore.toDouble()
        } else 0.0
    }, {
        it.creature().availableForMating = false
        it.creature().targetTile = null
        it.creature().targetFish = null
    }, { entity, deltaTime ->
        val fish = entity.creature()
        fish.availableForMating = true
        val body = entity.body()
        val currentTile = body.currentTile()
        if (fish.targetFish != null && fish.targetFish!!.hasBody() && fish.targetTile != currentTile) {
            fish.targetTile = fish.targetFish!!.body().currentTile()
            fish.direction.set(fish.targetTile!!.worldCenter - body.worldCenter).nor()
        } else if (fish.targetFish != null && !fish.targetFish!!.hasBody()) {
            fish.targetFish = null
        } else if (fish.targetTile == currentTile) {
            if (allTheFish.count() < GameConstants.MaxFishCount) {
                //MATE! - otherwise just let this repeat itself!
                val numberOfFish = (1..3).random()
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
            val closestFish =
                (allTheFish - entity).filter { it.creature().availableForMating }
                    .minByOrNull { it.body().position.dst(body.position) }
            if (closestFish != null) {
                fish.targetTile = closestFish.body().currentTile()
                fish.targetFish = closestFish
            }
        }

    })

    val fishFoodAction = GenericAction("Fish Food",
        {
            val score = 1f - MathUtils.norm(0f, GameConstants.FishMaxEnergy, it.creature().energy)
            val newScore = Interpolation.exp10Out.apply(score)
            newScore.toDouble()
        },
        {
            it.creature().targetTile = null
        },
        { entity, deltaTime ->
            //1. Check the current tile for food
            val body = entity.body()
            val creature = entity.creature()
            val currentTile = body.currentTile()
            val directionRange = -1..1

            if (creature.targetTile != null && creature.targetTile != currentTile) {
                creature.direction.set(creature.targetTile!!.worldCenter - body.worldCenter).nor()
            } else if (creature.targetTile == currentTile) {
                val offsetRange = -(GameConstants.TileSize / 2f)..(GameConstants.TileSize / 2f)

                creature.direction.lerp(
                    (creature.targetTile!!.worldCenter + vec2(
                        offsetRange.random(),
                        offsetRange.random()
                    )) - body.worldCenter, 0.1f
                ).nor()
                if (currentTile.currentFood > GameConstants.FishEatingPace) {
                    val eatAmount = deltaTime * GameConstants.FishEatingPace
                    creature.energy += eatAmount
                    creature.energy = MathUtils.clamp(creature.energy, 0f, GameConstants.FishMaxEnergy)
                    currentTile.currentFood -= eatAmount
                    currentTile.currentFood = MathUtils.clamp(currentTile.currentFood, 0f, GameConstants.TileMaxFood)
                } else {
                    creature.targetTile = null
                }
            } else if (creature.targetTile == null) {
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
                    foodTile = seaManager.someTileAt(currentTile, distance++, directionX, directionY)
                    keepSearching = foodTile.currentFood <= GameConstants.FishEatingPace
                    if (keepSearching && distance > 20 && iterations > 3) {
                        foodTile = seaManager.getCurrentTiles().filter { it.currentFood > GameConstants.FishEatingPace }
                            .random()
                        keepSearching = false
                    } else if (keepSearching && distance > 10) {
                        distance = 2
                        directionX = directionRange.random()
                        directionY = directionRange.random()
                        if (directionX == 0 && directionY == 0) {
                            directionX = -1
                        }
                        iterations++
                    } else if (iterations > 100) {
                        foodTile = seaManager.allTiles.filter { it.currentFood > GameConstants.FishEatingPace }
                            .random()
                        keepSearching = false
                    }
                }
                creature.targetTile = foodTile
            }
        }
    )
}