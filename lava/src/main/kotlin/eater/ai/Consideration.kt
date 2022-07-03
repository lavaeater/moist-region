package eater.ai

import com.badlogic.ashley.core.Entity

sealed class Consideration(val name: String, val scoreFunction: (entity: Entity) -> Float = { 0f }) {
    open fun normalizedScore(entity: Entity): Float {
        return scoreFunction(entity)
    }

//    object MyHealthConsideration: Consideration("My Health", { entity ->
//        val attackables = entity.attackables()
//        attackables.health / attackables.maxHealth
//    })
}