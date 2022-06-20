package moist.core

import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.log.debug
import ktx.math.vec2
import moist.injection.Context.inject
import moist.input.KeyPress
import moist.input.command
import moist.world.SeaManager
import moist.world.engine

class FirstScreen : KtxScreen, KtxInputAdapter {
    private val image = Texture("logo.png".toInternalFile(), true).apply { setFilter(
        Texture.TextureFilter.Linear,
        Texture.TextureFilter.Linear
    ) }
    private val batch = inject<PolygonSpriteBatch>()
    private val assets = inject<Assets>()
    private var needsInit = true
    private val viewPort: ExtendViewport by lazy { inject() }
    private val camera: OrthographicCamera by lazy { inject() }

    private val movementVector = vec2(0f,0f)
    private var cameraZoom = 0f
    private val zoomFactor = 0.1f
    private val cameraSpeed = 10f

    private val normalCommandMap = command("Normal") {
        setBoth(Input.Keys.W, "Move Up", { movementVector.y = 0f }, { movementVector.y = -1f })
        setBoth(Input.Keys.S, "Move Down", { movementVector.y = 0f }, { movementVector.y = 1f })
        setBoth(Input.Keys.A, "Move Left", { movementVector.x = 0f }, { movementVector.x = -1f })
        setBoth(Input.Keys.D, "Move Right", { movementVector.x = 0f }, { movementVector.x = 1f })
        setBoth(Input.Keys.Z, "Zoom out", { cameraZoom = 0f }, { cameraZoom = 1f })
        setBoth(Input.Keys.X, "Zoom in", { cameraZoom = 0f }, { cameraZoom = -1f })
    }

    override fun keyDown(keycode: Int): Boolean {
        return normalCommandMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return normalCommandMap.execute(keycode, KeyPress.Up)
    }

    override fun show() {
        if(needsInit) {
            needsInit = false
            Gdx.app.logLevel = LOG_DEBUG
            SeaManager.generate()
            Gdx.input.inputProcessor = this
        }
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.1f, green = 0.1f, blue = 0.7f)

        camera.position.add(movementVector.x * cameraSpeed, movementVector.y * cameraSpeed, 0f)
        camera.zoom += zoomFactor * cameraZoom
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined

        engine().update(delta)
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
        assets.disposeSafely()
    }
}