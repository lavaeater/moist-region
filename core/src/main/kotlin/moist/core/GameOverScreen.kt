package moist.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.actors.stage
import ktx.math.vec2
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.label
import moist.input.command

class GameOverScreen(mainGame: MainGame) : BasicScreen(mainGame, command("Normal") {
    setDown(Input.Keys.SPACE, "Start Game") {
        GameStats.caughtFish = 0
        GameStats.population = 0
        GameStats.maxPopulation = 0
        GameStats.remainingFood = 0
        GameStats.playTime = 0f
        mainGame.setScreen<GameScreen>()
    }
}) {

    override val viewport: Viewport by lazy { ExtendViewport(360f, 240f, camera) }

    override fun show() {
        super.show()
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)

    }

    private val stage by lazy {
        stage(batch, viewport).apply {
            actors {
                val currentPos = vec2(this@apply.width / 2f - 50f, this@apply.height / 2f + 50f)
                label("MOIST REGION") {
                    setFontScale(2f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2
                    currentPos.x -= 100f
                }
                label("GAME OVER") {
                    setFontScale(2f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2
                }
                label("Fish the fish with your floating city (theme, you know)") {
                    setFontScale(0.6f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2f
                }
                label("Steer the sail with A and D - ") {
                    setFontScale(0.6f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2f
                }
                label("the Red Dial shows where you're headed") {
                    setFontScale(0.6f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 7f
                }
                label("""You caught ${GameStats.caughtFish} fish
                    You maxed your population at ${GameStats.maxPopulation}
                    The game ended when you had ${GameStats.remainingFood} food remainging.
                    You played for ${GameStats.playTime} seconds 
                    Longest playtime is ${GameStats.highestPlayTime} seconds
                """.trimMargin()){
                    setFontScale(0.6f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2f
                }


            }
        }
    }

    override fun render(delta: Float) {
        super.render(delta)
        stage.act(delta)
        stage.draw()

    }

}