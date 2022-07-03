package eater.injection

import ktx.inject.Context

object Context {
    val context = Context()
    inline fun <reified T> inject(): T {
        return context.inject()
    }

    /**
     * Call this method with your context building needs.
     *
     */
    fun buildContext(init: Context.() -> Unit) {
        context.init()
    }
}