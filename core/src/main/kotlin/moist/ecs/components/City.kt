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
import moist.core.Assets
import moist.core.GameConstants.MaxTiles
import moist.core.GameConstants.TileSize
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.ecs.systems.fish
import moist.injection.Context.inject
import moist.world.engine
import moist.world.world

class City : Component, Poolable {
    var population = 100
    var food = 1000

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
            population = 100
        }
        with<CameraFollow>()
        with<Renderable> {
            val cityColor = Color(0.01f, 1f, 0.01f, 1f)
            renderType = RenderType.SelfRender { batch, deltaTime ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                val city = this@entity.entity.city()
                cityColor.g = MathUtils.norm(
                    0f,
                    100f,
                    MathUtils.clamp(((city.population + 1) / (city.food + 1)).toFloat(), 0f, 100f)
                )
                shapeDrawer.filledCircle(
                    this@entity.entity.body().position,
                    this@entity.entity.city().population / 100f,
                    cityColor
                )
            }
        }
    }
}

fun fishes() {
    (0..100).forEach {
        engine().entity {
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
                renderType = RenderType.SelfRender { batch, deltaTime ->
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