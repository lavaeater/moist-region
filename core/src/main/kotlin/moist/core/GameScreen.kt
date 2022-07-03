package moist.core

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Application.LOG_INFO
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.MainGame
import eater.core.SelectedItemList
import eater.core.engine
import eater.core.selectedItemListOf
import eater.ecs.components.Box2d
import eater.ecs.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import eater.input.KeyPress
import eater.input.command
import ktx.app.KtxInputAdapter
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.ashley.addComponent
import ktx.ashley.allOf
import ktx.ashley.getSystem
import ktx.ashley.remove
import ktx.assets.disposeSafely
import ktx.math.vec2
import ktx.preferences.get
import ktx.preferences.set
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.TileSize
import moist.ecs.components.*
import moist.ecs.systems.CityHungerSystem
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.ui.Hud

class GameScreen(val mainGame: MainGame) : KtxScreen, KtxInputAdapter {
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

    private val fishFamily = allOf(Fish::class).get()
    private val sharkFamily = allOf(Shark::class).get()


    private val normalCommandMap = command("Normal") {
        if (GlobalDebug.globalDebug) {
            setUp(Input.Keys.F, "Next Fish", { nextFish() })
            setUp(Input.Keys.G, "Prev Fish", { previousFish() })
            setUp(Input.Keys.S, "Next Shark", { nextShark() })
            setUp(Input.Keys.W, "PRev Shark", { previousShark() })
            setUp(Input.Keys.C, "Back To City", { backToCity() })
        }
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

    private fun backToCity() {
        cityEntity.addComponent<CameraFollow>(engine())
        trackedEntity?.remove<CameraFollow>()
        trackedEntity = cityEntity
    }

    private fun previousShark() {
        fixSharkList()
        val newTracked = selectedSharkList.previousItem()
        newTracked.addComponent<CameraFollow>(engine())
        trackedEntity?.remove<CameraFollow>()
        trackedEntity = newTracked

    }

    private fun fixSharkList() {
        if (!::selectedSharkList.isInitialized)
            selectedSharkList = selectedItemListOf()

        selectedSharkList.clear()
        selectedSharkList.addAll(engine().getEntitiesFor(sharkFamily))
    }

    private var trackedEntity: Entity? = null
    private fun nextShark() {
        fixSharkList()
        val newTracked = selectedSharkList.nextItem()
        newTracked.addComponent<CameraFollow>(engine())
        trackedEntity?.remove<CameraFollow>()
        trackedEntity = newTracked
    }

    private fun previousFish() {
        fixFishList()
        val newTracked = selectedFishList.previousItem()
        newTracked.addComponent<CameraFollow>(engine())
        trackedEntity?.remove<CameraFollow>()
        trackedEntity = newTracked
    }


    lateinit var selectedFishList: SelectedItemList<Entity>
    lateinit var selectedSharkList: SelectedItemList<Entity>

    private fun nextFish() {
        fixFishList()
        val newTracked = selectedFishList.nextItem()
        newTracked.addComponent<CameraFollow>(engine())
        trackedEntity?.remove<CameraFollow>()
        trackedEntity = newTracked
    }

    private fun fixFishList() {
        if (!::selectedFishList.isInitialized)
            selectedFishList = selectedItemListOf()
        selectedFishList.clear()
        selectedFishList.addAll(engine().getEntitiesFor(fishFamily))
    }

    override fun keyDown(keycode: Int): Boolean {
        return normalCommandMap.execute(keycode, KeyPress.Down)
    }

    override fun keyUp(keycode: Int): Boolean {
        return normalCommandMap.execute(keycode, KeyPress.Up)
    }

    override fun show() {
        cityEntity = city(true)
        for (system in engine().systems)
            system.setProcessing(true)
        sea()
        fishes()
        sharks(false)
        checkGameConditions()

        Gdx.input.inputProcessor = this
        Gdx.app.logLevel = LOG_INFO
        viewPort.minWorldHeight = MaxTilesPerSide.toFloat() * TileSize
        viewPort.minWorldWidth = MaxTilesPerSide.toFloat() * TileSize
        viewPort.update(Gdx.graphics.width, Gdx.graphics.height)
        engine().getSystem<CityHungerSystem>().setProcessing(!GlobalDebug.globalDebug)
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

        if (cameraZoom != 0f)
            camera.zoom = MathUtils.lerp(camera.zoom, camera.zoom + zoomFactor * cameraZoom, 0.1f)
        camera.update(false) //True or false, what's the difference?
        batch.projectionMatrix = camera.combined
        engine().update(delta)
        hud.render(delta)

        GameStats.playTime += delta
        checkGameConditions()
    }

    val bodyFamily = allOf(Box2d::class).get()
    val allBodies get() = engine().getEntitiesFor(bodyFamily)

    private fun checkGameConditions() {
        if (cityComponent.population < 10) {
            GameStats.population = cityComponent.population.toInt()
            GameStats.remainingFood = cityComponent.food.toInt()
            if (GameStats.playTime > GameStats.highestPlayTime)
                GameStats.highestPlayTime = GameStats.playTime

            for (entity in allBodies) {
                world.destroyBody(entity.body())
                entity.remove<Box2d>()
            }

            for (system in engine().systems)
                system.setProcessing(false)
            engine().removeAllEntities()
            mainGame.setScreen<GameOverScreen>()
        }
    }

    private fun applyInput() {
        cityComponent.sailVector.setAngleDeg(cityComponent.sailVector.angleDeg() + sailRotation)
    }

    override fun dispose() {
        batch.disposeSafely()
        assets.disposeSafely()
    }
}

object GameStats {
    var deadSharks = 0
    var deadFish = 0
    val preferences = Gdx.app.getPreferences("moist-region")
    var population = 0
    var maxPopulation = 0
    var remainingFood = 0
    var caughtFish = 0
    var playTime = 0f
    var highestPlayTime
        get() = preferences["highestPlayTime", 0f]
        set(value) {
            preferences["highestPlayTime"] = value
        }
}