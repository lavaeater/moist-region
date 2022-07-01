package moist.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.actors.stage
import ktx.math.vec2
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.label
import moist.ecs.components.GlobalDebug
import moist.input.command

class SplashScreen(mainGame: MainGame) : BasicScreen(mainGame, command("Normal") {
    setDown(Input.Keys.SPACE, "Start Game") {
        mainGame.setScreen<GameScreen>()
    }
    setDown(Input.Keys.ENTER, "Enable Debug Features") {
    GlobalDebug.globalDebug = true
}
}) {

    override val viewport: Viewport by lazy { ExtendViewport(360f, 240f, OrthographicCamera()) }

    override fun show() {
        super.show()
        Scene2DSkin.defaultSkin = Skin(Gdx.files.internal("ui/uiskin.json"))
    }

    private val stage by lazy {
        stage(batch, viewport).apply {
            actors {
                val currentPos = vec2(this@apply.width / 2f - 50f, this@apply.height / 2f + 50f)
                label("MOIST REGION") {
                    setSize(72f, 24f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 2
                    currentPos.x -= 100f
                }
                label("Fish the fish with your floating city's nets - just sail onto the fish.") {
                    setFontScale(0.75f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 1.5f
                }
                label("Keep population above 10 at all times!") {
                    setFontScale(0.75f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 1.5f
                }
                label("Steer the sail with A and D - your speed is indicated by the red dial") {
                    setFontScale(0.75f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 1.5f
                }
                label("the white dial shows the direction of the wind") {
                    setFontScale(0.75f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 1.5f
                }
                label("PRESS SPACE TO BEGIN") {
                    setFontScale(0.75f)
                    setPosition(currentPos.x, currentPos.y)
                    currentPos.y -= this.height * 1.5f
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