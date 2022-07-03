package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import eater.core.engine
import eater.ecs.components.Box2d
import eater.injection.InjectionContext.Companion.inject
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.math.random
import ktx.math.vec2
import moist.core.Assets
import moist.ecs.components.City
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable

object TrailColor {
    val trailColor = Color(1f, 1f, 1f,0.5f)
}

fun trails(at: Vector2) {
    engine().entity {
        with<Renderable> {
            var time = 0f
            val lifeTime = (2f..10f).random()
            renderType = RenderType.SelfRender(1) { batch, deltaTime ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                time += deltaTime
                if(time > lifeTime) {
                    engine().removeEntity(this@entity.entity)
                }
                shapeDrawer.setColor(TrailColor.trailColor)
                shapeDrawer.circle(at.x, at.y, time * 2.5f)
            }
        }
    }
}

class TrailsSystem : IntervalIteratingSystem(allOf(Box2d::class, City::class).get(),0.25f) {
    override fun processEntity(entity: Entity) {
        val body = entity.body()
        val position = body.position
        val offsetRange = (-10f..10f)
        if((1..10).random() > 7)
            trails(vec2(position.x + offsetRange.random(), position.y + offsetRange.random()))
    }
}