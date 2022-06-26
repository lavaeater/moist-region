package moist.core

import com.badlogic.ashley.core.Entity
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
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.log.debug
import ktx.math.vec2
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.TileSize
import moist.ecs.components.*
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.injection.Context.inject
import moist.input.KeyPress
import moist.input.command
import moist.world.ChunkKey
import moist.world.TileChunk
import moist.world.engine

class GameScreen(val mainGame: MainGame) : KtxScreen, KtxInputAdapter {
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

    private lateinit var cityEntity: Entity
    private val cityComponent get() = cityEntity.city()

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
            { sailRotation = -1f })
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
        cityEntity = city()
        for(system in engine().systems)
            system.setProcessing(true)
        sea()
        fishes()
        checkGameConditions()

        Gdx.input.inputProcessor = this
        viewPort.minWorldHeight = MaxTilesPerSide.toFloat() * TileSize
        viewPort.minWorldWidth = MaxTilesPerSide.toFloat() * TileSize
        viewPort.update(Gdx.graphics.width, Gdx.graphics.height)
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
        engine().update(delta)
        hud.render(delta)

        checkGameConditions()
    }

    val bodyFamily = allOf(Box::class).get()
    val allBodies get() =  engine().getEntitiesFor(bodyFamily)

    private fun checkGameConditions() {
        if (cityComponent.population < 50) {
            GameStats.population = cityComponent.population.toInt()
            GameStats.remainingFood = cityComponent.food.toInt()

            for(entity in allBodies) {
                world.destroyBody(entity.body())
                entity.remove<Box>()
            }

            for(system in engine().systems)
                system.setProcessing(false)
            engine().removeAllEntities()
            mainGame.setScreen<GameOverScreen>()
        }
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

object GameStats {
    var population = 0
    var maxPopulation = 0
    var remainingFood = 0
    var caughtFish = 0
}