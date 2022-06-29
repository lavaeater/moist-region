package moist.ai

object AiCounter {
    val actionCounter = mutableMapOf<AiAction, Int>(
        UtilityAiComponent.fishMatingAction to 0,
        UtilityAiComponent.fishPlayAction to 0,
        UtilityAiComponent.fishFoodAction to 0
    )
    val eventCounter = mutableMapOf("Births" to 0, "Deaths" to 0)
    var currentState = "Moving"
}