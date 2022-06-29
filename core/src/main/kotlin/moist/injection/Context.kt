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
import moist.core.Assets
import moist.core.GameConstants.GameHeight
import moist.core.GameConstants.GameWidth
import moist.ecs.components.Hud
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
            bindSingleton(createWorld().apply {
                setContactListener(FishAndGameManagement())
            })
            bindSingleton(Assets(AssetManager()))
            bindSingleton(getEngine())
            bindSingleton(Hud(inject()))
        }
    }

    private fun getEngine(): Engine {
        return PooledEngine().apply {
            addSystem(CameraUpdateSystem(inject(), inject()))
            addSystem(CurrentChunkSystem(inject()))
            //addSystem(PhysicsDebugRendererSystem(inject(), inject()))
            addSystem(RenderSystem(inject(), inject()))
            addSystem(SeaCurrentSystem(inject()))
            addSystem(WindSystem(inject()))
            addSystem(TemperatureChangeSystem(inject()))
            addSystem(ForcesOnCitySystem(inject()))
            addSystem(FishMovementSystem())
            addSystem(FishDeathSystem())
            addSystem(TileFoodSystem(inject()))
            addSystem(UtilityAiSystem())
            addSystem(UpdateActionsSystem())
            addSystem(FisherySystem())
            //addSystem(CityHungerSystem())
            addSystem(CloudSystem())
            addSystem(TrailsSystem())
            addSystem(AudioSystem())
//            addSystem(SeaWavesSystem())
        }
    }
}