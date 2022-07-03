package eater.ai

object AiCounter {
    fun addToCounter(action: eater.ai.AiAction, value: Int) {
        if(!actionCounter.containsKey(action)) {
            actionCounter[action] = value
        } else {
            actionCounter[action] = actionCounter[action]!! + value
        }

    }
    val actionCounter = mutableMapOf<eater.ai.AiAction, Int>()
    val eventCounter = mutableMapOf("Births" to 0, "Deaths" to 0)
    var currentState = "Moving"
}