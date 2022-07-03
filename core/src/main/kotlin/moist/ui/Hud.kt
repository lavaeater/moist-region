package moist.ui

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.ExtendViewport
import eater.core.engine
import eater.ecs.components.CameraFollow
import eater.injection.InjectionContext.Companion.inject
import ktx.actors.stage
import ktx.actors.txt
import ktx.ashley.allOf
import ktx.graphics.use
import ktx.math.*
import ktx.scene2d.*
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameStats
import moist.ecs.components.City
import moist.ecs.components.Fish
import moist.ecs.components.GlobalDebug
import moist.ecs.components.Shark
import moist.ecs.systems.*
import moist.world.currentTile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Hud(private val batch: PolygonSpriteBatch, debugAll: Boolean = false) {
    private val aspectRatio = 14f / 9f
    private val hudWidth = 360f
    private val hudHeight = hudWidth * aspectRatio
    private val camera = OrthographicCamera()
    private val hudViewPort = ExtendViewport(hudWidth, hudHeight, camera)
    private val worldCamera by lazy { inject<OrthographicCamera>() }

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

    private val fishFamily = allOf(Fish::class).get()
    private val allFishCount get() = engine().getEntitiesFor(fishFamily).count()
    private val sharkFamily = allOf(Shark::class).get()
    private val allsharkCount get() = engine().getEntitiesFor(sharkFamily).count()

    private val followedEntityFamily = allOf(CameraFollow::class).get()
    private val followedEntity get() = engine().getEntitiesFor(followedEntityFamily).firstOrNull()

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
            if(GlobalDebug.globalDebug) {
                boundLabel({
                    val moving =
                        if (followedEntity?.isCreature() == true) (followedEntity as Entity).creature().isMoving else true
                    val size =
                        if (followedEntity?.isCreature() == true) (followedEntity as Entity).creature().size else 0f
                    """Fishes: $allFishCount
                Sharks: $allsharkCount
                Dead Sharks: ${GameStats.deadSharks}
                Dead Fish: ${GameStats.deadFish}
                Velocity2: ${followedEntity?.body()?.linearVelocity?.len()}
                Size: $size
                Moving: $moving
                Energy: ${if (followedEntity?.isCreature() == true) followedEntity?.creature()?.energy else 0f}
                ${
                        if (followedEntity?.hasAi() == true) followedEntity?.ai()?.actions?.joinToString("\n") {
                            return@joinToString "${it.name}: ${
                                (it.score(
                                    (followedEntity as Entity)
                                ) * 100f).toInt()
                            }"
                        } else {
                            ""
                        }
                    }
            """
                }) {
                    setPosition(10f, 200f)
                }
            }
        }
        aStage
    }

    private val shapeDrawer by lazy { inject<Assets>().shapeDrawer }
    private val compassVector = vec2(300f, 60f)
    private val stopVector = vec2(300f, 60f)
    private val windDirectionVector = vec2(0f,0f)

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
        stopVector.lerp(compassVector + cityEntity.body().currentTile().wind * 50f, 0.2f)
        windDirectionVector.lerp(compassVector + cityEntity.body().linearVelocity / 3f, 0.2f)
        shapeDrawer.batch.use {
            shapeDrawer.filledCircle(compassVector, 5f)
            shapeDrawer.line(compassVector, stopVector, 3f)
            shapeDrawer.setColor(Color.RED)
            shapeDrawer.line(compassVector, windDirectionVector, 3f)
            shapeDrawer.setColor(Color.WHITE)
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