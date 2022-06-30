package moist.ai

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Pool
import ktx.ashley.mapperFor

class UtilityAiComponent : Component, Pool.Poolable {
    val actions = UtilityAiActions.defaultActions.toMutableList()
    private var currentAction: AiAction? = null

    fun updateAction(entity: Entity) {
        actions.sortByDescending { it.score(entity) }
    }

    fun topAction(entity: Entity): AiAction? {
        val potentialAction = actions.first()
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
        actions.addAll(UtilityAiActions.defaultActions)
        currentAction = null
    }

    companion object {
        val mapper = mapperFor<UtilityAiComponent>()
        fun get(entity: Entity): UtilityAiComponent {
            return mapper.get(entity)
        }
    }
}