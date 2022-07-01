package moist.ai

object AiCounter {
    fun addToCounter(action: AiAction, value: Int) {
        if(!actionCounter.containsKey(action)) {
            actionCounter[action] = value
        } else {
            actionCounter[action] = actionCounter[action]!! + value
        }

    }
    val actionCounter = mutableMapOf<AiAction, Int>()
    val eventCounter = mutableMapOf("Births" to 0, "Deaths" to 0)
    var currentState = "Moving"
}