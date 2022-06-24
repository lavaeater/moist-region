package moist.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.box2d.createWorld
import ktx.inject.Context
import ktx.inject.register
import moist.ai.UtilityAiSystem
import moist.core.Assets
import moist.core.GameConstants.GameHeight
import moist.core.GameConstants.GameWidth
import moist.ecs.systems.*
import moist.world.SeaManager

object Context {
    val context = Context()

    init {
        buildContext()
    }

    inline fun <reified T> inject(): T {
        return context.inject()
    }

    private fun buildContext() {
        context.register {
            bindSingleton(PolygonSpriteBatch())
            bindSingleton(OrthographicCamera())
            bindSingleton(
                ExtendViewport(
                    GameWidth,
                    GameHeight,
                    inject<OrthographicCamera>() as Camera
                )
            )
            bindSingleton(SeaManager())
            bindSingleton(createWorld())
            bindSingleton(Assets(AssetManager()))
            bindSingleton(getEngine())
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(CurrentChunkSystem(inject()))
            //addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(RenderSystem(inject(), inject()))
            addSystem(SeaCurrentSystem())
            addSystem(ForcesOnCitySystem())
            addSystem(FishMovementSystem())
            addSystem(FishDeathSystem())
            addSystem(TileFoodSystem())
            addSystem(UtilityAiSystem())
//            addSystem(SeaWavesSystem())
        }
    }
}