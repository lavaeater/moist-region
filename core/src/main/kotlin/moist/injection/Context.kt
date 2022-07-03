package moist.injection

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.ecs.systems.CameraUpdateSystem
import eater.ecs.systems.CurrentChunkSystem
import eater.ecs.systems.UpdateActionsSystem
import eater.ecs.systems.UtilityAiSystem
import eater.injection.InjectionContext
import eater.world.ITileManager
import ktx.box2d.createWorld
import moist.core.Assets
import moist.core.GameConstants.GameHeight
import moist.core.GameConstants.GameWidth
import moist.core.GameConstants.TileSize
import moist.ui.Hud
import moist.ecs.systems.*
import moist.world.SeaManager

object Context : InjectionContext() {
    fun initialize() {
        buildContext {
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
            addSystem(CurrentChunkSystem(inject<SeaManager>() as ITileManager, TileSize))
            addSystem(RenderSystem(inject(), inject()))
            addSystem(SeaCurrentSystem(inject()))
            addSystem(WindSystem(inject()))
            addSystem(TemperatureChangeSystem(inject()))
            addSystem(ForcesOnCitySystem(inject()))
            addSystem(FishMovementSystem())
            addSystem(SharkMovementSystem())
            addSystem(FishDeathSystem())
            addSystem(TileFoodSystem(inject()))
            addSystem(UtilityAiSystem())
            addSystem(UpdateActionsSystem())
            addSystem(FisherySystem())
            addSystem(CityHungerSystem())
            addSystem(CloudSystem())
            addSystem(TrailsSystem())
            addSystem(AudioSystem())
        }
    }
}