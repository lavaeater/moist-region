package eater.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class UtilityAiComponent : Component, Pool.Poolable {
    val actions = mutableListOf<eater.ai.AiAction>()
    private var currentAction: eater.ai.AiAction? = null

    fun updateAction(entity: Entity) {
        actions.sortByDescending { it.score(entity) }
    }

    fun topAction(entity: Entity): eater.ai.AiAction? {
        val potentialAction = actions.first()
        if (currentAction != potentialAction) {
            if (currentAction != null)
                AiCounter.addToCounter(currentAction!!, -1)
            AiCounter.addToCounter(potentialAction, 1)
            currentAction?.abort(entity)
            currentAction = potentialAction
        }
        return currentAction
    }

    override fun reset() {
        actions.clear()
        currentAction = null
    }

    companion object {
        val mapper = mapperFor<UtilityAiComponent>()
        fun get(entity: Entity): UtilityAiComponent {
            return mapper.get(entity)
        }
    }
}