package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import ktx.ashley.allOf
import moist.ai.UtilityAiComponent

class UpdateActionsSystem : IntervalIteratingSystem(allOf(UtilityAiComponent::class).get(), 1f) {
    override fun processEntity(entity: Entity) {
        val ai = UtilityAiComponent.get(entity)
        ai.updateAction(entity)
    }

}