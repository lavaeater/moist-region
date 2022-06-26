package moist.core

import com.badlogic.gdx.Application.LOG_DEBUG
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.log.debug
import ktx.math.times
import ktx.math.vec2
import moist.core.GameConstants.ControlMagnitude
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.TileSize
import moist.ecs.components.Hud
import moist.ecs.components.city
import moist.ecs.components.fishes
import moist.ecs.components.sea
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.injection.Context.inject
import moist.input.KeyPress
import moist.input.command
import moist.world.ChunkKey
import moist.world.TileChunk
import moist.world.engine

class FirstScreen : KtxScreen, KtxInputAdapter {
    private val image = Texture("logo.png".toInternalFile(), true).apply {
        setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        )
    }
    private val batch = inject<PolygonSpriteBatch>()
    private val assets = inject<Assets>()
    private var needsInit = true
    private val viewPort: ExtendViewport by lazy { inject() }
    private val camera: OrthographicCamera by lazy { inject() }

    private val movementVector = vec2(0f, 0f)
    private var cameraZoom = 0f
    private val zoomFactor = 0.1f
    private val cameraSpeed = 10f

    private val velIters = 8
    private val posIters = 3
    private val timeStep = 1 / 60f
    private var accumulator = 0f
    private val world by lazy { inject<World>() }

    private val cityEntity by lazy { city() }
    private val cityComponent by lazy { cityEntity.city() }

    private var sailRotation = 0f
    private val hud by lazy { inject<Hud>() }


    private val normalCommandMap = command("Normal") {
        setBoth(Input.Keys.W, "Move Up", { movementVector.y = 0f }, { movementVector.y = 1f })
        setBoth(Input.Keys.S, "Move Down", { movementVector.y = 0f }, { movementVector.y = -1f })
        setBoth(
            Input.Keys.A,
            "Move Left",
            { sailRotation = 0f },
            { sailRotation = 1f })
        setBoth(
            Input.Keys.D,
            "Move Right",
            { sailRotation = 0f },
            { sailRotation = - 1f })
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
        if (needsInit) {
            needsInit = false
            Gdx.app.logLevel = LOG_DEBUG
            for (x in (-4..-3))
                for (y in 2..3) {
                    val ck = ChunkKey.keyForTileCoords(x, y)
                    val chunk = TileChunk(ck)
                    debug { "Chunk: $x:$y:$ck" }
                    debug { "Local: " + chunk.localX(x) + ":" + chunk.localY(y) }
                    debug { "Index: " + chunk.getIndex(chunk.localX(x), chunk.localY(y)) }
                    val tile = chunk.getTileAt(x, y)
                    debug { "T: " + tile.x.toString() + ":" + tile.y.toString() }
                }



            sea()
            fishes()
            Gdx.input.inputProcessor = this
            viewPort.minWorldHeight = MaxTilesPerSide.toFloat() * TileSize
            viewPort.minWorldWidth = MaxTilesPerSide.toFloat() * TileSize
        }
    }

    override fun resize(width: Int, height: Int) {
        viewPort.update(width, height)
        batch.projectionMatrix = camera.combined
    }


    private fun updatePhysics(delta: Float) {
        val ourTime = delta.coerceAtMost(timeStep * 2)
        accumulator += ourTime
        while (accumulator > timeStep) {
            world.step(timeStep, velIters, posIters)
            accumulator -= ourTime
        }
    }

    override fun render(delta: Float) {
        clearScreen(red = 0.1f, green = 0.1f, blue = 0.7f)
        updatePhysics(delta)

        applyInput()

        camera.zoom += zoomFactor * cameraZoom
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined
//        hud.render(delta)
        engine().update(delta)
    }

    private fun applyInput() {
        cityComponent.sailVector.setAngleDeg(cityComponent.sailVector.angleDeg() + sailRotation)
//        val cityBody = cityEntity.body()
//        cityBody.applyForceToCenter(movementVector * ControlMagnitude, true)
    }

    override fun dispose() {
        image.disposeSafely()
        batch.disposeSafely()
        assets.disposeSafely()
    }
}