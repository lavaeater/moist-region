package moist.ecs.components

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.actors.stage
import ktx.actors.txt
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.plus
import ktx.math.times
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.*
import moist.ai.AiCounter
import moist.ai.UtilityAiComponent
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameStats
import moist.ecs.systems.*
import moist.injection.Context
import moist.injection.Context.inject
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
    private val cityEntity by lazy { cities.first() }
    private val city by lazy { cityEntity.city() }

    private val followedFishFamily = allOf(CameraFollow::class, Fish::class).get()
    private val fishFamily = allOf(Fish::class).get()
    private val allFishCount get() = engine().getEntitiesFor(fishFamily).count()

    private val followedFish by lazy { engine().getEntitiesFor(followedFishFamily).first() }
    private val numbers = Array(10) { it * .1f }
    private val interpolators = mutableMapOf(
        "fastSlow" to Interpolation.fastSlow,
        "slowFast" to Interpolation.slowFast,
        "exp10In" to Interpolation.exp10In,
        "exp10out" to Interpolation.exp10Out,
        "pow4In" to Interpolation.pow4In,
        "pow4Out" to Interpolation.pow4Out
    )

    val stage by lazy {
        val aStage = stage(batch, hudViewPort)
        aStage.isDebugAll = debugAll
        aStage.actors {
            boundLabel({
                """
                Population: ${city.population.toInt()} / ${GameConstants.PopulationMax.toInt()}
                Food: ${city.food.toInt()} / ${GameConstants.FoodMax.toInt()}
                Playtime: ${GameStats.playTime.toInt()} (HiScore: ${GameStats.highestPlayTime.toInt()})
                Speed: ${MathUtils.ceil(cityEntity.body().linearVelocity.len() / 5)} knots
                """.trimIndent()
            }) {
                setPosition(20f, 20f)
            }
//            label(
//                """
//                ${interpolators.map { ip -> "${ip.key}: ${numbers.map { (ip.value.apply(it) * 1000f).toInt() }.joinToString(", ")}\n" }}
//            """.trimIndent()
//            ){
//                setPosition(200f, 20f)
//            }
//            boundLabel({
//                "Number of Fish: $allFishCount\n"
//                "Moving: ${followedFish.fish().isMoving}\n" +
//                "Energy: ${followedFish.fish().energy}\n" +
//                "Can Mate: ${followedFish.fish().canMate}\n" +
//                "Mating Count: ${followedFish.fish().matingCount}\n" +
//                        "Unmodded MatingScore: ${MathUtils.norm(0f, GameConstants.MaxFishMatings.toFloat(), GameConstants.MaxFishMatings.toFloat() - followedFish.fish().matingCount.toFloat())}\n" +
//                        "Modde MatingScore: ${Interpolation.exp10In.apply(MathUtils.norm(0f, GameConstants.MaxFishMatings.toFloat(), GameConstants.MaxFishMatings.toFloat() - followedFish.fish().matingCount.toFloat()))}\n" +
//                "Alt Score: ${Interpolation.exp10Out.apply(((MathUtils.norm(0f, GameConstants.FishMaxEnergy, followedFish.fish().energy) + Interpolation.exp10In.apply(MathUtils.norm(0f, GameConstants.MaxFishMatings.toFloat(), GameConstants.MaxFishMatings.toFloat() - followedFish.fish().matingCount.toFloat()))) / 2f)) }\n" +
//                UtilityAiComponent.get(followedFish).actions.joinToString("\n") { "${it.name}: ${(it.score(followedFish) * 100f).toInt()}" }
//            }) {
//                setPosition(10f, 200f)
//            }
        }
        aStage
    }

    private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
    private val compassVector = vec2(300f, 60f)
    private val stopVector = vec2(300f, 60f)

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
        stopVector.lerp(compassVector + cityEntity.body().currentTile().wind * 50f, 0.2f)
        shapeDrawer.batch.use {
            shapeDrawer.filledCircle(compassVector, 5f)
            shapeDrawer.line(compassVector, stopVector, 3f)
        }
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


open class BoundLabel(private val textFunction: () -> String, skin: Skin = Scene2DSkin.defaultSkin) :
    Label(textFunction(), skin) {
    override fun act(delta: Float) {
        txt = textFunction()
        super.act(delta)
    }
}