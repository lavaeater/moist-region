package eater.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import eater.ai.UtilityAiComponent
import ktx.ashley.allOf

class UpdateActionsSystem : IntervalIteratingSystem(allOf(UtilityAiComponent::class).get(), 1f) {
    override fun processEntity(entity: Entity) {
        val ai = UtilityAiComponent.get(entity)
        ai.updateAction(entity)
    }

}