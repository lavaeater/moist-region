package moist.ai

import com.badlogic.ashley.core.Entity
import moist.ai.AiAction

class GenericAction(
    name: String,
    private val scoreFunction: (entity: Entity) -> Double,
    private val abortFunction: (entity: Entity) -> Unit,
    private val actFunction: (entity: Entity, deltaTime:Float) -> Unit): AiAction(name) {
    override fun abort(entity: Entity) {
        abortFunction(entity)
    }

    override fun act(entity: Entity, deltaTime: Float) {
        actFunction(entity, deltaTime)
    }

    override fun score(entity: Entity): Double {
        return scoreFunction(entity)
    }
}