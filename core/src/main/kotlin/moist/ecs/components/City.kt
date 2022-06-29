package moist.ecs.components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.utils.Pool.Poolable
import ktx.ashley.entity
import ktx.ashley.with
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.circle
import ktx.box2d.filter
import ktx.math.*
import moist.ai.UtilityAiComponent
import moist.core.Assets
import moist.core.Box2dCategories
import moist.core.GameConstants.FoodMax
import moist.core.GameConstants.FoodMin
import moist.core.GameConstants.MaxTilesPerSide
import moist.core.GameConstants.PopulationMax
import moist.core.GameConstants.PopulationMin
import moist.core.GameConstants.StartFishCount
import moist.core.GameConstants.TileSize
import moist.ecs.systems.body
import moist.ecs.systems.city
import moist.injection.Context.inject
import moist.world.SeaManager
import moist.world.engine
import moist.world.world

class City : Component, Poolable {
    val drag = vec2()
    val sailVector: Vector2 = Vector2.Y
    var population = 100f
    var food = 100f//FoodMax / 2

    val currentForce = vec2()
    val windForce = vec2()

    val potentialCatches = mutableMapOf<Entity, Float>()

    override fun reset() {
        population = 100f
        currentForce.setZero()
        windForce.setZero()
        food = 100f// FoodMax / 2
        potentialCatches.clear()
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
                    friction = 10f //Tune
                    density = 1f //tune
//                    restitution = 0.9f
                    filter {
                        categoryBits = Box2dCategories.cities
                        maskBits = Box2dCategories.whatCitiesCollideWith
                    }
                }
                circle(radius = 60f) {
                    isSensor = true
                }
            }
        }
        with<City> {
            population = 100f
        }
        with<CameraFollow>()
        with<Renderable> {
            val sprite = inject<Assets>().citySprite
            val cityColor = Color(0f, 0f, 0f, .1f)
            val spritePos = vec2()
            renderType = RenderType.SelfRender(2) { batch, _ ->
                val shapeDrawer = inject<Assets>().shapeDrawer
                val body = this@entity.entity.body()
                val position = body.position
                val city = this@entity.entity.city()
                val rows = 8
                val start = 0 - rows / 2
                val stop = rows / 2

                for (x in start until stop)
                    for (y in start until stop) {
                        spritePos.set(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.setOriginBasedPosition(position.x - x * sprite.width, position.y - y * sprite.height)
                        sprite.draw(batch)
                    }

                val healthBarStart = position - vec2(rows / 2 * sprite.width - sprite.width, 0f + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f,Color.BLACK, 3f)

                val normalizedFood = MathUtils.norm(FoodMin, FoodMax, city.food)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedFood, Color.GREEN, 3f)

                healthBarStart.set(healthBarStart.x, healthBarStart.y + sprite.height * 2f)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f,Color.BLACK, 3f)
                val normalizedPop = MathUtils.norm(PopulationMin, PopulationMax, city.population)
                shapeDrawer.line(healthBarStart, healthBarStart + Vector2.X * 50f * normalizedPop, Color.RED, 3f)

                val sailStart = position - (city.sailVector * 50)//.rotate90(1)
                val sailStop = position + (city.sailVector * 50)//.rotate90(1)
                val sailBulge = position + (city.sailVector * 50).rotateDeg(45f)

                val sailArray = listOf(sailStart.x, sailStart.y, sailBulge.x, sailBulge.y, sailStop.x, sailStop.y)
                shapeDrawer.setColor(Color.WHITE)
                shapeDrawer.filledPolygon(sailArray.toFloatArray())

                shapeDrawer.line(sailStart, sailStop, Color.BLACK, 3f)
//                shapeDrawer.line(position, position + body.linearVelocity, Color.RED, 3f)
                shapeDrawer.setColor(cityColor)
                shapeDrawer.filledCircle(position.x, position.y, 60f)
                shapeDrawer.setColor(Color.WHITE)

            }
        }
    }
}

fun cloud(cloudPos: Vector2) {
    engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(cloudPos)
                circle(1f) {
                    filter {
                        categoryBits = Box2dCategories.cloud
                        maskBits = Box2dCategories.whatCloudsCollideWith
                    }
                }
            }
        }
        with<Renderable> {
            renderType = RenderType.Cloud
        }
        with<Cloud> {
            val v = Vector2.X.cpy()
            val range = 0f..75f
            val sizeRange = 15f..50f
            val angleRange = 0f..359f
            for(i in 1 until (4..7).random()) {
                val randomSpot = (v * range.random()).rotateDeg(angleRange.random())
                cloudPuffs.add(Circle(randomSpot.x, randomSpot.y, sizeRange.random()))
            }
        }
    }
}

fun fish(fishPos: Vector2, cameraFollow: Boolean = false) {
    engine().entity {
        with<Box> {
            body = world().body {
                userData = this@entity.entity
                type = BodyDef.BodyType.DynamicBody
                position.set(fishPos)
                box(.5f, .5f) {
                    density = 1f
                    filter {
                        categoryBits = Box2dCategories.fish
                        maskBits = Box2dCategories.whatFishCollideWith
                    }
                }
            }
        }
        with<Fish> {
            id = 0
            canDie = !cameraFollow
        }
        if(cameraFollow)
            with<CameraFollow>()
        with<UtilityAiComponent>()
        with<Renderable> {
            renderType = RenderType.RenderAnimation(0, inject<Assets>().fishAnim)
        }
    }
}

fun randomFish() {
    val seaManager = inject<SeaManager>()
    val minX = seaManager.getCurrentTiles().minByOrNull { it.x }!!.x * TileSize
    val maxX = seaManager.getCurrentTiles().maxByOrNull { it.x }!!.x * TileSize
    val minY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * TileSize
    val maxY = seaManager.getCurrentTiles().maxByOrNull { it.y }!!.y * TileSize
    val xRange = minX..maxX
    val yRange = minY..maxY
    fish(vec2(xRange.random(), yRange.random()))
}

fun fishes() {
    val min = 0 + TileSize
    val max = MaxTilesPerSide * TileSize - TileSize
    val range = min..max
    (0 until StartFishCount).forEach { _ ->
        val fishPos = vec2(range.random(), range.random())
        fish(fishPos)//, it == 0)
    }
}

fun sea() {
    engine().entity {
        with<Renderable> {
            renderType = RenderType.Sea()
        }
    }
}
