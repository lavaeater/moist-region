package moist.ai

object AiCounter {
    val actionCounter = mutableMapOf<AiAction, Int>(
        UtilityAiActions.fishMatingAction to 0,
        UtilityAiActions.fishPlayAction to 0,
        UtilityAiActions.fishFoodAction to 0
    )
    val eventCounter = mutableMapOf("Births" to 0, "Deaths" to 0)
    var currentState = "Moving"
}