package moist.core

import eater.core.MainGame

class MoistGame : MainGame() {
    override fun create() {
        addScreen(SplashScreen(this))
        addScreen(GameScreen(this))
        addScreen(GameOverScreen(this))
    }
}

