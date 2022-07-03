package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool.Poolable
import eater.ai.GenericActionWithState
import eater.core.engine
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.log.debug
import ktx.math.minus
import moist.core.GameConstants
import moist.core.GameConstants.SharkMaxVelocity
import moist.core.GameConstants.TileSize
import moist.core.shark
import moist.ecs.components.*
import moist.ecs.systems.body
import moist.ecs.systems.creature
import moist.ecs.systems.hasBody
import moist.world.SeaManager
import moist.world.currentTile

class SharkMating : Component, Poolable {
    var targetShark: Entity? = null
    var matingState: MatingState = MatingState.NotStarted
        set(value) {
            if (field != value)
                debug { "${field.name} -> ${value.name}" }
            field = value
        }

    override fun reset() {
        targetShark = null
        matingState = MatingState.NotStarted
    }
}

sealed class MatingState(val name: String) {
    object NotStarted : MatingState("Not Started")
    object LookingForAMate : MatingState("Looking for a mate")
    object SwimmingTowardsAMate : MatingState("Swimming Towards a Mate")
    object JustMated : MatingState("Just Mated")
}

sealed class HuntingState(val name: String) {
    object NotStarted : HuntingState("Not Started")
    object LookingForPrey : HuntingState("Searching For Prey")
    object HuntingPrey : HuntingState("Hunting Prey")
    object GoingToANewPlace : HuntingState("Going to a New Place")
}

class SharkHunting : Component, Poolable {
    var targetTile: SeaTile? = null
    var targetFish: Entity? = null
    var huntingState: HuntingState = HuntingState.NotStarted
        set(value) {
            if (field != value)
                debug { "${field.name} -> ${value.name}" }
            field = value
        }

    override fun reset() {
        huntingState = HuntingState.NotStarted
        targetFish = null
        targetTile = null
    }

}

object SharkActions {
    val seaManager by lazy { inject<SeaManager>() }
    private val fishFamily = allOf(Fish::class).get()
    val allFish get() = engine().getEntitiesFor(fishFamily)

    val huntingAction = GenericActionWithState("Shark Hunting", {
        val score = 1f - MathUtils.norm(0f, GameConstants.FishMaxEnergy, it.creature().energy)
        val newScore = Interpolation.pow2Out.apply(score)
        newScore.toDouble()
    }, {
        debug { "ABort mating" }
    }, { entity, state, deltaTime ->

        /*
        1. What state are we in?
         */
        when (state.huntingState) {
            HuntingState.HuntingPrey -> {
                if (state.targetFish != null) {
                    if (state.targetFish!!.hasBody()) {
                        val sharkBody = entity.body()
                        val creatureStats = entity.creature()
                        val fishBody = state.targetFish!!.body()
                        val fishFish = state.targetFish!!.creature()
                        if (fishBody.position.dst(sharkBody.position) < 15f) {
                            //This shark will now eat this fish.
                            creatureStats.energy += fishFish.foodValue
                            fishFish.energy = -1000f // This fish will now die
                            state.targetFish = null
                            state.targetTile = null
                            state.huntingState = HuntingState.LookingForPrey
                        } else {
                            val distance = fishBody.position.dst(sharkBody.position)
                            val speedFactor = if(distance < 75f) 2.5f else if(distance < 150f) 1.25f else 1.0f
                            creatureStats.fishMaxVelocity = SharkMaxVelocity * speedFactor
                            creatureStats.direction.lerp(fishBody.position - sharkBody.position, 0.25f)
                        }
                    } else {
                        /*
                        The target fish might have died.
                         */
                        state.targetFish = null
                        state.huntingState = HuntingState.LookingForPrey
                    }
                } else {
                    state.huntingState = HuntingState.LookingForPrey
                }
            }
            HuntingState.LookingForPrey -> {
                //1. find some fish
                /**
                 * I want the fish to be in front of the shark.
                 * I want the shark to be able to...
                 *
                 * OK, here's what we do. Check if any fish are closer than like, three squares
                 */
                /**
                 * I want the fish to be in front of the shark.
                 * I want the shark to be able to...
                 *
                 * OK, here's what we do. Check if any fish are closer than like, three squares
                 */
                state.targetFish = null
                state.targetTile = null
                val sharkBody = entity.body()
                val sharkPos = sharkBody.position
                val checkDistance = TileSize * 4f
                val potentialFishes =
                    allFish.filter { it.body().position.dst(sharkPos) < checkDistance }//.minByOrNull { it.body().position.dst(sharkPos) }
                if (potentialFishes.any()) {
                    state.huntingState = HuntingState.HuntingPrey
                    state.targetFish = potentialFishes.maxByOrNull { it.creature().size }!!
                } else {
                    /**
                     * How about we go towards other sharks, just in case they know what's up?
                     */

                    /**
                     * How about we go towards other sharks, just in case they know what's up?
                     */

                    val ps = allSharks.filter { it.body().position.dst(sharkBody.position) < checkDistance * 8 }
                    val targetTile = if(ps.any()) ps.random().body().currentTile() else seaManager.allTiles.random()
                    state.targetTile = targetTile
                    state.huntingState = HuntingState.GoingToANewPlace
                }
            }
            HuntingState.NotStarted -> state.huntingState = HuntingState.LookingForPrey
            HuntingState.GoingToANewPlace -> {
                val sharkBody = entity.body()
                if (state.targetTile != null && state.targetTile != sharkBody.currentTile()) {
                    val fish = entity.creature()
                    fish.fishMaxVelocity = SharkMaxVelocity / 3f
                    fish.direction.lerp(state.targetTile!!.worldCenter - sharkBody.position, 0.1f)
                } else {
                    state.targetTile = null
                    state.huntingState = HuntingState.LookingForPrey
                }
            }
        }


    }, SharkHunting::class.java)

    val sharkFamily = allOf(Shark::class, Box2d::class).get()
    val allSharks get() = engine().getEntitiesFor(sharkFamily)

    val matingAction = GenericActionWithState("Shark Mating", {
        val thisCreature = it.creature()
        val score = MathUtils.norm(0f, GameConstants.FishMaxEnergy, thisCreature.energy)
        val matingScore = Interpolation.exp10In.apply(
            MathUtils.norm(
                0f,
                GameConstants.MaxFishMatings.toFloat(),
                GameConstants.MaxFishMatings.toFloat() - thisCreature.matingCount.toFloat()
            )
        )
        val newScore = Interpolation.pow2Out.apply((score + matingScore) / 2f)
        newScore.toDouble()
    }, {
        it.creature().availableForMating = true
        debug { "ABort mating" }
    }, { entity, state, deltaTime ->
        when (state.matingState) {
            MatingState.LookingForAMate -> {
                val sharkBody = entity.body()
                val mapper = mapperFor<SharkMating>()
                val potentialMates =
                    (allSharks - entity).filter { it.creature().availableForMating && mapper.has(it) && mapper.get(it).matingState == MatingState.LookingForAMate}
                if (potentialMates.any()) {
                    val targetShark = potentialMates.minByOrNull { it.body().position.dst(sharkBody.position) }!!
                    val mateState = mapper.get(targetShark)
                    mateState.targetShark = entity
                    mateState.matingState = MatingState.SwimmingTowardsAMate
                    state.targetShark = targetShark
                    state.matingState = MatingState.SwimmingTowardsAMate
                }
            }
            MatingState.NotStarted -> {
                entity.creature().availableForMating = true
                state.matingState = MatingState.LookingForAMate
            }
            MatingState.SwimmingTowardsAMate -> {
                if (state.targetShark != null) {
                    if (state.targetShark!!.hasBody()) {
                        val sharkBody = entity.body()
                        val sharkFish = entity.creature()
                        val mateBody = state.targetShark!!.body()
                        val mateFish = state.targetShark!!.creature()
                        if (mateBody.position.dst(sharkBody.position) < 15f) {
                            //This shark will now mate with this shark.
                            state.targetShark = null
                            state.matingState = MatingState.JustMated
                            shark(sharkBody.position)
                            sharkFish.matingCount += 1
                            sharkFish.availableForMating = false
                            sharkFish.energy = 15f
                            mateFish.energy = 15f
                            mateFish.matingCount += 1
                            mateFish.availableForMating = false
                        } else {
                            sharkFish.fishMaxVelocity = SharkMaxVelocity / 3f
                            sharkFish.direction.lerp(mateBody.position - sharkBody.position, 0.1f)
                        }
                    } else {
                        /*
                        The target fish might have died.
                         */
                        state.targetShark = null
                        state.matingState = MatingState.LookingForAMate
                    }
                } else {
                    state.matingState = MatingState.LookingForAMate
                }
            }
            MatingState.JustMated -> {}
        }
    }, SharkMating::class.java)
}