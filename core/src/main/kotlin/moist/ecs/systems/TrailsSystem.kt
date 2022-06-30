package moist.ecs.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector
import com.badlogic.gdx.math.Vector2
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.with
import ktx.math.random
import ktx.math.times
import ktx.math.vec2
import moist.core.Assets
import moist.core.GameConstants
import moist.core.GameConstants.WindMagnitude
import moist.ecs.components.Box
import moist.ecs.components.City
import moist.ecs.components.RenderType
import moist.ecs.components.Renderable
import moist.injection.Context.inject
import moist.world.SeaManager
import moist.world.engine

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

class TrailsSystem : IntervalIteratingSystem(allOf(Box::class, City::class).get(),0.25f) {
    override fun processEntity(entity: Entity) {
        val body = entity.body()
        val position = body.position
        val offsetRange = (-10f..10f)
        if((1..10).random() > 7)
            trails(vec2(position.x + offsetRange.random(), position.y + offsetRange.random()))
    }
}