package moist.ai

import com.badlogic.ashley.core.Entity
import moist.ai.AiAction

abstract class StaticScoreAction(name: String, private val score: Float) : AiAction(name) {
    override fun score(entity: Entity): Double {
        return score.toDouble()
    }
}