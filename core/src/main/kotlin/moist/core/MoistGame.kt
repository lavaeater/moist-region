package moist.core

import eater.core.MainGame
import moist.injection.Context

class MoistGame : MainGame() {
    override fun create() {
        Context.initialize()
        addScreen(SplashScreen(this))
        addScreen(GameScreen(this))
        addScreen(GameOverScreen(this))
        setScreen<SplashScreen>()
    }
}

