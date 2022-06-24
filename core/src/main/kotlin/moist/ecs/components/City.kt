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
import ktx.box2d.filter
import ktx.math.random
import ktx.math.vec2
import moist.ai.UtilityAiComponent
import moist.core.Assets
import moist.core.Box2dCategories
import moist.core.GameConstants.FishMaxEnergy
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.StartFishCount
import moist.core.GameConstants.TileSize
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
                position.set((MaxTilesPerSide / 2) * TileSize, (MaxTilesPerSide / 2) * TileSize)
//                linearDamping = 5f
                box(2f, 2f) {
//                    friction = 10f //Tune
                    density = 1f //tune
//                    restitution = 0.9f
                    filter {
                        categoryBits = Box2dCategories.cities
                        maskBits = Box2dCategories.whatCitiesCollideWith
                    }
                }
            }
        }
        with<City> {
            population = 100f
        }
        with<CameraFollow>()
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
                        sprite.setOriginBasedPosition(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.draw(batch)
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
    val min = 0 + TileSize
    val max = MaxTilesPerSide * TileSize - TileSize
    val range = min..max
    val shoalStartPoint = vec2(range.random(), range.random())
    (0 until StartFishCount).forEach {
        engine().entity {
            with<Box> {
                body = world().body {
                    userData = this@entity.entity
                    type = BodyDef.BodyType.DynamicBody
                    position.set(range.random(), range.random())
                    box(.5f, .5f) {
                        density = 1f
                        filter {
                            categoryBits = Box2dCategories.fish
                            maskBits = Box2dCategories.whatFishCollideWith
                        }
                    }
                }
            }
            with<Fish>()
            with<UtilityAiComponent>()
            with<Renderable> {
                val fishColor = Color(1f, 0f, 1f, 1f)
                renderType = RenderType.SelfRender(0) { batch, deltaTime ->
                    val shapeDrawer = inject<Assets>().shapeDrawer
                    val fish = this@entity.entity.fish()
                    fishColor.g = MathUtils.norm(0f, FishMaxEnergy, fish.energy)
                    shapeDrawer.filledCircle(
                        this@entity.entity.body().position,
                        1.0f,
                        fishColor
                    )
                }
            }
        }
    }
}

fun sea() {
    engine().entity {
        with<Renderable> {
            renderType = RenderType.Sea()
        }
    }
}