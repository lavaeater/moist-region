package moist.ecs.systems


import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.ExtendViewport
import ktx.ashley.allOf
import ktx.math.vec2
import ktx.math.vec3
import moist.ecs.components.Box
import moist.ecs.components.CameraFollow

class CameraUpdateSystem(
    private val camera: OrthographicCamera,
    private val viewport: ExtendViewport
) :
    IteratingSystem(
        allOf(
            CameraFollow::class,
            Box::class
        ).get()) {

    private val cameraPosition = vec2()

    override fun processEntity(entity: Entity, deltaTime: Float) {

        val body = entity.body()
        cameraPosition.set(body.position)

        camera.position.lerp(
            vec3(cameraPosition, 0f), 0.5f
        )
    }
}