package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.math.vec2
import moist.core.Assets
import moist.core.GameConstants.MaxTiles
import moist.core.GameConstants.TileSize
import moist.core.GameConstants.foodMax
import moist.core.GameConstants.foodMin
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.ecs.systems.fish
import moist.injection.Context.inject
import moist.world.engine
import moist.world.world
import kotlin.math.sqrt

class City : Component, Poolable {
    var population = 100f
    var food = 1000f

    override fun reset() {

    }
}

fun city(): Entity {
    return engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set((MaxTiles / 2) * TileSize, (MaxTiles / 2) * TileSize)
//                linearDamping = 5f
                circle(1f) {
//                    friction = 10f //Tune
                    //                   density = 1f //tune
//                    restitution = 0.9f
                }
            }
        }
        with<City> {
            population = 100f
        }
//        with<CameraFollow>()
        with<Renderable> {
            val sprite = inject<Assets>().citySprite
            val cityColor = Color(0.01f, 1f, 0.01f, 1f)
            val spritePos = vec2()
            renderType = RenderType.SelfRender(2) { batch, deltaTime ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                val position = this@entity.entity.body().position
                val city = this@entity.entity.city()
                val rows = sqrt(city.population.toDouble()).toInt()
                val start = 0 - rows / 2
                val stop = rows / 2
                val radius = rows / 2 * sprite.width

                for (x in start until stop)
                    for (y in start until stop) {
                        spritePos.set(position.x - x * sprite.width, position.y - y * sprite.height)
                        if(spritePos.dst(position) < radius) {
                            sprite.setOriginBasedPosition(position.x - x * sprite.width, position.y - y * sprite.height)
                            sprite.draw(batch)
                        }
                    }


//                cityColor.g = MathUtils.norm(foodMin, foodMax, city.food)
//                shapeDrawer.filledCircle(
//                    ,
//                    this@entity.entity.city().population / 100f,
//                    cityColor
//                )
            }
        }
    }
}

fun fishes() {
    (0..100).forEach {
        engine().entity {
            if(it == 0)
                with<CameraFollow>()
            with<Box> {
                body = world().body {
                    userData = this@entity.entity
                    type = BodyDef.BodyType.DynamicBody
                    position.set((MaxTiles / 2) * TileSize, (MaxTiles / 2) * TileSize)
                    box(.5f, .5f) {
                    }
                }
            }
            with<Fish>()
            with<Renderable> {
                renderType = RenderType.SelfRender(0) { batch, deltaTime ->
                    val shapeDrawer = inject<Assets>().shapeDrawer
                    val fish = this@entity.entity.fish()
                    shapeDrawer.filledCircle(
                        this@entity.entity.body().position,
                        0.25f,
                        Color.YELLOW
                    )
                }
            }
        }
    }
}