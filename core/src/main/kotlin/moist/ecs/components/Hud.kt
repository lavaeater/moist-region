package moist.ecs.components

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.actors.stage
import ktx.math.vec2
import ktx.math.vec3
import ktx.scene2d.actors
import moist.injection.Context

class Hud(private val batch: PolygonSpriteBatch, debugAll: Boolean = false) {
    private val aspectRatio = 14f / 9f
    private val hudWidth = 720f
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

    val stage by lazy {
        val aStage = stage(batch, hudViewPort)
        aStage.isDebugAll = debugAll
        aStage
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

}