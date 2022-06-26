package moist.ecs.components

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.actors.stage
import ktx.actors.txt
import ktx.ashley.allOf
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.*
import moist.ai.AiCounter
import moist.core.GameConstants
import moist.core.GameStats
import moist.ecs.systems.city
import moist.injection.Context
import moist.world.engine
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Hud(private val batch: PolygonSpriteBatch, debugAll: Boolean = false) {
    private val aspectRatio = 14f / 9f
    private val hudWidth = 360f
    private val hudHeight = hudWidth * aspectRatio
    private val camera = OrthographicCamera()
    private val hudViewPort = ExtendViewport(hudWidth, hudHeight, camera)
    private val worldCamera by lazy { Context.inject<OrthographicCamera>() }

    private val projectionVector = vec3()
    private val _projectionVector = vec2()
    private val projection2d: Vector2
        get() {
            _projectionVector.set(projectionVector.x, projectionVector.y)
            return _projectionVector
        }

    private val cityFamily = allOf(City::class).get()
    private val cities get() = engine().getEntitiesFor(cityFamily)
    private val city by lazy { cities.first().city() }

    val stage by lazy {
        val aStage = stage(batch, hudViewPort)
        aStage.isDebugAll = debugAll
        aStage.actors {
            boundLabel({ "Population: ${city.population.toInt()} / ${GameConstants.PopulationMax.toInt()}" }) {
                setPosition(20f, 20f)
            }
            boundLabel({ "Food: ${city.food.toInt()} / ${GameConstants.FoodMax.toInt()}" }) {
                setPosition(20f, 40f)
            }
            boundLabel({ "Playtime: ${GameStats.playTime.toInt()} (HiScore: ${GameStats.highestPlayTime.toInt()})" }) {
                setPosition(160f, 40f)
            }
        }
        aStage
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

}


@Scene2dDsl
@OptIn(ExperimentalContracts::class)
inline fun <S> KWidget<S>.boundLabel(
    noinline textFunction: () -> String,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: (@Scene2dDsl BoundLabel).(S) -> Unit = {}
): Label {
    contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }
    return actor(BoundLabel(textFunction, skin), init)
}


open class BoundLabel(private val textFunction: ()-> String, skin: Skin = Scene2DSkin.defaultSkin): Label(textFunction(), skin) {
    override fun act(delta: Float) {
        txt = textFunction()
        super.act(delta)
    }
}