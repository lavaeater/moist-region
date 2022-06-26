package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import moist.ai.UtilityAiComponent

class UtilityAiSystem() : IteratingSystem(allOf(UtilityAiComponent::class).get()) {
    private val utilMapper = mapperFor<UtilityAiComponent>()
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val ai = utilMapper.get(entity)
        ai.topAction(entity)?.act(entity, deltaTime)
    }

    /*
    Infinite Axis Utility System

    range: 0-1

    clamp, then normalize

    Action has list of considerations.

    Multiple considerations (axes) with each other,  get 0..1 value

    Makes a sorted list - take the top one.

    Why the FUCK would you multiply normalized values together,
    when you could just simply use an average? Why not an average?
    WHYYY?

    A consideration should simply return a value between zero and one
     */
}
